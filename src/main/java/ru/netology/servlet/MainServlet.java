package ru.netology.servlet;

import ru.netology.controller.PostController;
import ru.netology.exception.NotFoundException;
import ru.netology.repository.PostRepository;
import ru.netology.service.PostService;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;

import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

public class MainServlet extends HttpServlet {
    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String DELETE = "DELETE";

    private PostController postController;
    private HandlerMapping handlerMapping;

    @Override
    public void init() {
        final var repository = new PostRepository();
        final var service = new PostService(repository);
        postController = new PostController(service);
        handlerMapping = new HandlerMapping();

        handlerMapping.addStaticPathHandler(GET, "/api/posts", (req, resp) -> {
            postController.all(resp);
        });
        handlerMapping.addPatternPathHandler(GET, "/api/posts/\\d+", (req, resp) -> {
            postController.getById(
                    getIdPathParam(req.getServletPath()).orElseThrow(NotFoundException::new), resp);
        });
        handlerMapping.addStaticPathHandler(POST, "/api/posts", (req, resp) -> {
            postController.save(req.getReader(), resp);
        });
        handlerMapping.addPatternPathHandler(DELETE, "/api/posts/\\d+", (req, resp) -> {
            postController.removeById(
                    getIdPathParam(req.getServletPath()).orElseThrow(NotFoundException::new), resp);
        });
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

    public static Optional<Long> getIdPathParam(String path) {
        final var lastSlashIndex = path.lastIndexOf("/");
        if (lastSlashIndex == -1 || lastSlashIndex >= path.length()) return Optional.empty();
        try {
            return Optional.of(Long.parseLong(path.substring(lastSlashIndex + 1)));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }
}

