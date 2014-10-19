package org.projectbuendia.web.api;

import org.projectbuendia.fileops.Logging;
import org.projectbuendia.web.JettyServer;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * Created by wwadewitte on 10/11/14.
 */
public abstract class ApiHandler extends HttpServlet {

    private HashMap<String, String> urlVariables = new HashMap<String, String>();

    protected abstract String getBaseUrl();

    private JsonElement json = null;
    private HashMap<String, String> payLoad = new HashMap<String, String>();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException{
        callHandler("get", request, response);
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException{
        callHandler("post", request, response);
    }
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException{
        callHandler("put", request, response);
    }
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException{
        callHandler("delete", request, response);
    }

    private void callHandler(String method, HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            reportPayload(request, response);

            if (request.getPathInfo() == null) {
                JettyServer.getApiStructure(method).get(getBaseUrl()).call(request, response, urlVariables, request.getParameterMap(), json, payLoad);
                return;
            }
            String[] pathArray = request.getPathInfo().replaceFirst("/", "").split("/");
            StringBuilder lastFoundHandler = new StringBuilder();

            lastFoundHandler.append(getBaseUrl());

            for (int i = 0; i < pathArray.length; i++) {
                if ((pathArray[i].isEmpty())) {
                    continue;
                } else if (JettyServer.getApiStructure(method).containsKey(lastFoundHandler.toString() + "/" + pathArray[i])) {
                    lastFoundHandler.append("/" + pathArray[i]);
                } else if (JettyServer.getApiStructure(method).containsKey(lastFoundHandler.toString() + "/*")) {
                    lastFoundHandler.append("/*");
                    urlVariables.put(getSpecification()[i], pathArray[i]);
                }
            }

            JettyServer.getApiStructure(method).get(lastFoundHandler.toString()).call(request, response, urlVariables, request.getParameterMap(), json, payLoad);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reportPayload(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String type = request.getContentType();
        if (type == null) return;
        String body = SharedFunctions.getBody(request);
        if (body == null) return;

        type = type.split(";")[0].trim();
        if (type.equals("application/json")) {
            try {
                json = new JsonParser().parse(body);
            } catch (JsonParseException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                throw e;  // don't call the request handler
            }
        } else if (type.equals("application/x-www-form-urlencoded")) {
            String[] nodes = body.split(Pattern.quote("&"));
            for(String s : nodes) {
                if(s.length() <= 1) {
                    continue;
                }
                System.out.println("Node:" + s);
                String[] keyValue = s.split(Pattern.quote("="));
                if(keyValue[1] != null) {
                    payLoad.put(URLDecoder.decode(keyValue[0]), URLDecoder.decode(keyValue[1]));
                } else {

                }
            }
        }
    }
;

    protected abstract String[] getSpecification();
}
