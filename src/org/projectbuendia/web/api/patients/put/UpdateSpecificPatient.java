package org.projectbuendia.web.api.patients.put;

import org.projectbuendia.models.Patient;
import org.projectbuendia.server.Server;
import org.projectbuendia.sqlite.SqlDatabaseException;
import org.projectbuendia.utils.InvalidInputException;
import org.projectbuendia.utils.JsonUtils;
import org.projectbuendia.web.api.ApiInterface;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
public class UpdateSpecificPatient implements ApiInterface {
    // All the fields in the patients table that can be set during an UPDATE.
    static final HashSet UPDATABLE_COLUMNS = new HashSet(Arrays.asList(
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

        List<String> updates = new ArrayList<String>();
        List<Object> args = new ArrayList<Object>();

        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
            String key = entry.getKey();
            Object value;
            try {
                value = JsonUtils.toStringOrLongOrNull(entry.getValue());
            } catch (InvalidInputException e) {
                // TODO(kpy): Let this exception return an HTTP 400.
                continue;
            }
            if (UPDATABLE_COLUMNS.contains(key)) {
                updates.add(key + " = ?");
                args.add(value);
            }
        }

        String updateQuery = "update `patients` set ";
        updateQuery += Joiner.on(", ").join(updates);
        updateQuery += " where `id` = ?";

        args.add(urlVariables.get("id"));

        Server.getSqlDatabase().update(updateQuery, args.toArray());

        // Retrieve stored values
        List<String> args2 = new ArrayList();
        String checkQuery = "SELECT * FROM `patients` WHERE `id` = ?";
        args2.add(urlVariables.get("id"));

        Patient patient = null;
        try (ResultSet result = Server.getSqlDatabase().query(
            checkQuery, args2.toArray())) {
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
