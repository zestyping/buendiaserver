package org.projectbuendia.web.api;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

/**
 * Created by wwadewitte on 10/11/14.
 */
public class SharedFunctions {

    public static String SpecificPatientResponse(ResultSet result) throws SQLException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return "{" +
                "\"id\":" + (result.getString("id") != null ? "\""+result.getString("id")+"\"" :  "null") +","+
                "\"created_timestamp_utc\":" + result.getLong("created_timestamp")+","+
                "\"created_local_date\":" + "\""+sdf.format(result.getLong("created_timestamp")*1000)+"\","+
                "\"status\":" + (result.getString("status") != null ? "\""+result.getString("status")+"\"" :  "null") +","+
                "\"given_name\":" + (result.getString("given_name") != null ? "\""+result.getString("given_name")+"\"" :  "null") +","+
                "\"family_name\":" + (result.getString("family_name") != null ? "\""+result.getString("family_name")+"\"" :  "null") +","+
                "\"assigned_location\": { " +
                "\"zone\":" + result.getInt("assigned_location_zone_id")+","+
                "\"tent\":" + result.getInt("assigned_location_tent_id")+","+
                "\"bed\":" + result.getInt("assigned_location_bed") +
                "},"+
                "\"age\": { " +
                "\"years\":" + result.getInt("age_years")+","+
                "\"months\":" + result.getInt("age_months")+","+
                "\"certainty\":" + (result.getString("age_certainty") != null ? "\""+result.getString("age_certainty")+"\"" :  "null") +","+
                "\"type\":" + (result.getString("age_type") != null ? "\""+result.getString("age_type")+"\"" :  "null") +""+
                "},"+
                "\"gender\":" + (result.getString("gender") != null ? "\""+result.getString("gender")+"\"" :  "null") +","+
                "\"important_information\":" + (result.getString("important_information") != null ? "\""+result.getString("important_information")+"\"" :  "null") +","+
                "\"pregnancy_start_date\":" + (result.getLong("pregnancy_start_timestamp") > 0 ? "\""+sdf.format(result.getLong("pregnancy_start_timestamp")*1000)+"\"" : "null")+","+
                "\"first_showed_symptoms_timestamp_utc\":" + result.getLong("first_showed_symptoms_timestamp")+","+
                "\"first_showed_symptoms_local_date\":" + "\""+sdf.format(result.getLong("first_showed_symptoms_timestamp")*1000)+"\","+
                "\"movement\":" + (result.getString("movement") != null ? "\""+result.getString("movement")+"\"" :  "null") +","+
                "\"eating\":" +(result.getString("eating") != null ? "\""+result.getString("eating")+"\"" :  "null") +","+
                "\"origin_location\":" +(result.getString("origin_location") != null ? "\""+result.getString("origin_location")+"\"" :  "null") +","+
                "\"next_of_kin\":" + (result.getString("next_of_kin") != null ? "\""+result.getString("next_of_kin")+"\"" :  "null") +""+
                "}";
    }
    public static String getBody(HttpServletRequest request) throws IOException {

        String body = null;
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = null;

        try {
            InputStream inputStream = request.getInputStream();
            if (inputStream != null) {
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                char[] charBuffer = new char[128];
                int bytesRead = -1;
                while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
                    stringBuilder.append(charBuffer, 0, bytesRead);
                }
            } else {
                stringBuilder.append("");
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException ex) {
                    throw ex;
                }
            }
        }

        body = stringBuilder.toString();
        return body;
    }
}
