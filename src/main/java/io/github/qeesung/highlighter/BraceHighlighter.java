package io.github.qeesung.highlighter;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.IndentGuideDescriptor;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.tree.IElementType;
import io.github.qeesung.adapter.BraceMatchingUtilAdapter;
import io.github.qeesung.brace.Brace;
import io.github.qeesung.brace.BracePair;
import io.github.qeesung.setting.HighlightBracketPairSettingsPage;
import io.github.qeesung.util.Pair;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static io.github.qeesung.brace.BraceTokenTypes.DOUBLE_QUOTE;

/**
 * Brace highlighter abstract class.
 */
abstract public class BraceHighlighter {
    public final static int NON_OFFSET = -1;
    public final static int HIGHLIGHT_LAYER_WEIGHT = 100;
    public final static BracePair EMPTY_BRACE_PAIR =
            new BracePair.BracePairBuilder().
                    leftOffset(NON_OFFSET).
                    rightOffset(NON_OFFSET).build();

    protected Editor editor;
    protected Project project;
    protected Document document;
    protected FileType fileType;
    protected CharSequence fileText;
    protected PsiFile psiFile;
    protected MarkupModelEx markupModelEx;

    public BraceHighlighter(Editor editor) {
        this.editor = editor;
        this.project = this.editor.getProject();
        this.document = this.editor.getDocument();
        this.psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
        this.fileType = psiFile.getFileType();
        this.fileText = this.editor.getDocument().getImmutableCharSequence();
        this.markupModelEx = (MarkupModelEx) this.editor.getMarkupModel();
    }

    /**
     * @return
     */
    public List<Pair<IElementType, IElementType>> getSupportedBraceToken() {
        return new LinkedList<>();
    }

    public BracePair findClosetBracePairInBraceTokens(int offset) {
        EditorHighlighter editorHighlighter = ((EditorEx) editor).getHighlighter();
        boolean isBlockCaret = this.isBlockCaret();
        List<Pair<IElementType, IElementType>> braceTokens = this.getSupportedBraceToken();
        for (Pair<IElementType, IElementType> braceTokenPair :
                braceTokens) {
            HighlighterIterator leftTraverseIterator = editorHighlighter.createIterator(offset);
            HighlighterIterator rightTraverseIterator = editorHighlighter.createIterator(offset);
            int leftBraceOffset = BraceMatchingUtilAdapter.findLeftLParen(
                    leftTraverseIterator, braceTokenPair.getLeft(), this.fileText, this.fileType, isBlockCaret);
            int rightBraceOffset = BraceMatchingUtilAdapter.findRightRParen(
                    rightTraverseIterator, braceTokenPair.getRight(), this.fileText, this.fileType, isBlockCaret);
            if (leftBraceOffset != NON_OFFSET && rightBraceOffset != NON_OFFSET) {
                return new BracePair.BracePairBuilder().
                        leftType(braceTokenPair.getLeft()).
                        rightType(braceTokenPair.getRight()).
                        leftIterator(leftTraverseIterator).
                        rightIterator(rightTraverseIterator).build();


            }
        }
        return EMPTY_BRACE_PAIR;
    }

    public BracePair findClosetBracePairInStringSymbols(int offset) {
        if (offset < 0 || this.fileText == null || this.fileText.length() == 0)
            return EMPTY_BRACE_PAIR;
        EditorHighlighter editorHighlighter = ((EditorEx) editor).getHighlighter();
        HighlighterIterator iterator = editorHighlighter.createIterator(offset);
        IElementType type = iterator.getTokenType();
        boolean isBlockCaret = this.isBlockCaret();
        if (!BraceMatchingUtilAdapter.isStringToken(type))
            return EMPTY_BRACE_PAIR;

        int leftOffset = iterator.getStart();
        int rightOffset = iterator.getEnd() - 1;
        if (!isBlockCaret && leftOffset == offset)
            return EMPTY_BRACE_PAIR;
        return new BracePair.BracePairBuilder().
                leftType(DOUBLE_QUOTE).
                rightType(DOUBLE_QUOTE).
                leftOffset(leftOffset).
                rightOffset(rightOffset).build();
    }

