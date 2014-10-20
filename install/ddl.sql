CREATE TABLE IF NOT EXISTS flag_sub_types
(
flag_type_id INTEGER(6) DEFAULT 0 NOT NULL,
flag_subtype_id INTEGER(6) DEFAULT 0 NOT NULL,
name VARCHAR(25) NOT NULL)
;

CREATE TABLE IF NOT EXISTS flag_types
(flag_type_id INTEGER(6),
name VARCHAR(25) NOT NULL,
PRIMARY KEY (flag_type_id))
;

CREATE TABLE IF NOT EXISTS flags
(
patient_id VARCHAR(60) NOT NULL,
status VARCHAR(25) DEFAULT "not resolved",
created_timestamp timestamp default (strftime('%s', 'now')),
resolved_timestamp timestamp DEFAULT -1,
flag_type_id INTEGER(6) DEFAULT -1,
flag_subtype_id INTEGER(6) DEFAULT -1)
;

CREATE TABLE IF NOT EXISTS patients
(id VARCHAR(60) DEFAULT '0' NOT NULL,
created_timestamp timestamp default (strftime('%s', 'now')),
status VARCHAR(25) DEFAULT NULL,
given_name VARCHAR(60) DEFAULT NULL,
family_name VARCHAR(60) DEFAULT NULL,
assigned_location_zone_id INTEGER(6) DEFAULT -1,
assigned_location_tent_id NUMERIC(6) DEFAULT -1,
assigned_location_bed INTEGER(6) DEFAULT -1,
age_years INTEGER(4) DEFAULT -1,
age_months INTEGER(4) DEFAULT -1,
age_type VARCHAR(25) DEFAULT NULL,
age_certainty VARCHAR(25) DEFAULT NULL,
gender VARCHAR(8) DEFAULT NULL,
important_information VARCHAR(250) DEFAULT NULL,
pregnancy_start_timestamp timestamp DEFAULT NULL,
first_showed_symptoms_timestamp timestamp DEFAULT NULL,
movement VARCHAR(25) DEFAULT NULL,
eating VARCHAR(25) DEFAULT NULL,
origin_location VARCHAR(250) DEFAULT NULL,
next_of_kin VARCHAR(250) DEFAULT NULL,
PRIMARY KEY (id))
;

CREATE TABLE IF NOT EXISTS tents
(
zone_id INTEGER(6) NOT NULL,
tent_id INTEGER(6) NOT NULL,
name VARCHAR(25) NOT NULL,
capacity INTEGER(6) DEFAULT -1)
;

CREATE TABLE IF NOT EXISTS zones
(
zone_id INTEGER(6) NOT NULL,
name VARCHAR(25) NOT NULL,
capacity INTEGER(6) DEFAULT -1,
PRIMARY KEY (zone_id))
;

