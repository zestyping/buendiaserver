package org.projectbuendia.web.api.flags.post;

import org.projectbuendia.server.Server;
import org.projectbuendia.sqlite.SQLiteUpdate;
import org.projectbuendia.web.api.ApiInterface;

import com.google.gson.JsonElement;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wwadewitte on 10/11/14.
 */
public class AddNewFlag implements ApiInterface {
    @Override
    public void call(final HttpServletRequest request, final HttpServletResponse response,final HashMap<String, String> urlVariables, final Map<String, String[]> parameterMap, final JsonElement json, final HashMap<String, String> payLoad){

        Server.getLocalDatabase().executeUpdate(new SQLiteUpdate("" +
                "INSERT INTO `patients`" +
                "()" +
                "VALUES" +
                "()"
        ));
        String responseText = "{";

        responseText = responseText + "}";

        response.setStatus(HttpServletResponse.SC_OK);
        try {
            response.getWriter().write(responseText);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
