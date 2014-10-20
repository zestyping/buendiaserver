package org.projectbuendia.web.api.patients.get;

import org.projectbuendia.models.Patient;
import org.projectbuendia.server.Server;
import org.projectbuendia.sqlite.SqlDatabaseException;
import org.projectbuendia.web.api.ApiInterface;
import org.projectbuendia.web.api.SharedFunctions;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
                     final JsonObject json,
                     final HashMap<String, String> payLoad) {

        List<String> conditions = new ArrayList();
        String op = null;
        List<String> args = new ArrayList();

        // Gather all the filter conditions.
        // TODO(ping): Change the parameterMap to a Map<String, String>
        // instead of Map<String, String[]> so we don't have to [0] all over.
        final String[] searchParams = parameterMap.get("search");
        if (searchParams != null) {
            // In SQLite, || is the string concatenation operator.
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
        String sql = "select * from patients";
        if (!conditions.isEmpty()) {
            sql += " where " + Joiner.on(" " + op + " ").join(conditions);
        }
        String[] limitParams = parameterMap.get("limit");
        if (limitParams != null) {
            sql += " limit ?";
            args.add(limitParams[0]);
        }
        String[] offsetParams = parameterMap.get("offset");
        if (offsetParams != null) {
            sql += " offset ?";
            args.add(offsetParams[0]);
        }
        System.out.println("query: " + sql);
        System.out.println("with args: " + Joiner.on(", ").join(args));

        // Execute the query and collect all the results into a list.
        final List<Patient> patients = new ArrayList<Patient>();
        try (ResultSet result = Server.getSqlDatabase().query(
            sql, args.toArray())) {
            while (result.next()) {
                patients.add(Patient.fromResultSet(result));
            }
        } catch (SQLException e) {
            throw new SqlDatabaseException("Error getting results", e);
        }

        // Write out all the results as a JSON array.
        response.setStatus(HttpServletResponse.SC_OK);
        try {
            response.getWriter().write(new Gson().toJson(patients));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
