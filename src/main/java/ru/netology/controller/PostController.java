package ru.netology.controller;

import com.google.gson.Gson;
import ru.netology.exception.NotFoundException;
import ru.netology.model.Post;
import ru.netology.service.PostService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Reader;

import static javax.servlet.http.HttpServletResponse.*;

public class PostController {
    private static final String APPLICATION_JSON = "application/json";

    private final PostService service;
    private final Gson gson;

    public PostController(PostService service, Gson gson) {
        this.service = service;
        this.gson = gson;
    }

    public void all(HttpServletResponse response) throws IOException {
        response.setContentType(APPLICATION_JSON);
        final var data = service.all();
        response.getWriter().print(gson.toJson(data));
    }

    public void getById(long id, HttpServletResponse response) throws IOException {
        response.setContentType(APPLICATION_JSON);
        try {
            final var post = service.getById(id);
            response.getWriter().print(gson.toJson(post));
        } catch (NotFoundException ex) {
            response.setStatus(SC_NOT_FOUND);
        }
    }

    public void save(Reader body, HttpServletResponse response) throws IOException {
        response.setContentType(APPLICATION_JSON);
        final var post = gson.fromJson(body, Post.class);
        final var requestedId = post.getId();
        final var data = service.save(post);
        if (requestedId != data.getId()) response.setStatus(SC_CREATED);
        response.getWriter().print(gson.toJson(data));
    }

    public void removeById(long id, HttpServletResponse response) {
        service.removeById(id);
        response.setStatus(SC_NO_CONTENT);
    }
}
