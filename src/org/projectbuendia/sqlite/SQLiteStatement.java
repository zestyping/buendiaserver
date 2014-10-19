package org.projectbuendia.sqlite;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Pim de Witte
 */
public abstract class SQLiteStatement implements SQLiteItem {
	private final String sql;
    private final long startTime;
    private final Object[] params;
    public boolean sleepRequest = true;


	protected SQLiteStatement(String sql, Object...params) {
		this.sql = sql;
        this.startTime = System.currentTimeMillis();
        this.params = params;
	}

    public final String getString(){
        return this.sql;
    }
    public final long getStartTime(){
        return this.startTime;
    }

	public abstract void execute(ResultSet result) throws SQLException;

    public final boolean canExecute() {
        return true;
    }

	@Override
	public final boolean execute(SqlDatabase database) throws SQLException {
		long startTime = System.currentTimeMillis();
		ResultSet result = database.query(sql, params);

		if ((System.currentTimeMillis() - startTime) >= 2000) {
			System.err.println("Query took: " + sql + ' ' + (System.currentTimeMillis() - startTime));
		}
		this.execute(result);
        sleepRequest = false;
		return true;
	}
}
