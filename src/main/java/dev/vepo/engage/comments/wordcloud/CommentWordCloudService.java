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
    private static final Pattern HTML_TAG = Pattern.compile("<[^>]+>");
    private static final Set<String> STOP_WORDS = Set.of(
                                                         "a",
                                                         "ao",
                                                         "aos",
                                                         "aquela",
                                                         "aquelas",
                                                         "aquele",
                                                         "aqueles",
                                                         "aquilo",
                                                         "as",
                                                         "ate",
                                                         "com",
                                                         "como",
                                                         "contra",
                                                         "da",
                                                         "das",
                                                         "de",
                                                         "dela",
                                                         "delas",
                                                         "dele",
                                                         "deles",
                                                         "depois",
                                                         "dessa",
                                                         "desse",
                                                         "desta",
                                                         "deste",
                                                         "disso",
                                                         "disto",
                                                         "do",
                                                         "dos",
                                                         "e",
                                                         "ela",
                                                         "elas",
                                                         "ele",
                                                         "eles",
                                                         "em",
                                                         "entre",
                                                         "era",
                                                         "eram",
                                                         "essa",
                                                         "essas",
                                                         "esse",
                                                         "esses",
                                                         "esta",
                                                         "estao",
                                                         "estas",
                                                         "este",
                                                         "estes",
                                                         "eu",
                                                         "foi",
                                                         "for",
                                                         "foram",
                                                         "ha",
                                                         "havia",
                                                         "isso",
                                                         "isto",
                                                         "ja",
                                                         "lhe",
                                                         "lhes",
                                                         "mais",
                                                         "mas",
                                                         "me",
                                                         "mesmo",
                                                         "meu",
                                                         "meus",
                                                         "minha",
                                                         "minhas",
                                                         "muito",
                                                         "na",
                                                         "nas",
                                                         "nao",
                                                         "nos",
                                                         "nossa",
                                                         "nossas",
                                                         "nosso",
                                                         "nossos",
                                                         "num",
                                                         "numa",
                                                         "o",
                                                         "os",
                                                         "ou",
                                                         "para",
                                                         "pela",
                                                         "pelas",
                                                         "pelo",
                                                         "pelos",
                                                         "por",
                                                         "qual",
                                                         "quando",
                                                         "que",
                                                         "quem",
                                                         "se",
                                                         "sem",
                                                         "ser",
                                                         "seu",
                                                         "seus",
                                                         "so",
                                                         "sua",
                                                         "suas",
                                                         "tambem",
                                                         "te",
                                                         "tem",
                                                         "temos",
                                                         "ter",
                                                         "teu",
                                                         "teus",
                                                         "tua",
                                                         "tuas",
                                                         "um",
                                                         "uma",
                                                         "umas",
                                                         "uns",
                                                         "voce",
                                                         "voces");

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

        var withoutHtml = HTML_TAG.matcher(text).replaceAll(" ");
        var normalized = Normalizer.normalize(withoutHtml, Normalizer.Form.NFD)
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
