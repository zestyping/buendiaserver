package org.projectbuendia.web.api.patients.post;

import org.projectbuendia.server.Server;
import org.projectbuendia.sqlite.SQLiteQuery;
import org.projectbuendia.sqlite.SQLiteUpdate;
import org.projectbuendia.web.api.ApiInterface;
import org.projectbuendia.web.api.SharedFunctions;

import com.google.common.base.Joiner;
import com.google.gson.JsonElement;
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
                     final JsonElement json,
                     final HashMap<String, String> payLoad) {

        final String[] responseText = new String[]{null};
        final int[] lastId = new int[]{-1};

        SQLiteQuery query = new SQLiteQuery("SELECT `id`, count(1) FROM `patients` ORDER BY ROWID DESC LIMIT 1") {

            @Override
            public void execute(ResultSet result) throws SQLException {
                if(result == null) {

                } else {
                    while (result.next()) {
                        if(result.getInt("count(1)") <= 0) {
                            System.out.println("Empty result, inserting first");
                            lastId[0] = 0;
                            return;
                        }
                        System.out.println("ID IS:" + result.getString("id"));
                        lastId[0] = Integer.parseInt(result.getString("id").split(Pattern.quote("."))[2]);
                        System.out.println("got last id" + lastId[0]);
                    }
                }
            }
        };

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

        Server.getLocalDatabase().executeQuery(query);

        while(lastId[0] == -1) {
            try {
               /// System.out.println("Waiting.....");
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            // waiting for that query to clear considering the sensitive process
        }

        int newId = lastId[0] + 1;

        List<String> fields = new ArrayList();
        List<String> values = new ArrayList();
        List<String> args = new ArrayList();

        fields.add("`id`");
        values.add("?");
        args.add("MSF.TS."+ newId);

        for(String s : payLoad.keySet()) {
            if (INSERTABLE_COLUMNS.contains(s)) {
                fields.add("`"+s+"`");
                values.add("?");
                args.add(payLoad.get(s));
            }
        }

        String insertQuery = "insert into `patients`";
        insertQuery += " (" + Joiner.on(", ").join(fields) + ")";
        insertQuery += " values (" + Joiner.on(", ").join(values) + ")";

        Server.getLocalDatabase().executeUpdate(
            new SQLiteUpdate(insertQuery, args.toArray()));

        Server.setDoingPatient(false);

        SQLiteQuery checkQuery = new SQLiteQuery("SELECT * FROM `patients` WHERE `id` = 'MSF.TS."+newId+"'  ") {

            @Override
            public void execute(ResultSet result) throws SQLException {
                while (result.next()) {
                    responseText[0] = SharedFunctions.SpecificPatientResponse(result);
                }
            }
        };

        Server.getLocalDatabase().executeQuery(checkQuery);

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
