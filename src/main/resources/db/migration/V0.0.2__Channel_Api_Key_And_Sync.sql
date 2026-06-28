ALTER TABLE tb_channels
    ADD COLUMN youtube_api_key VARCHAR(255),
    ADD COLUMN connected       BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE tb_videos
    ADD COLUMN comments_sync_at TIMESTAMP(6) WITH TIME ZONE,
    ADD COLUMN comments_next_page_token VARCHAR(255);

CREATE INDEX idx_videos_comments_sync_at ON tb_videos (comments_sync_at);
