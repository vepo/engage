package dev.vepo.engage.comments.wordcloud;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.vepo.engage.model.Comment;

class CommentWordCloudServiceTest {

    private final CommentWordCloudService service = new CommentWordCloudService();

    @Test
    void shouldReturnEmptyWordCloudWhenNoComments() {
        assertThat(service.buildFromComments(List.of())).isEmpty();
    }

    @Test
    void shouldCountRepeatedWordsAndIgnoreStopWords() {
        var first = commentWithText("Engage é excelente! Muito bom engage.");
        var second = commentWithText("Engage funciona muito bem.");

        var wordCloud = service.buildFromComments(List.of(first, second));

        assertThat(wordCloud).isNotEmpty();
        assertThat(wordCloud.getFirst().word()).isEqualTo("engage");
        assertThat(wordCloud.getFirst().count()).isEqualTo(3);
        assertThat(wordCloud.stream().map(WordCloudEntry::word)).doesNotContain("muito", "e");
    }

    @Test
    void shouldNormalizeAccentsAndCase() {
        var comment = commentWithText("Coração coracao CORAÇÃO");

        var wordCloud = service.buildFromComments(List.of(comment));

        assertThat(wordCloud).hasSize(1);
        assertThat(wordCloud.getFirst().word()).isEqualTo("coracao");
        assertThat(wordCloud.getFirst().count()).isEqualTo(3);
    }

    @Test
    void shouldStripHtmlTagsBeforeTokenizing() {
        var comment = commentWithText("<a href=\"https://example.com\">link</a> excelente conteudo");

        var wordCloud = service.buildFromComments(List.of(comment));

        assertThat(wordCloud.stream().map(WordCloudEntry::word)).contains("excelente", "conteudo");
        assertThat(wordCloud.stream().map(WordCloudEntry::word)).doesNotContain("href", "https", "example", "com");
    }

    @Test
    void shouldDecodeHtmlEntitiesBeforeTokenizing() {
        var comment = commentWithText("&quot;video sobre pra sao victor&quot; bom conteudo");

        var wordCloud = service.buildFromComments(List.of(comment));

        assertThat(wordCloud.stream().map(WordCloudEntry::word)).contains("video", "sobre", "victor", "conteudo");
        assertThat(wordCloud.stream().map(WordCloudEntry::word)).doesNotContain("quot", "amp", "nbsp");
    }

    @Test
    void shouldDecodeNumericHtmlEntities() {
        var comment = commentWithText("&#39;otimo&#39; canal");

        var wordCloud = service.buildFromComments(List.of(comment));

        assertThat(wordCloud.stream().map(WordCloudEntry::word)).contains("otimo", "canal");
    }

    @Test
    void shouldIgnoreEnglishStopWordsNotInPortugueseList() {
        var comment = commentWithText("the quick brown fox");

        var wordCloud = service.buildFromComments(List.of(comment));

        assertThat(wordCloud.stream().map(WordCloudEntry::word)).contains("quick", "brown");
    }

    private Comment commentWithText(String text) {
        var comment = new Comment();
        comment.setText(text);
        return comment;
    }
}
