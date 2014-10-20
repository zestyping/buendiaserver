package org.projectbuendia.web.api.patients.get;

import org.projectbuendia.models.Patient;
import org.projectbuendia.server.Server;
import org.projectbuendia.sqlite.SqlDatabaseException;
import org.projectbuendia.web.api.ApiInterface;

import com.google.gson.Gson;
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
public class ShowPatientData implements ApiInterface {
    @Override
    public void call(final HttpServletRequest request, final HttpServletResponse response,final HashMap<String, String> urlVariables, final Map<String, String[]> parameterMap, final JsonObject json, final HashMap<String, String> payLoad){

        Patient patient = null;
        try (ResultSet result = Server.getSqlDatabase().query(
            "SELECT * FROM patients WHERE id = ?", urlVariables.get("id"))) {
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
