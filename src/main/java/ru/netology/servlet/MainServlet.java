package ru.netology.servlet;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import ru.netology.config.ApplicationConfig;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

public class MainServlet extends HttpServlet {

    private GenericApplicationContext context;
    private HandlerMapping handlerMapping;

    @Override
    public void init() {
        context = new AnnotationConfigApplicationContext(ApplicationConfig.class);
        handlerMapping = context.getBean(HandlerMapping.class);
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) {
        try {
            final var path = req.getServletPath();
            final var method = req.getMethod();
            final var handler = handlerMapping.getHandler(method, path);
            if (handler.isPresent()) {
                handler.get().handle(req, resp);
            } else {
                resp.setStatus(SC_NOT_FOUND);
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(SC_INTERNAL_SERVER_ERROR);
        }
    }

    public GenericApplicationContext getContext() {
        return context;
    }
}

