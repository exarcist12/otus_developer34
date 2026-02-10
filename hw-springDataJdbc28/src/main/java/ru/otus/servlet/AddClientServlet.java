package ru.otus.servlet;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.stream.Collectors;
import ru.otus.crm.model.Client;
import ru.otus.crm.service.DBServiceClient;

public class AddClientServlet extends HttpServlet {

    private final DBServiceClient dbServiceClient;
    private final Gson gson;

    public AddClientServlet(DBServiceClient dbServiceClient, Gson gson) {
        this.dbServiceClient = dbServiceClient;
        this.gson = gson;
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");

        try {

            BufferedReader reader = req.getReader();
            String jsonBody = reader.lines().collect(Collectors.joining());

            Client client = gson.fromJson(jsonBody, Client.class);

            if (client.getName() == null || client.getName().trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().println("{\"error\":\"Client name is required\"}");
                return;
            }

            if (client.getAddress() != null) {
                client.getAddress().setClient(client);
            }

            if (client.getPhones() != null) {
                client.getPhones().forEach(phone -> phone.setClient(client));
            }

            Client savedClient = dbServiceClient.saveClient(client);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            resp.getWriter().println(gson.toJson(savedClient));

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().println("{\"error\":\"Server error: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
}
