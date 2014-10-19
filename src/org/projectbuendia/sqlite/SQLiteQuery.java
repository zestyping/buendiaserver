package org.projectbuendia.sqlite;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Pim de Witte
 */
public abstract class SQLiteQuery implements SQLiteItem {
	private final String sql;
    private final long startTime;
    public boolean sleepRequest = true;
    

	protected SQLiteQuery(String sql) {
		this.sql = sql;
        this.startTime = System.currentTimeMillis();
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
		ResultSet result = database.query(sql);

		if ((System.currentTimeMillis() - startTime) >= 2000) {
			System.err.println("Query took: " + sql + ' ' + (System.currentTimeMillis() - startTime));
		}
        if(result == null) {
            System.err.println(sql + " was null");
            sleepRequest = false;
            return true;
        }
		this.execute(result);
        sleepRequest = false;
		return true;
	}
}
