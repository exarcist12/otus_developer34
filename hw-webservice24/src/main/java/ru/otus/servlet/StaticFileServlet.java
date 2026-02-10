package ru.otus.servlet;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

public class StaticFileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String uri = request.getRequestURI();
        System.out.println("StaticFileServlet: " + uri);

        String resourcePath = uri;
        if (uri.startsWith("/static/")) {
            resourcePath = uri.substring(1);
        }

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath)) {

            if (is == null) {
                System.out.println("  Not found: " + resourcePath);
                response.sendError(404);
                return;
            }

            if (uri.endsWith(".html")) {
                response.setContentType("text/html; charset=UTF-8");
            } else if (uri.endsWith(".css")) {
                response.setContentType("text/css");
            } else if (uri.endsWith(".js")) {
                response.setContentType("application/javascript");
            }

            is.transferTo(response.getOutputStream());
            System.out.println("  Served successfully: " + uri);
        }
    }
}
