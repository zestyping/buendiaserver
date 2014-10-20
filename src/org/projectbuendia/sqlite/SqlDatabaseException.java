package org.projectbuendia.sqlite;

public class SqlDatabaseException extends RuntimeException {
    public SqlDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
