package in.reeltime.security;

import org.codehaus.groovy.grails.web.json.JSONObject;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");

        String jsonResponse = generateJsonErrorResponse(accessDeniedException);
        response.getWriter().write(jsonResponse);
    }

    private String generateJsonErrorResponse(AccessDeniedException accessDeniedException) {
        JSONObject json = new JSONObject();
        json.put("error", accessDeniedException.getMessage());
        return json.toString();
    }
}
