package org.projectbuendia.web.api;

import com.google.inject.Singleton;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Singleton
public class ServletFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain filterChain) throws IOException, ServletException {



        if(response instanceof HttpServletResponse){
            HttpServletResponse alteredResponse = ((HttpServletResponse)response);
            // I need to find a way to make sure this only gets called on 200-300 http responses
            // TODO: see above comment
            addHeadersFor200Response(alteredResponse);
        }
        //System.out.println("response: " + response.getContentType());
       // System.out.println("request: " + request.getContentType());
        filterChain.doFilter(request, response);
    }

    private void addHeadersFor200Response(HttpServletResponse response){
        //TODO: externalize the Allow-Origin
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
        //response.addHeader("Access-Control-Allow-Headers", "X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept");
        response.addHeader("Access-Control-Max-Age", "1728000");

        // Specifying utf-8 here is important, as it sets the character
        // encoding used by response.getWriter().
        response.setContentType("application/json; charset=UTF-8");
    }

    @Override
    public void destroy() {}

    @Override
    public void init(FilterConfig filterConfig)throws ServletException {}
}
