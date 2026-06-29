package dev.vepo.engage.comments.wordcloud;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class CommentHtmlText {

    private static final Pattern NUMERIC_HEX = Pattern.compile("&#x([0-9A-Fa-f]+);");
    private static final Pattern NUMERIC_DECIMAL = Pattern.compile("&#(\\d+);");
    private static final Pattern NAMED_ENTITY = Pattern.compile("&([a-zA-Z]+);");
    private static final Pattern HTML_TAG = Pattern.compile("<[^>]+>");
    private static final Map<String, String> NAMED_ENTITIES = Map.ofEntries(
                                                                            Map.entry("quot", "\""),
                                                                            Map.entry("amp", "&"),
                                                                            Map.entry("lt", "<"),
                                                                            Map.entry("gt", ">"),
                                                                            Map.entry("nbsp", " "),
                                                                            Map.entry("apos", "'"),
                                                                            Map.entry("mdash", "—"),
                                                                            Map.entry("ndash", "-"),
                                                                            Map.entry("hellip", "..."),
                                                                            Map.entry("rsquo", "'"),
                                                                            Map.entry("lsquo", "'"),
                                                                            Map.entry("rdquo", "\""),
                                                                            Map.entry("ldquo", "\""));

    private CommentHtmlText() {}

    static String toPlainText(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        var decoded = decodeEntities(text);
        var withoutTags = HTML_TAG.matcher(decoded).replaceAll(" ");
        return withoutTags.replaceAll("\\s+", " ").trim();
    }

    private static String decodeEntities(String text) {
        var decoded = text;
        var previous = "";
        while (!decoded.equals(previous)) {
            previous = decoded;
            decoded = decodeEntitiesOnce(decoded);
        }
        return decoded;
    }

    private static String decodeEntitiesOnce(String text) {
        var decoded = replaceMatches(NUMERIC_HEX, text, CommentHtmlText::decodeNumericHex);
        decoded = replaceMatches(NUMERIC_DECIMAL, decoded, CommentHtmlText::decodeNumericDecimal);
        return replaceMatches(NAMED_ENTITY, decoded, CommentHtmlText::decodeNamedEntity);
    }

    private static String replaceMatches(Pattern pattern, String text, java.util.function.Function<Matcher, String> replacer) {
        var matcher = pattern.matcher(text);
        var builder = new StringBuilder();
        while (matcher.find()) {
            matcher.appendReplacement(builder, Matcher.quoteReplacement(replacer.apply(matcher)));
        }
        matcher.appendTail(builder);
        return builder.toString();
    }

    private static String decodeNumericHex(Matcher matcher) {
        return decodeCodePoint(Integer.parseInt(matcher.group(1), 16));
    }

    private static String decodeNumericDecimal(Matcher matcher) {
        return decodeCodePoint(Integer.parseInt(matcher.group(1)));
    }

    private static String decodeCodePoint(int codePoint) {
        if (codePoint > 0 && codePoint <= Character.MAX_CODE_POINT) {
            return new String(Character.toChars(codePoint));
        }
        return " ";
    }

    private static String decodeNamedEntity(Matcher matcher) {
        var name = matcher.group(1);
        return NAMED_ENTITIES.getOrDefault(name, " ");
    }
}
