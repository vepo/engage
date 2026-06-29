ALTER TABLE tb_channels
    ADD COLUMN uploads_playlist_id VARCHAR(50);

-- Search pagination tokens are not valid for uploads playlist backfill.
UPDATE tb_channels SET next_page_token = NULL;
