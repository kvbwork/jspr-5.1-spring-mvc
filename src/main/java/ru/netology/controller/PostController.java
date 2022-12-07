package ru.netology.controller;

import org.springframework.web.bind.annotation.*;
import ru.netology.dto.PostDto;
import ru.netology.exception.NotFoundException;
import ru.netology.mapper.PostMapper;
import ru.netology.service.PostService;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Optional.of;

@RestController
@RequestMapping("/api/posts")
public class PostController {
    private final PostService service;
    private final PostMapper postMapper;

    public PostController(PostService service, PostMapper postMapper) {
        this.service = service;
        this.postMapper = postMapper;
    }

    @GetMapping
    public List<PostDto> all() {
        return service.all().stream()
                .map(postMapper::postToPostDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public PostDto getById(@PathVariable long id) {
        return postMapper.postToPostDto(service.getById(id));
    }

    @PostMapping
    public PostDto save(@RequestBody PostDto postDto) {
        return of(postDto)
                .map(postMapper::postDtoToPost)
                .map(service::save)
                .map(postMapper::postToPostDto)
                .orElseThrow(NotFoundException::new);
    }

    @DeleteMapping("/{id}")
    public void removeById(@PathVariable long id) {
        service.removeById(id);
    }
}