package org.projectbuendia.web.api.patients.get;

import org.projectbuendia.server.Server;
import org.projectbuendia.sqlite.SQLiteStatement;
import org.projectbuendia.web.api.ApiInterface;
import org.projectbuendia.web.api.SharedFunctions;

import com.google.common.base.Joiner;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;

/**
 * Created by wwadewitte on 10/11/14.
 */
public class FilterPatients implements ApiInterface {
    // All the string fields in the patients table.
    static final HashSet FILTERABLE_COLUMNS = new HashSet(Arrays.asList(
        "id",
        "rfid_ids",
        "bluetooth_id",
        "status",
        "given_name",
        "family_name",
        "age_certainty",
        "gender",
        "movement",
        "eating",
        "origin_location",
        "next_of_kin"
    ));

    @Override
    public void call(final HttpServletRequest request,
                     final HttpServletResponse response,
                     final HashMap<String, String> urlVariables,
                     final Map<String, String[]> parameterMap,
                     final HashMap<String, String> payLoad) {

        List<String> conditions = new ArrayList();
        String op = null;
        List<String> args = new ArrayList();

        // Gather all the filter conditions.
        // TODO(ping): Change the parameterMap to a Map<String, String>
        // instead of Map<String, String[]> so we don't have to [0] all over.
        final String[] searchParams = parameterMap.get("search");
        if (searchParams != null) {
            conditions.add("id like '%' || ? || '%'");
            args.add(searchParams[0]);
            conditions.add("given_name like '%' || ? || '%'");
            args.add(searchParams[0]);
            conditions.add("family_name like '%' || ? || '%'");
            args.add(searchParams[0]);
            op = "or";
        } else {
            for (String s : parameterMap.keySet()) {
                // Only accept valid column names.
                if (FILTERABLE_COLUMNS.contains(s)) {
                    conditions.add(s + " = ?");
                    args.add(parameterMap.get(s)[0]);
                }
            }
            op = "and";
        }

        // Construct the SQL query.
        String query = "select * from patients";
        if (!conditions.isEmpty()) {
            query += " where " + Joiner.on(" " + op + " ").join(conditions);
        }
        String[] limitParams = parameterMap.get("limit");
        if (limitParams != null) {
            query += " limit ?";
            args.add(limitParams[0]);
        }
        String[] offsetParams = parameterMap.get("offset");
        if (offsetParams != null) {
            query += " offset ?";
            args.add(offsetParams[0]);
        }
        System.out.println("query: " + query);
        System.out.println("with args: " + Joiner.on(", ").join(args));

        // Execute the query and collect all the results into a list.
        final List<String> jsonResults = new ArrayList();
        final boolean[] finished = {false};
        Server.getLocalDatabase().executeStatement(
            new SQLiteStatement(query, args.toArray()) {
            @Override
            public void execute(ResultSet result) throws SQLException {
                while (result.next()) {
                    jsonResults.add(
                        SharedFunctions.SpecificPatientResponse(result));
                }
                finished[0] = true;
            }
        });

        // Wait until the query is done.
        // TODO(ping): If an exception occurs while processing the query,
        // execute() is never called and the server hangs forever.  We should
        // issue the query directly to SQLite instead of sending it to the
        // connection processor and spin-waiting here.
        while (!finished[0]) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Write out all the results as a JSON array.
        response.setStatus(HttpServletResponse.SC_OK);
        try {
            response.getWriter().write(
                "[" + Joiner.on(", ").join(jsonResults) + "]");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
