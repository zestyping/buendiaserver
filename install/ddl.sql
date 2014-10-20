CREATE TABLE IF NOT EXISTS patients (
    id varchar(100),
    created_timestamp integer default (strftime('%s', 'now')),
    status varchar(100),
    given_name varchar(100),
    family_name varchar(100),
    age_years integer,
    gender varchar(100),
    assigned_location_zone varchar(100),
    assigned_location_tent varchar(100),
    assigned_location_bed varchar(100),

    PRIMARY KEY (id)
);
