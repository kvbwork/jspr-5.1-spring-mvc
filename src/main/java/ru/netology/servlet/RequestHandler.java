package ru.netology.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@FunctionalInterface
public interface RequestHandler {
    void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException;
}
