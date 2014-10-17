package org.projectbuendia.models;

import java.sql.ResultSet;
import java.sql.SQLException;

/** Base class for models. */
class Model {
    /** Converts a SQL string value to a String object. */
    static String getString(ResultSet rs, String column) {
        try {
            String value = rs.getString(column);
            if (value == null) return null;
            // Missing string values are stored in the database as "".
            return value.isEmpty() ? null : value;
        } catch (SQLException e) {
            return null;
        }
    }

    /** Converts a SQL integer value to a Long object. */
    static Long getLong(ResultSet rs, String column) {
        try {
            Long value = rs.getLong(column);
            if (value == null) return null;
            // Missing integer values are stored in the database as -1.
            return (value == -1) ? null : value;
        } catch (SQLException e) {
            return null;
        }
    }
}
