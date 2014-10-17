package org.projectbuendia.models;

import java.sql.ResultSet;

public class Patient extends Model {
    // We use Long instead of long in our models so that a value of null can
    // be used to represent a missing field.  Gson automatically serializes a
    // field with a value of null by omitting the field.
    public String id;
    public Long created_timestamp;
    public String status;
    public String given_name;
    public String family_name;
    public Long assigned_location_zone_id;
    public Long assigned_location_tent_id;
    public Long assigned_location_bed;
    public Long age_years;
    public Long age_months;
    public String age_type;
    public String age_certainty;
    public String gender;
    public String important_information;
    public Long pregnancy_start_timestamp;
    public Long first_showed_symptoms_timestamp;
    public String movement;
    public String eating;
    public String origin_location;
    public String next_of_kin;

    public static Patient fromResultSet(ResultSet rs) {
        Patient p = new Patient();
        p.id = getString(rs, "id");
        p.created_timestamp = getLong(rs, "created_timestamp");
        p.status = getString(rs, "status");
        p.given_name = getString(rs, "given_name");
        p.family_name = getString(rs, "family_name");
        p.assigned_location_zone_id = getLong(rs, "assigned_location_zone_id");
        p.assigned_location_tent_id = getLong(rs, "assigned_location_tent_id");
        p.assigned_location_bed = getLong(rs, "assigned_location_bed");
        p.age_years = getLong(rs, "age_years");
        p.age_months = getLong(rs, "age_months");
        p.age_type = getString(rs, "age_type");
        p.age_certainty = getString(rs, "age_certainty");
        p.gender = getString(rs, "gender");
        p.important_information = getString(rs, "important_information");
        p.pregnancy_start_timestamp = getLong(rs, "pregnancy_start_timestamp");
        p.first_showed_symptoms_timestamp =
            getLong(rs, "first_showed_symptoms_timestamp");
        p.movement = getString(rs, "movement");
        p.eating = getString(rs, "eating");
        p.origin_location = getString(rs, "origin_location");
        p.next_of_kin = getString(rs, "next_of_kin");
        return p;
    }
}
