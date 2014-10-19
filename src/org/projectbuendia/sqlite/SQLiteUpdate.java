package org.projectbuendia.sqlite;

/**
 * @author Pim de Witte
 */
public final class SQLiteUpdate implements SQLiteItem {
    private final String sql;
    private final Object[] params;

    public SQLiteUpdate(String sql, Object...params) {
        this.sql = sql;
        this.params = params;
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public final boolean execute(SqlDatabase database) {
        long startTime = System.currentTimeMillis();
        int result = database.update(sql, params);
        if ((System.currentTimeMillis() - startTime) >= 5000) {
            System.err.println("Update took: " + (System.currentTimeMillis() - startTime));
        }
        if (result == -1) {
            return false;
        }
        if (result == -2) {
            return true;
        }
        return true;
    }
}