    public BracePair findClosetBracePair(int offset) {
        BracePair braceTokenBracePair = this.findClosetBracePairInBraceTokens(offset);
        BracePair stringSymbolBracePair = this.findClosetBracePairInStringSymbols(offset);
        if (
                (offset - braceTokenBracePair.getLeftBrace().getOffset() >
                        offset - stringSymbolBracePair.getLeftBrace().getOffset())
                        && (offset - braceTokenBracePair.getRightBrace().getOffset() <
                        offset - stringSymbolBracePair.getRightBrace().getOffset()
                )) {
            return stringSymbolBracePair;
        } else {
            return braceTokenBracePair;
        }
    }

    public List<RangeHighlighter> highlightPairLines(BracePair bracePair) {
        final Brace leftBrace = bracePair.getLeftBrace();
        final Brace rightBrace = bracePair.getRightBrace();
        final int leftBraceOffset = leftBrace.getOffset();
        final int rightBraceOffset = rightBrace.getOffset();
        final String leftBraceText = leftBrace.getText() + "_";

        if (leftBraceOffset == NON_OFFSET ||
                rightBraceOffset == NON_OFFSET)
            return null;

        // try to get the text attr by element type
        TextAttributesKey textAttributesKey =
                HighlightBracketPairSettingsPage.getTextAttributesKeyByToken(leftBrace.getElementType());
        // if not found, get the text attr by brace text
        if (textAttributesKey == null) {
            textAttributesKey = HighlightBracketPairSettingsPage.getTextAttributesKeyByText(leftBraceText);
        }
        final TextAttributes textAttributes = editor.getColorsScheme().getAttributes(textAttributesKey);

        int lineOfLeftBrace = document.getLineNumber(leftBraceOffset);
        int lineOfRightBrace = document.getLineNumber(rightBraceOffset);

        IndentGuideDescriptor indentGuideDescriptor =
                editor.getIndentsModel().getDescriptor(lineOfLeftBrace, lineOfRightBrace);

        int level = 0;
        if (indentGuideDescriptor != null) {
            level = indentGuideDescriptor.indentLevel;
        }

        if (level == 0) {
            if (editor.getIndentsModel().getCaretIndentGuide() != null) {
                level = editor.getIndentsModel().getCaretIndentGuide().indentLevel;
            }
        }

        if (level == 0) {
            IndentGuideDescriptor indentGuideDescriptorInner =
                    editor.getIndentsModel().getDescriptor(lineOfLeftBrace + 1, lineOfRightBrace - 1);
            if (indentGuideDescriptorInner != null) {
                level = indentGuideDescriptorInner.indentLevel;
            }
        }

        List<RangeHighlighter> result;

        if (lineOfLeftBrace == lineOfRightBrace) {
            result = highlightOneLines(leftBraceOffset, rightBraceOffset, textAttributes);
        } else {
            result = highlightTwoLines(leftBraceOffset, rightBraceOffset, level, textAttributes);
        }

        return result;
    }

    public List<RangeHighlighter> highlightTwoLines(int leftBraceOffset, int rightBraceOffset, int level, TextAttributes textAttributes) {
        int lineOfLeftBrace = document.getLineNumber(leftBraceOffset);
        int lineOfRightBrace = document.getLineNumber(rightBraceOffset);

        int lineStartLeftIndentOffset =
                document.getLineStartOffset(lineOfLeftBrace) + level;

        int lineStartRightIndentOffset =
                document.getLineStartOffset(lineOfRightBrace) + level;

        if (level == 0) return null;
        if (lineStartLeftIndentOffset > leftBraceOffset) return null;
        if (lineStartRightIndentOffset > rightBraceOffset) return null;

        RangeHighlighter leftHighlighter = markupModelEx.addRangeHighlighter(
                lineStartLeftIndentOffset,
                leftBraceOffset,
                HighlighterLayer.SELECTION + HIGHLIGHT_LAYER_WEIGHT,
                textAttributes,
                HighlighterTargetArea.EXACT_RANGE);

        RangeHighlighter rightHighlighter = markupModelEx.addRangeHighlighter(
                lineStartRightIndentOffset,
                rightBraceOffset,
                HighlighterLayer.SELECTION + HIGHLIGHT_LAYER_WEIGHT,
                textAttributes,
                HighlighterTargetArea.EXACT_RANGE);


        List<RangeHighlighter> verticalLineHighlighters =
                renderVerticalLineBetweenTwoLines(lineOfLeftBrace, lineOfRightBrace, level, textAttributes);

        List<RangeHighlighter> highlighters = new ArrayList<RangeHighlighter>();
        highlighters.add(leftHighlighter);
        highlighters.add(rightHighlighter);
        highlighters.addAll(verticalLineHighlighters);

        return highlighters;
    }

