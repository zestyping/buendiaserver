package org.projectbuendia.web.api.patients.post;

import org.projectbuendia.models.Patient;
import org.projectbuendia.server.Server;
import org.projectbuendia.sqlite.SqlDatabaseException;
import org.projectbuendia.utils.InvalidInputException;
import org.projectbuendia.utils.JsonUtils;
import org.projectbuendia.web.api.ApiInterface;
import org.projectbuendia.web.api.SharedFunctions;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
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
import java.util.regex.Pattern;

/**
 * Created by wwadewitte on 10/11/14.
 */
public class AddNewPatient implements ApiInterface {
    // All the fields in the patients table that can be set during an INSERT.
    static final HashSet INSERTABLE_COLUMNS = new HashSet(Arrays.asList(
        "status",
        "given_name",
        "family_name",
        "assigned_location_zone_id",
        "assigned_location_tent_id",
        "assigned_location_bed",
        "age_years",
        "age_months",
        "age_certainty",
        "age_type",
        "gender",
        "important_information",
        "pregnancy_start_timestamp",
        "first_showed_symptoms_timestamp",
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

        /* we can only execute this once at a time because we are generating the UID*/

        while(Server.isDoingPatient()) {
            try {
                System.out.println("Waiting;;;");
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        Server.setDoingPatient(true);


        int lastId = -1;

        // try-with-resources ensures that every ResultSet is closed.  Closing
        // ResultSets is very important; otherwise the database will be locked.
        try (ResultSet result = Server.getSqlDatabase().query(
            "SELECT id, count(1) FROM patients ORDER BY ROWID DESC LIMIT 1")) {
            if (result.next()) {
                if (result.getInt("count(1)") <= 0) {
                    System.out.println("Empty result, inserting first");
                    lastId = 0;
                } else {
                    System.out.println("ID IS:" + result.getString("id"));
                    lastId = Integer.parseInt(result.getString("id").split(Pattern.quote("."))[2]);
                    System.out.println("got last id" + lastId);
                }
            }
        } catch (SQLException e) {
            throw new SqlDatabaseException("Error getting results", e);
        }


        String newId = "MSF.TS." + (lastId + 1);
        System.out.println("newId = " + newId);

        List<String> fields = new ArrayList();
        List<String> values = new ArrayList();
        List<Object> args = new ArrayList();

        fields.add("id");
        values.add("?");
        args.add(newId);

        for (Map.Entry<String, JsonElement> entry: json.entrySet()) {
            String key = entry.getKey();
            Object value;
            try {
                value = JsonUtils.toStringOrLongOrNull(entry.getValue());
            } catch (InvalidInputException e) {
                // TODO(kpy): Let this exception return an HTTP 400.
                continue;
            }
            if (INSERTABLE_COLUMNS.contains(key)) {
                fields.add(key);
                values.add("?");
                args.add(value);
            }
        }

        String sql = "insert into patients " +
            "(" + Joiner.on(", ").join(fields) + ") " +
            "values (" + Joiner.on(", ").join(values) + ")";

        Server.getSqlDatabase().update(sql, args.toArray());

        Server.setDoingPatient(false);

        Patient patient = null;
        try (ResultSet result = Server.getSqlDatabase().query(
            "SELECT * FROM patients WHERE id = ?", newId)) {
            if (result.next()) {
                patient = Patient.fromResultSet(result);
            }
        } catch (SQLException e) {
            throw new SqlDatabaseException("Error getting results", e);
        }

        response.setStatus(HttpServletResponse.SC_OK);
        try {
            response.getWriter().write(new Gson().toJson(patient));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
