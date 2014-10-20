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
    public Long age_years;
    public String gender;
    public String assigned_location_zone;
    public String assigned_location_tent;
    public String assigned_location_bed;

    public static Patient fromResultSet(ResultSet rs) {
        Patient p = new Patient();
        p.id = getString(rs, "id");
        p.created_timestamp = getLong(rs, "created_timestamp");
        p.status = getString(rs, "status");
        p.given_name = getString(rs, "given_name");
        p.family_name = getString(rs, "family_name");
        p.age_years = getLong(rs, "age_years");
        p.gender = getString(rs, "gender");
        p.assigned_location_zone = getString(rs, "assigned_location_zone");
        p.assigned_location_tent = getString(rs, "assigned_location_tent");
        p.assigned_location_bed = getString(rs, "assigned_location_bed");
        return p;
    }
}
