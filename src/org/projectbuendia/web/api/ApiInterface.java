package org.projectbuendia.web.api;

import com.google.gson.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by wwadewitte on 10/8/14.
 */
public interface ApiInterface {
    /* with payload */
    public abstract void call(final HttpServletRequest request, final HttpServletResponse response,final HashMap<String, String> urlVariables, final Map<String, String[]> parameterMap, JsonObject json, final HashMap<String, String> payLoad) throws IOException;
}
