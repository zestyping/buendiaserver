package org.projectbuendia.sqlite;


import org.projectbuendia.fileops.Logging;

import java.sql.SQLException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author Pim de Witte
 */
public final class SQLiteConnectionProcessor implements Runnable {

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    private final SqlDatabase database;
    private final Thread thread;
    private boolean running;
    private final Queue<SQLiteItem> items = new ConcurrentLinkedQueue<SQLiteItem>();
    private final Object lock = new Object();


    public SQLiteConnectionProcessor(SqlDatabase database) {
        this.thread = new Thread(this);
        this.database = database;
    }


    public void start() {
        if (running) {
            throw new IllegalStateException("The processor is already running.");
        }
        thread.start();
    }

    public void stop() {
        if (!running) {
            throw new IllegalStateException("The processor is already stopped.");
        }
        running = false;
    }

    @Override
    public void run() {

        running = true;
        MainLoop:
        while (running) {
            while (!items.isEmpty()) {
                SQLiteItem item = items.peek();
                if (!item.canExecute()) {
                    items.remove();
                    continue;
                }
                try {
                    if (!item.execute(database)) {
                        continue MainLoop;
                    }
                } catch (SQLException e) {
                    Logging.log("SQL error", e);
                } catch (SqlDatabaseException e) {
                    Logging.log("SQL error", e);
                }
                items.remove();
            }
            synchronized (this) {
                try {
                    this.wait();
                } catch (InterruptedException ignored) {
                }
            }
        }
        database.close();
    }

    public void waitForPendingPackets() {
        while (!items.isEmpty()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public boolean executeStatement(SQLiteStatement statement) {
        boolean result = items.offer(statement);
        synchronized (this) {
            this.notify();
        }
        return result;
    }

    public boolean executeQuery(SQLiteQuery query) {
        boolean result = items.offer(query);
        synchronized (this) {
            this.notify();
        }
        return result;
    }

    public boolean forceQuery(SQLiteQuery query) {
        boolean result = items.offer(query);
        synchronized (this) {
            this.notify();
        }
        return result;
    }

    public boolean forceUpdate(SQLiteUpdate update) {
        boolean result = items.offer(update);
        return update.execute(database);
    }

    public boolean executeUpdate(SQLiteUpdate update) {
        boolean result = items.offer(update);
        synchronized (this) {
            this.notify();
        }
        return result;
    }

    public boolean blockingQuery(SQLiteQuery update) throws SQLException {
        synchronized (lock) {
            return update.execute(database);
        }
    }
}
