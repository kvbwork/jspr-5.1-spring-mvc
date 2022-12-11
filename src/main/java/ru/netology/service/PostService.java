package ru.netology.service;

import org.springframework.stereotype.Service;
import ru.netology.dto.PostDto;
import ru.netology.exception.NotFoundException;
import ru.netology.mapper.PostMapper;
import ru.netology.model.Post;
import ru.netology.repository.PostRepository;

import java.util.ArrayList;
import java.util.List;

import static java.util.function.Predicate.not;

@Service
public class PostService {
    private static final long EMPTY_ID = 0L;

    private final PostRepository repository;
    private final PostMapper postMapper;

    public PostService(PostRepository repository, PostMapper postMapper) {
        this.repository = repository;
        this.postMapper = postMapper;
    }

    public List<PostDto> all() {
        final var postDtoList = new ArrayList<PostDto>();
        for (var post : repository.all()) {
            if (post.isRemoved()) continue;
            final var postDto = postMapper.postToPostDto(post);
            postDtoList.add(postDto);
        }
        return postDtoList;
    }

    public PostDto getById(long id) {
        return repository.getById(id)
                .filter(not(Post::isRemoved))
                .map(postMapper::postToPostDto)
                .orElseThrow(NotFoundException::new);
    }

    public PostDto save(PostDto postDto) {
        final var post = postMapper.postDtoToPost(postDto);
        final var postEntity = isNew(post)
                ? repository.save(post)
                : updateOrError(post);
        return postMapper.postToPostDto(postEntity);
    }

    private boolean isNew(Post post) {
        return post.getId() == EMPTY_ID;
    }

    private Post updateOrError(Post post) {
        return repository.getById(post.getId())
                .filter(not(Post::isRemoved))
                .map(p -> post)
                .map(repository::save)
                .orElseThrow(NotFoundException::new);
    }

    public void removeById(long id) {
        repository.getById(id)
                .ifPresent(post -> post.setRemoved(true));
    }
}

