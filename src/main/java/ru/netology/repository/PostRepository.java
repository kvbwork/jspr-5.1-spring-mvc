package ru.netology.repository;

import org.springframework.stereotype.Repository;
import ru.netology.model.Post;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.util.Optional.*;
import static java.util.function.Predicate.not;

@Repository
public class PostRepository {
    private static final long EMPTY = 0;

    private final AtomicLong lastId;
    private final Map<Long, Post> storage;

    public PostRepository() {
        lastId = new AtomicLong();
        storage = new ConcurrentHashMap<>();
    }

    public List<Post> all() {
        return storage.values().stream()
                .filter(not(Post::isRemoved))
                .collect(Collectors.toList());
    }

    public Optional<Post> getById(long id) {
        return id == EMPTY
                ? empty()
                : ofNullable(storage.get(id))
                .filter(not(Post::isRemoved));
    }

    public Post save(Post post) {
        return getById(post.getId())
                .map(Post::getId)
                .or(() -> of(lastId.incrementAndGet()))
                .map(id -> {
                    post.setId(id);
                    storage.put(id, post);
                    return post;
                }).orElseThrow();
    }

    public void removeById(long id) {
        getById(id).ifPresent(post -> post.setRemoved(true));
    }
}
