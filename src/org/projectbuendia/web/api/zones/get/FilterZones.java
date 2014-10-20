package org.projectbuendia.web.api.zones.get;

import org.projectbuendia.server.Server;
import org.projectbuendia.sqlite.SqlDatabaseException;
import org.projectbuendia.web.api.ApiInterface;
import org.projectbuendia.web.api.SharedFunctions;

import com.google.gson.JsonObject;
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
    public void call(final HttpServletRequest request, final HttpServletResponse response,final HashMap<String, String> urlVariables, final Map<String, String[]> parameterMap, final JsonObject json, final HashMap<String, String> payLoad){

        StringBuilder s = new StringBuilder();
        try (ResultSet result = Server.getSqlDatabase().query(
            "SELECT * FROM zones")) {
            while (result.next()) {
                if (result.isFirst()) {
                    s.append(SharedFunctions.SpecificPatientResponse(result));
                } else {
                    s.append("," + SharedFunctions.SpecificPatientResponse(result));
                }
            }
        } catch (SQLException e) {
            throw new SqlDatabaseException("Error getting results", e);
        }

        response.setStatus(HttpServletResponse.SC_OK);
        try {
            response.getWriter().write("[" + s.toString() + "]");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