    public List<RangeHighlighter> renderVerticalLineBetweenTwoLines(int firstLine, int secondLine, int indent, TextAttributes textAttributes) {

        List<RangeHighlighter> highlighters = new ArrayList<RangeHighlighter>();

        for (int line = 1; line < secondLine; line++) {
            int lineStartOffset = document.getLineStartOffset(firstLine + line);

            if (firstLine + line == secondLine) return highlighters;

            RangeHighlighter highlighter;
            if (lineStartOffset == document.getLineEndOffset(firstLine + line)) {
                highlighter = markupModelEx.addRangeHighlighter(
                        lineStartOffset + indent,
                        lineStartOffset + indent,
                        HighlighterLayer.SELECTION + HIGHLIGHT_LAYER_WEIGHT,
                        textAttributes,
                        HighlighterTargetArea.EXACT_RANGE);
            } else {
                highlighter = markupModelEx.addRangeHighlighter(
                        lineStartOffset + indent,
                        lineStartOffset + indent + 1,
                        HighlighterLayer.SELECTION + HIGHLIGHT_LAYER_WEIGHT,
                        textAttributes,
                        HighlighterTargetArea.EXACT_RANGE);
            }

            highlighters.add(highlighter);

        }

        return highlighters;
    }


    public List<RangeHighlighter> highlightOneLines(int leftBraceOffset, int rightBraceOffset, TextAttributes textAttributes) {
        RangeHighlighter leftHighlighter = markupModelEx.addRangeHighlighter(
                leftBraceOffset,
                rightBraceOffset,
                HighlighterLayer.SELECTION + HIGHLIGHT_LAYER_WEIGHT,
                textAttributes,
                HighlighterTargetArea.EXACT_RANGE);

        List<RangeHighlighter> highlighters = new ArrayList<RangeHighlighter>();
        highlighters.add(leftHighlighter);

        return highlighters;
    }


    public Pair<RangeHighlighter, RangeHighlighter> highlightPair(BracePair bracePair) {
        final Brace leftBrace = bracePair.getLeftBrace();
        final Brace rightBrace = bracePair.getRightBrace();
        final int leftBraceOffset = leftBrace.getOffset();
        final int rightBraceOffset = rightBrace.getOffset();
        final String leftBraceText = leftBrace.getText();
        final String rightBraceText = rightBrace.getText();

        if (leftBraceOffset == NON_OFFSET ||
                rightBraceOffset == NON_OFFSET)
            return null;
        // try to get the text attr by element type
        TextAttributesKey textAttributesKey =
                HighlightBracketPairSettingsPage.getTextAttributesKeyByToken(leftBrace.getElementType());
        // if not found, get the text attr by brace text
        if (textAttributesKey == null) {
            textAttributesKey = HighlightBracketPairSettingsPage.getTextAttributesKeyByText(leftBraceText);
        }
        final TextAttributes textAttributes = editor.getColorsScheme().getAttributes(textAttributesKey);

        RangeHighlighter leftHighlighter = markupModelEx.addRangeHighlighter(
                leftBraceOffset,
                leftBraceOffset + leftBraceText.length(),
                HighlighterLayer.SYNTAX + HIGHLIGHT_LAYER_WEIGHT,
                textAttributes,
                HighlighterTargetArea.EXACT_RANGE);
        RangeHighlighter rightHighlighter = markupModelEx.addRangeHighlighter(
                rightBraceOffset,
                rightBraceOffset + rightBraceText.length(),
                HighlighterLayer.SYNTAX + HIGHLIGHT_LAYER_WEIGHT,
                textAttributes,
                HighlighterTargetArea.EXACT_RANGE);
        return new Pair<>(leftHighlighter, rightHighlighter);
    }

    public void eraseHighlight(List<RangeHighlighter> list) {
        for (RangeHighlighter l :
                list) {
            this.markupModelEx.removeHighlighter(l);
        }
    }

    public boolean isBlockCaret() {
        return this.editor.getSettings().isBlockCursor();
    }
}
