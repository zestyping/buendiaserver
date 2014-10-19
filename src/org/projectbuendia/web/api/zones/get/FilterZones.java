package org.projectbuendia.web.api.zones.get;

import org.projectbuendia.server.Server;
import org.projectbuendia.sqlite.SQLiteQuery;
import org.projectbuendia.web.api.ApiInterface;
import org.projectbuendia.web.api.SharedFunctions;

import com.google.gson.JsonElement;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wwadewitte on 10/11/14.
 */
public class FilterZones implements ApiInterface {
    @Override
    public void call(final HttpServletRequest request, final HttpServletResponse response,final HashMap<String, String> urlVariables, final Map<String, String[]> parameterMap, final JsonElement json, final HashMap<String, String> payLoad){

        final String[] responseText = new String[]{null};


        SQLiteQuery checkQuery = new SQLiteQuery("SELECT * FROM `zones`") {

            @Override
            public void execute(ResultSet result) throws SQLException {
                StringBuilder s = new StringBuilder();
                while (result.next()) {
                    if(result.isFirst()) {
                        s.append(SharedFunctions.SpecificPatientResponse(result));
                    } else {
                        s.append("," + SharedFunctions.SpecificPatientResponse(result));
                    }
                }
                responseText[0] = s.toString();
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
            response.getWriter().write("[" + responseText[0] + "]");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
