ALTER TABLE v1_delivra_service.users
    ADD COLUMN IF NOT EXISTS truck_gross_weight INTEGER,
    ADD COLUMN IF NOT EXISTS truck_height       INTEGER,
    ADD COLUMN IF NOT EXISTS truck_width        INTEGER,
    ADD COLUMN IF NOT EXISTS truck_length       INTEGER;
