package io.github.qeesung.setting;

import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.PlainSyntaxHighlighter;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.xml.XmlTokenType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

import static io.github.qeesung.brace.BraceTokenTypes.DOUBLE_QUOTE;

/**
 * Color settings for different bracket pair shape.
 */
public class HighlightBracketPairSettingsPage implements ColorSettingsPage {
    public static final TextAttributesKey BRACE_ATTR =
            TextAttributesKey.createTextAttributesKey("BRACE_ATTR");
    public static final TextAttributesKey BRACE_LINE_ATTR =
            TextAttributesKey.createTextAttributesKey("BRACE_LINE_ATTR");
    public static final TextAttributesKey BRACKET_ATTR =
            TextAttributesKey.createTextAttributesKey("BRACKET_ATTR");
    public static final TextAttributesKey BRACKET_LINE_ATTR =
            TextAttributesKey.createTextAttributesKey("BRACKET_LINE_ATTR");
    public static final TextAttributesKey PARENTHESIS_ATTR =
            TextAttributesKey.createTextAttributesKey("PARENTHESIS_ATTR");
    public static final TextAttributesKey PARENTHESIS_LINE_ATTR =
            TextAttributesKey.createTextAttributesKey("PARENTHESIS_LINE_ATTR");
    public static final TextAttributesKey DOUBLE_QUOTE_ATTR =
            TextAttributesKey.createTextAttributesKey("DOUBLE_QUOTE_ATTR");
    public static final TextAttributesKey DOUBLE_QUOTE_LINE_ATTR =
            TextAttributesKey.createTextAttributesKey("DOUBLE_QUOTE_LINE_ATTR");
    public static final TextAttributesKey CUSP_BRACKETS_ATTR =
            TextAttributesKey.createTextAttributesKey("CUSP_BRACKETS_ATTR");
    public static final TextAttributesKey CUSP_BRACKETS_LINE_ATTR =
            TextAttributesKey.createTextAttributesKey("CUSP_BRACKETS_LINE_ATTR");

    private static final Map<String, TextAttributesKey> TAGS = new HashMap<>();
    private static final AttributesDescriptor[] ATTRIBUTESDESC = {
            new AttributesDescriptor("Brace", BRACE_ATTR),
            new AttributesDescriptor("BraceLine", BRACE_LINE_ATTR),
            new AttributesDescriptor("Bracket", BRACKET_ATTR),
            new AttributesDescriptor("BracketLine", BRACKET_LINE_ATTR),
            new AttributesDescriptor("Parenthesis", PARENTHESIS_ATTR),
            new AttributesDescriptor("ParenthesisLine", PARENTHESIS_LINE_ATTR),
            new AttributesDescriptor("DoubleQuote", DOUBLE_QUOTE_ATTR),
            new AttributesDescriptor("DoubleQuoteLine", DOUBLE_QUOTE_LINE_ATTR),
            new AttributesDescriptor("CuspBracket", CUSP_BRACKETS_ATTR),
            new AttributesDescriptor("CuspBracketLine", CUSP_BRACKETS_LINE_ATTR),
    };
    private static final Map<IElementType, TextAttributesKey> ELETYPE2ATTR = new HashMap<>();
    private static final Map<String, TextAttributesKey> CONTENT2ATTR = new HashMap<>();

    static {
        ELETYPE2ATTR.put(XmlTokenType.XML_START_TAG_START, CUSP_BRACKETS_ATTR);
        ELETYPE2ATTR.put(XmlTokenType.XML_ATTRIBUTE_VALUE_START_DELIMITER, DOUBLE_QUOTE_ATTR);
        ELETYPE2ATTR.put(DOUBLE_QUOTE, DOUBLE_QUOTE_ATTR);
    }

    static {
        CONTENT2ATTR.put("{", BRACE_ATTR);
        CONTENT2ATTR.put("{_", BRACE_LINE_ATTR);
        CONTENT2ATTR.put("[", BRACKET_ATTR);
        CONTENT2ATTR.put("[_", BRACKET_LINE_ATTR);
        CONTENT2ATTR.put("(", PARENTHESIS_ATTR);
        CONTENT2ATTR.put("(_", PARENTHESIS_LINE_ATTR);
        CONTENT2ATTR.put("<", CUSP_BRACKETS_LINE_ATTR);
        CONTENT2ATTR.put("<_", CUSP_BRACKETS_LINE_ATTR);
    }

    static {
        TAGS.put("Brace", BRACE_ATTR);
        TAGS.put("BraceLine", BRACE_LINE_ATTR);
        TAGS.put("Bracket", BRACKET_ATTR);
        TAGS.put("BracketLine", BRACKET_LINE_ATTR);
        TAGS.put("Parenthesis", PARENTHESIS_ATTR);
        TAGS.put("ParenthesisLine", PARENTHESIS_LINE_ATTR);
        TAGS.put("DoubleQuote", DOUBLE_QUOTE_ATTR);
        TAGS.put("DoubleQuoteLine", DOUBLE_QUOTE_LINE_ATTR);
        TAGS.put("CuspBracket", CUSP_BRACKETS_ATTR);
        TAGS.put("CuspBracketLine", CUSP_BRACKETS_LINE_ATTR);
    }

    public static TextAttributesKey getTextAttributesKeyByToken(IElementType type) {
        return ELETYPE2ATTR.get(type);
    }

    public static TextAttributesKey getTextAttributesKeyByText(String content) {
        return CONTENT2ATTR.get(content);
    }

    @Nullable
    @Override
    public Icon getIcon() {
        return null;
    }

    @NotNull
    @Override
    public SyntaxHighlighter getHighlighter() {
        return new PlainSyntaxHighlighter();
    }

    @NotNull
    @Override
    public String getDemoText() {
        return "<Brace>{</Brace>...<Brace>}</Brace>" +
                " <Parenthesis>(</Parenthesis>...<Parenthesis>)</Parenthesis>" +
                " <Bracket>[</Bracket>...<Bracket>]</Bracket>" +
                " <CuspBracket><</CuspBracket>...<CuspBracket>></CuspBracket>" +
                " <DoubleQuote>\"</DoubleQuote>...<DoubleQuote>\"</DoubleQuote>";
    }

    @Nullable
    @Override
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return TAGS;
    }

    @NotNull
    @Override
    public AttributesDescriptor[] getAttributeDescriptors() {
        return ATTRIBUTESDESC;
    }

    @NotNull
    @Override
    public ColorDescriptor[] getColorDescriptors() {
        return new ColorDescriptor[0];
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "HighlightBracketPair";
    }
}
