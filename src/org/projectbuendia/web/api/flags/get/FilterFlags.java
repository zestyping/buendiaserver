package org.projectbuendia.web.api.flags.get;

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
public class FilterFlags implements ApiInterface {
    @Override
    public void call(final HttpServletRequest request, final HttpServletResponse response,final HashMap<String, String> urlVariables, final Map<String, String[]> parameterMap, final JsonObject json, final HashMap<String, String> payLoad){

        final String[] responseText = new String[]{null};

        StringBuilder whereString = new StringBuilder();

        if(parameterMap.containsKey("search")) {
        } else {
            for (String s : parameterMap.keySet()) {
                if (s.contains("limit") || s.contains("offset") || s.contains("order")) {
                    continue;
                }
                if(whereString.length() <= 0) {
                    whereString.append(" WHERE `" + s + "` LIKE '%" + parameterMap.get(s)[0] + "%'");
                } else {
                    whereString.append(" AND `" + s + "` LIKE '%" + parameterMap.get(s)[0] + "%'");
                }
            }
        }
        String queryString = "" +
                "SELECT * FROM `flags`" +
                (!parameterMap.isEmpty() ?
                        whereString.toString()
                        : ""
                )
                +
                (parameterMap.containsKey("limit") && parameterMap.containsKey("offset")  ?
                        " LIMIT " +parameterMap.get("offset")[0] + "," +parameterMap.get("limit")[0]
                        :
                        parameterMap.containsKey("limit") ? " LIMIT " + parameterMap.get("limit")[0]
                                :
                                /*parameterMap.containsKey("offset") ? " OFFSET " + parameterMap.get("offset")[0]
                                        : todo(pim) figure out why this doesnt work, for now offset only works in combination with limit */"");

        ;

        System.out.println(queryString);
        StringBuilder s = new StringBuilder();
        try (ResultSet result = Server.getSqlDatabase().query(queryString)) {
            while (result.next()) {
                if (result.isFirst()) {
                    s.append(SharedFunctions.SpecificFlagResponse(result));
                } else {
                    s.append("," + SharedFunctions.SpecificFlagResponse(result));
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
