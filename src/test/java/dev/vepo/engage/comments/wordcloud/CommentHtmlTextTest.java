package dev.vepo.engage.comments.wordcloud;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CommentHtmlTextTest {

    @Test
    void shouldDecodeQuotedYoutubeCommentText() {
        assertThat(CommentHtmlText.toPlainText("&quot;video sobre pra sao victor&quot;"))
                                                                                         .isEqualTo("\"video sobre pra sao victor\"");
    }

    @Test
    void shouldDecodeDoubleEncodedEntities() {
        assertThat(CommentHtmlText.toPlainText("&amp;quot;teste&amp;quot;"))
                                                                            .isEqualTo("\"teste\"");
    }

    @Test
    void shouldStripTagsAndDecodeEntities() {
        assertThat(CommentHtmlText.toPlainText("<b>&quot;otimo&quot;</b> canal"))
                                                                                 .isEqualTo("\"otimo\" canal");
    }
}
