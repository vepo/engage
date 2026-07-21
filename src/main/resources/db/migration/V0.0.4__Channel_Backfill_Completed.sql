ALTER TABLE tb_channels
    ADD COLUMN backfill_completed BOOLEAN NOT NULL DEFAULT FALSE;
