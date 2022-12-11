package ru.netology.service;

import org.springframework.stereotype.Service;
import ru.netology.exception.NotFoundException;
import ru.netology.model.Post;
import ru.netology.repository.PostRepository;

import java.util.List;

@Service
public class PostService {
    private static final long EMPTY_ID = 0L;

    private final PostRepository repository;

    public PostService(PostRepository repository) {
        this.repository = repository;
    }

    public List<Post> all() {
        return repository.all();
    }

    public Post getById(long id) {
        return repository.getById(id).orElseThrow(NotFoundException::new);
    }

    public Post save(Post post) {
        return isNew(post)
                ? repository.save(post)
                : updateOrError(post);
    }

    private boolean isNew(Post post) {
        return post.getId() == EMPTY_ID;
    }

    private Post updateOrError(Post post) {
        return repository.getById(post.getId())
                .map(p -> post)
                .map(repository::save)
                .orElseThrow(NotFoundException::new);
    }

    public void removeById(long id) {
        repository.removeById(id);
    }
}

