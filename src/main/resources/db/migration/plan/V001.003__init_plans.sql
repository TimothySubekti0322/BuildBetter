CREATE TABLE plans (
    id                  UUID PRIMARY KEY,
    user_id             UUID NOT NULL,
    province            VARCHAR(255) NOT NULL,
    city                VARCHAR(255) NOT NULL,
    land_form           VARCHAR(255) NOT NULL,
    land_area           INT          NOT NULL,
    entrance_direction  VARCHAR(255) NOT NULL,
    style               VARCHAR(255) NOT NULL,
    floor               INT          NOT NULL,
    rooms               INT          NOT NULL,
    suggestion_id       UUID NOT NULL,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT fk_plans_suggestion
        FOREIGN KEY (suggestion_id) REFERENCES suggestions(id)
);
