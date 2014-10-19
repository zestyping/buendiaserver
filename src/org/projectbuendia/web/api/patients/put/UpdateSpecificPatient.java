package org.projectbuendia.web.api.patients.put;

import org.projectbuendia.server.Server;
import org.projectbuendia.sqlite.SQLiteStatement;
import org.projectbuendia.sqlite.SQLiteUpdate;
import org.projectbuendia.web.api.ApiInterface;
import org.projectbuendia.web.api.SharedFunctions;

import com.google.gson.JsonElement;
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
                     final JsonElement json,
                     final HashMap<String, String> payLoad) {

        final String[] responseText = new String[]{null};

        List<String> updates = new ArrayList();
        List<String> args = new ArrayList();

        for(String s : payLoad.keySet()) {
            if (UPDATABLE_COLUMNS.contains(s)) {
                updates.add("`"+s+"` = ?");
                args.add(payLoad.get(s));
            }
        }

        String updateQuery = "update `patients` set ";
        updateQuery += Joiner.on(", ").join(updates);
        updateQuery += " where `id` = ?";

        args.add(urlVariables.get("id"));

        Server.getLocalDatabase().executeUpdate(
            new SQLiteUpdate(updateQuery, args.toArray()));

        // Retrieve stored values
        List<String> args2 = new ArrayList();
        String checkQuery = "SELECT * FROM `patients` WHERE `id` = ?";
        args2.add(urlVariables.get("id"));

        Server.getLocalDatabase().executeStatement(
            new SQLiteStatement(checkQuery, args2.toArray()) {
                @Override
                public void execute(ResultSet result) throws SQLException {
                    while (result.next()) {
                        responseText[0] = SharedFunctions.SpecificPatientResponse(result);
                    }
                }
            }
        );

        while(responseText[0] == null) {
            // wait until the response is given
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        response.setStatus(HttpServletResponse.SC_OK);
        try {
            response.getWriter().write(responseText[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
