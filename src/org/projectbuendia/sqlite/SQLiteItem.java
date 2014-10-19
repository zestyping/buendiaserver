package org.projectbuendia.sqlite;

import java.sql.SQLException;

/**
 * @author Pim de Witte
 */
public interface SQLiteItem {
	boolean canExecute();
	boolean execute(SqlDatabase database) throws SQLException;
}
