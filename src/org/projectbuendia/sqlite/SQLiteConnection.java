package org.projectbuendia.sqlite;

import org.projectbuendia.fileops.Logging;

import java.sql.*;

/**
 * @author Pim de Witte
 */
public final class SQLiteConnection {

    private final String url;
    private Connection connection;
    private Statement statement;
    private boolean connected;

    public SQLiteConnection(String database) {
        this.url = "jdbc:sqlite:"+database;
    }

    public boolean isConnected() {
        if (!connected) {
            return false;
        }
        try {
            return connected = !connection.isClosed();
        } catch (SQLException e) {
            Logging.log("SQLite connection problem", e);
            return connected = false;
        }
    }

    public boolean statementConnected() {
        try {
            return !statement.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean close() {
        if (this.isConnected()) {
            return false;
        }
        try {
            connection.close();
            statement.close();
            connected = false;
            return true;
        } catch (SQLException e) {
            return false;
        }
    }


    public boolean connect() {
        if (connected) {
            throw new IllegalStateException("SQL is already connected.");
        }
        try {
            connection = DriverManager.getConnection(url);
            statement = connection.createStatement();
            System.out.println("Connected to SQLITE");
            return connected = true;
        } catch (SQLException e) {
            //Server.getLogger().log(Level.SEVERE, e.getMessage());
            Logging.log("SQLite problem", e);
            return false;
        }
    }

    public ResultSet executeQuery(String sql) {
        return this.executeQueryInternal(sql);
    }


    public ResultSet executeStatement(String sql, Object...params) {
        return this.executeStatementInternal(sql, true, params);
    }

    public ResultSet executeQueryInternal(String sql) {
        if (!this.isConnected()) {
            return null;
        }
        try {
            return statement.executeQuery(sql);
        } catch (SQLException e) {
            return null;
        }
    }
    public ResultSet executeStatementInternal(String sql,boolean retry, Object...params) {
        if (!this.isConnected()) {
            return null;
        }
        PreparedStatement st;
        try {
            st = connection.prepareStatement(sql);
            int i = 0;
            for(Object o : params) {
                i++;
                st.setObject(i, o);
            }
            return st.executeQuery();
        } catch (SQLException e) {
            if (retry) {
                Logging.log("OWE", e);
                return this.executeStatementInternal(sql, false, params);
            }
            return null;
        }
    }

    public int executeUpdate(String sql, Object...params) {
        return this.executeUpdateInternal(sql, params);
    }

    public int executeUpdateInternal(String sql, Object...params) {
        if (!this.isConnected()) {
            return -1;
        }
        PreparedStatement st;
        try {
            st = connection.prepareStatement(sql);
            int i = 0;
            for(Object o : params) {
                i++;
                st.setObject(i, o);
            }
            /*if (statement.isClosed()) {
                Logging.log("SEVERE", sql + "statement was closed");
                statement = connection.createStatement();
            }*/
            return st.executeUpdate();
        } catch (SQLException e) {
            return -2;
        }
    }
}
