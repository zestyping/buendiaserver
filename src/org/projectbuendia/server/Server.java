package org.projectbuendia.server;

import com.mongodb.*;
import org.projectbuendia.config.Config;
import org.projectbuendia.config.DatabaseConfigs;
import org.projectbuendia.config.ServerProperties;
import org.projectbuendia.fileops.FileChecks;
import org.projectbuendia.fileops.Logging;
import org.projectbuendia.logic.BackupThread;
import org.projectbuendia.logic.LogicThread;
import org.projectbuendia.mongodb.MongoConnectionProcessor;
import org.projectbuendia.mongodb.MongoQuery;
import org.projectbuendia.sqlite.SQLiteConnectionProcessor;
import org.projectbuendia.sqlite.SQLiteConnection;
import org.projectbuendia.sqlite.SqlDatabase;
import org.projectbuendia.web.JettyServer;

import java.io.*;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.TimeZone;


/**
 * Created by wwadewitte on 10/1/14.
 */
public final class Server {

    public static boolean doingPatient = false;

    public static boolean isDoingPatient() {
        return doingPatient;
    }
    public static void setDoingPatient(boolean value) {
        doingPatient = value;
    }

    private static ServerProperties systemProperties;
    public static ServerProperties getServerProperties() {
        return systemProperties;
    }
    private static Calendar calendar = new GregorianCalendar();
    private static SqlDatabase db = null;
    public static SqlDatabase getSqlDatabase() {
        return db;
    }
    private static SQLiteConnectionProcessor localDatabase = null;
    public static SQLiteConnectionProcessor getLocalDatabase() {
        return localDatabase;
    }

    private static MongoConnectionProcessor mongoDatabase;
    public static MongoConnectionProcessor getMongoDatabase() {
        return mongoDatabase;
    }
    private static final LogicThread logic = new LogicThread();
    private static final BackupThread backups = new BackupThread();

    public static Calendar getCalendar() {
        return calendar;
    }

    public static final void main(String[] args) throws InterruptedException {

        /*
        Initialize logging and system properties
         */

        systemProperties = new ServerProperties(Config.CONFIGURATION_FILE);
        ServerProperties.read();
        try {
            System.setErr(new PrintStream(new Logging.ErrorFile(), true));
        }catch (Exception e) {
            System.out.println("Error was thrown with config.prop config. Switching to errors/");
            Logging.log("SEVERE", e);
            try {
                System.setErr(new PrintStream(new Logging.ErrorFile("errors/"), true));
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
                System.out.println("Closing error logging stream");
                // close the stream so it is no longer logged anywhere
                System.err.close();
            }
        }
        calendar.setTimeZone(TimeZone.getTimeZone(Config.SERVER_TIMEZONE));
        Logging.log("RESTART","--------------------------------------------------------------------");



        /*

        START SQLITE

         */
        long start = System.currentTimeMillis();
        db = SqlDatabase.openSqliteFile(Config.SQLITE_PATH);
        long end = System.currentTimeMillis();

        Logging.log("INFO", Config.SQLITE_PATH + " (sqlite) took " +
                    (end - start) + " ms to connect");

        // Split ddl.sql into statements and execute each one (executeUpdate
        // can only execute one statement at a time).
        String schema = "";
        try {
            schema = FileChecks.readFile("install/ddl.sql");
        } catch (IOException ignored) { }
        for (String sql : schema.split(";\\s*\n")) {
            db.update(sql);
        }

        /*

        Start mongodb if it's enabled and get a result to test

         */

        if(Config.ENABLE_MONGOPUSH) {
            start = System.currentTimeMillis();
            System.out.println("Putting thread to sleep until mongodb is connected..");
            try {
                mongoDatabase = new MongoConnectionProcessor(new MongoClient(Config.MONGODB_HOST), Config.MONGODB_DB);
            } catch (UnknownHostException e) {
                Logging.log("MongoServer problem", e);
            }
            mongoDatabase.start();

            while (!mongoDatabase.isConnected()) {
                Thread.sleep(1);
            }

            end = System.currentTimeMillis();

            Logging.log("INFO", Config.MONGODB_DB + "@" + Config.MONGODB_HOST + " (mongo) took " + (end - start) + " ms to connect");
            BasicDBObject query = new BasicDBObject("i", 71);

            mongoDatabase.executeQuery(new MongoQuery("patients", query) {
                @Override
                public void execute(DBCursor result) throws MongoException {
                    try {
                        while (result.hasNext()) {
                            System.out.println(result.next());
                        }
                    } finally {
                        result.close();
                    }
                }
            });

        }

        /* We load the all the database configs */

        DatabaseConfigs.getDatabaseConfigs();




        /*
        We start the logic thread
         */

        logic.start();

        /*
        We start the backup thread
         */
        backups.start();


        /*
        finally, we start the web server
         */

        try {
            JettyServer.start();
        } catch (Exception e) {
            Logging.log("Jetty problem", e);
        } finally {
            Logging.log("INFO", "The server was successfully restarted");
        }

    }

}
