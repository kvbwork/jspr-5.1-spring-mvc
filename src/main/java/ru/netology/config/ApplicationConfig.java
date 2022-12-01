package ru.netology.config;

import com.google.gson.Gson;
import ru.netology.controller.PostController;
import ru.netology.exception.NotFoundException;
import ru.netology.repository.PostRepository;
import ru.netology.service.PostService;
import ru.netology.servlet.HandlerMapping;

import java.util.Optional;

public class ApplicationConfig {
    private static volatile ApplicationConfig INSTANCE;

    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String DELETE = "DELETE";

    private final HandlerMapping handlerMapping;
    private final PostRepository postRepository;
    private final PostService postService;
    private final PostController postController;
    private final Gson gson;

    private ApplicationConfig() {
        gson = new Gson();
        postRepository = new PostRepository();
        postService = new PostService(postRepository);
        postController = new PostController(postService, gson);
        handlerMapping = new HandlerMapping();

        configureRoutes(handlerMapping);
    }

    private void configureRoutes(HandlerMapping handlers) {
        handlers.addStaticPathHandler(GET, "/api/posts", (req, resp) -> {
            postController.all(resp);
        });
        handlers.addPatternPathHandler(GET, "/api/posts/\\d+", (req, resp) -> {
            postController.getById(
                    getIdPathParam(req.getServletPath()).orElseThrow(NotFoundException::new), resp);
        });
        handlers.addStaticPathHandler(POST, "/api/posts", (req, resp) -> {
            postController.save(req.getReader(), resp);
        });
        handlers.addPatternPathHandler(DELETE, "/api/posts/\\d+", (req, resp) -> {
            postController.removeById(
                    getIdPathParam(req.getServletPath()).orElseThrow(NotFoundException::new), resp);
        });
    }

    public static ApplicationConfig getInstance() {
        if (INSTANCE == null) {
            synchronized (ApplicationConfig.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ApplicationConfig();
                }
            }
        }
        return INSTANCE;
    }

    public void dispose() {
        INSTANCE = null;
    }

    private static Optional<Long> getIdPathParam(String path) {
        final var lastSlashIndex = path.lastIndexOf("/");
        if (lastSlashIndex == -1 || lastSlashIndex >= path.length()) return Optional.empty();
        try {
            return Optional.of(Long.parseLong(path.substring(lastSlashIndex + 1)));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    public HandlerMapping getHandlerMapping() {
        return handlerMapping;
    }

    public PostRepository getPostRepository() {
        return postRepository;
    }

    public PostService getPostService() {
        return postService;
    }

    public PostController getPostController() {
        return postController;
    }

    public Gson getGson() {
        return gson;
    }
}
