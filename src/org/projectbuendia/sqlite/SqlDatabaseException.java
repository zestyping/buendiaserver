package org.projectbuendia.sqlite;

class SqlDatabaseException extends RuntimeException {
    public SqlDatabaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
