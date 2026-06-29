package dev.vepo.engage.comments.wordcloud;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import dev.vepo.engage.model.Comment;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CommentWordCloudService {

    private static final int MIN_WORD_LENGTH = 3;
    private static final int MAX_WORDS = 50;
    private static final Pattern TOKEN_SPLIT = Pattern.compile("[^\\p{IsAlphabetic}]+");
    private static final Set<String> STOP_WORDS = Set.of(
                                                         "a",
                                                         "ao",
                                                         "aos",
                                                         "as",
                                                         "at",
                                                         "but",
                                                         "com",
                                                         "da",
                                                         "das",
                                                         "de",
                                                         "del",
                                                         "do",
                                                         "dos",
                                                         "e",
                                                         "el",
                                                         "ele",
                                                         "ella",
                                                         "em",
                                                         "en",
                                                         "era",
                                                         "essa",
                                                         "esse",
                                                         "esta",
                                                         "este",
                                                         "eu",
                                                         "for",
                                                         "from",
                                                         "ha",
                                                         "he",
                                                         "in",
                                                         "is",
                                                         "it",
                                                         "ja",
                                                         "la",
                                                         "las",
                                                         "le",
                                                         "lo",
                                                         "los",
                                                         "mais",
                                                         "mas",
                                                         "me",
                                                         "mesmo",
                                                         "muito",
                                                         "na",
                                                         "nas",
                                                         "no",
                                                         "nos",
                                                         "not",
                                                         "o",
                                                         "of",
                                                         "on",
                                                         "or",
                                                         "os",
                                                         "para",
                                                         "por",
                                                         "que",
                                                         "se",
                                                         "sem",
                                                         "she",
                                                         "so",
                                                         "som",
                                                         "sua",
                                                         "suas",
                                                         "tao",
                                                         "te",
                                                         "tem",
                                                         "that",
                                                         "the",
                                                         "their",
                                                         "they",
                                                         "this",
                                                         "to",
                                                         "tu",
                                                         "tua",
                                                         "um",
                                                         "uma",
                                                         "unas",
                                                         "uns",
                                                         "up",
                                                         "voce",
                                                         "was",
                                                         "we",
                                                         "with",
                                                         "you",
                                                         "your");

    public List<WordCloudEntry> buildFromComments(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            return List.of();
        }

        var frequencies = new HashMap<String, Long>();
        for (var comment : comments) {
            tokenize(comment.getText()).forEach(token -> frequencies.merge(token, 1L, Long::sum));
        }

        return frequencies.entrySet()
                          .stream()
                          .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder())
                                           .thenComparing(Map.Entry.comparingByKey()))
                          .limit(MAX_WORDS)
                          .map(entry -> new WordCloudEntry(entry.getKey(), entry.getValue()))
                          .toList();
    }

    private List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        var normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
                                   .replaceAll("\\p{M}+", "")
                                   .toLowerCase(Locale.ROOT);
        var tokens = new ArrayList<String>();
        for (var part : TOKEN_SPLIT.split(normalized)) {
            addToken(tokens, part);
        }
        return tokens;
    }

    private void addToken(List<String> tokens, String rawToken) {
        var token = rawToken.trim();
        if (token.length() < MIN_WORD_LENGTH || STOP_WORDS.contains(token)) {
            return;
        }
        tokens.add(token);
    }
}
