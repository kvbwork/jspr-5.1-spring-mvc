package ru.netology.config;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.netology.controller.PostController;
import ru.netology.exception.NotFoundException;
import ru.netology.repository.PostRepository;
import ru.netology.service.PostService;
import ru.netology.servlet.HandlerMapping;

import java.util.Optional;

@Configuration
public class ApplicationConfig {

    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String DELETE = "DELETE";

    @Autowired
    public void configurePostRoutes(HandlerMapping handlers, PostController postController) {
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

    private static Optional<Long> getIdPathParam(String path) {
        final var lastSlashIndex = path.lastIndexOf("/");
        if (lastSlashIndex == -1 || lastSlashIndex >= path.length()) return Optional.empty();
        try {
            return Optional.of(Long.parseLong(path.substring(lastSlashIndex + 1)));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    @Bean
    public HandlerMapping handlerMapping() {
        return new HandlerMapping();
    }

    @Bean
    public PostRepository postRepository() {
        return new PostRepository();
    }

    @Bean
    public PostService postService(PostRepository postRepository) {
        return new PostService(postRepository);
    }

    @Bean
    public PostController postController(PostService postService, Gson gson) {
        return new PostController(postService, gson);
    }

    @Bean
    public Gson gson() {
        return new Gson();
    }
}
