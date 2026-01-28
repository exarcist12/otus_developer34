package ru.otus.server;

import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.security.ConstraintMapping;
import org.eclipse.jetty.ee10.servlet.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.Constraint;
import org.eclipse.jetty.security.LoginService;
import org.eclipse.jetty.security.authentication.BasicAuthenticator;
import org.eclipse.jetty.server.Handler;
import ru.otus.crm.service.DBServiceClient;
import ru.otus.services.TemplateProcessor;

public class UsersWebServerWithBasicSecurity extends UsersWebServerSimple {
    private static final String ROLE_NAME_USER = "user";
    private static final String ROLE_NAME_ADMIN = "admin";

    private final LoginService loginService;

    public UsersWebServerWithBasicSecurity(
            int port,
            LoginService loginService,
            DBServiceClient dbServiceClient,
            Gson gson,
            TemplateProcessor templateProcessor) {
        super(port, dbServiceClient, gson, templateProcessor);
        this.loginService = loginService;
    }



    @Override
    protected Handler applySecurity(ServletContextHandler servletContextHandler, String... paths) {

        List<ConstraintMapping> constraintMappings = new ArrayList<>();
        Constraint constraintGet = Constraint.from(ROLE_NAME_USER, ROLE_NAME_ADMIN);
        for (String path : paths) {
            ConstraintMapping mapping = new ConstraintMapping();
            mapping.setPathSpec(path);
            mapping.setMethod("GET");
            mapping.setConstraint(constraintGet);
            constraintMappings.add(mapping);
        }

        Constraint constraintPost = Constraint.from(ROLE_NAME_ADMIN);
        for (String path : paths) {

            ConstraintMapping mapping = new ConstraintMapping();
            mapping.setPathSpec(path);
            mapping.setMethod("POST");
            mapping.setConstraint(constraintPost);
            constraintMappings.add(mapping);
        }

        ConstraintSecurityHandler security = new ConstraintSecurityHandler();
        security.setAuthenticator(new BasicAuthenticator());
        security.setLoginService(loginService);
        security.setConstraintMappings(constraintMappings);
        security.setHandler(new Handler.Wrapper(servletContextHandler));

        return security;
    }
}
