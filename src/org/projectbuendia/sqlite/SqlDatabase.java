package org.projectbuendia.sqlite;

import org.projectbuendia.fileops.Logging;

import java.sql.*;

/**
 * Provides query and update access to a SQL database.  All SQLExceptions
 * are treated as unrecoverable and converted to unchecked exceptions.
 * @author Pim de Witte
 */
public final class SqlDatabase {
    private final String url;
    private Connection connection = null;

    public SqlDatabase(String url) {
        this.url = url;
        try {
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new SqlDatabaseException("SQL connection error", e);
        }
    }

    public static SqlDatabase openSqliteFile(String filename) {
        return new SqlDatabase("jdbc:sqlite:" + filename);
    }

    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new SqlDatabaseException("SQL close() error", e);
            }
        }
        connection = null;
    }

    public ResultSet query(String sql) {
        try {
            Statement statement = connection.createStatement();
            try {
                return statement.executeQuery(sql);
            } finally {
                statement.close();
            }
        } catch (SQLException e) {
            throw new SqlDatabaseException("SQL query error", e);
        }
    }

    public ResultSet query(String sql, Object... params) {
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            try {
                int i = 0;
                for (Object param : params) {
                    i++;
                    statement.setObject(i, param);
                }
                return statement.executeQuery();
            } finally {
                statement.close();
            }
        } catch (SQLException e) {
            throw new SqlDatabaseException("SQL query error", e);
        }
    }

    public int update(String sql) {
        try {
            Statement statement = connection.createStatement();
            try {
                return statement.executeUpdate(sql);
            } finally {
                statement.close();
            }
        } catch (SQLException e) {
            throw new SqlDatabaseException("SQL update error", e);
        }
    }

    public int update(String sql, Object... params) {
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            try {
                int i = 0;
                for (Object param : params) {
                    i++;
                    statement.setObject(i, param);
                }
                return statement.executeUpdate();
            } finally {
                statement.close();
            }
        } catch (SQLException e) {
            throw new SqlDatabaseException("SQL update error", e);
        }
    }
}
