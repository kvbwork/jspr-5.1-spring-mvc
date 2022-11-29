package ru.netology.repository;

import ru.netology.model.Post;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static java.util.Optional.ofNullable;

public class PostRepository {
    private static long EMPTY = 0;

    private AtomicLong lastId;
    private Map<Long, Post> storage;

    public PostRepository() {
        lastId = new AtomicLong();
        storage = new ConcurrentHashMap<>();
    }

    public List<Post> all() {
        return List.copyOf(storage.values());
    }

    public Optional<Post> getById(long id) {
        return ofNullable(storage.get(id));
    }

    public Post save(Post post) {
        final var id = post.getId();
        if (id == EMPTY || id > lastId.get() || getById(id).isEmpty()) {
            post.setId(lastId.incrementAndGet());
        }
        storage.put(post.getId(), post);
        return post;
    }

    public void removeById(long id) {
        storage.remove(id);
    }
}
