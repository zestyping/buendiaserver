package org.projectbuendia.web.api.flags.get;

import org.projectbuendia.server.Server;
import org.projectbuendia.sqlite.SQLiteQuery;
import org.projectbuendia.web.api.ApiInterface;
import org.projectbuendia.web.api.SharedFunctions;

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
    public void call(final HttpServletRequest request, final HttpServletResponse response,final HashMap<String, String> urlVariables, final Map<String, String[]> parameterMap, final HashMap<String, String> payLoad){

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
        SQLiteQuery checkQuery = new SQLiteQuery(queryString) {

            @Override
            public void execute(ResultSet result) throws SQLException {
                StringBuilder s = new StringBuilder();
                while (result.next()) {
                    if(result.isFirst()) {
                        s.append(SharedFunctions.SpecificFlagResponse(result));
                    } else {
                        s.append("," + SharedFunctions.SpecificFlagResponse(result));
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
