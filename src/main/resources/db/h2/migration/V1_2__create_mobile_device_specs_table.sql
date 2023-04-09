CREATE TABLE IF NOT EXISTS mobile_device_specs
(
    ID         VARCHAR          DEFAULT CONCAT('msd_', TO_CHAR(RANDOM_UUID())) PRIMARY KEY,
    model_code VARCHAR NOT NULL,
    source     VARCHAR NOT NULL,
    synced     boolean NOT NULL DEFAULT false,
    technology VARCHAR,
    bands2g    VARCHAR,
    bands3g    VARCHAR,
    bands4g    VARCHAR
);

CREATE INDEX IF NOT EXISTS mobile_device_specs_model_code_ndx ON mobile_device_specs (model_code);