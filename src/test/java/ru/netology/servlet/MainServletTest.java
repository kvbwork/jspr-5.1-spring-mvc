package ru.netology.servlet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.netology.dto.PostDto;
import ru.netology.model.Post;
import ru.netology.service.PostService;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringJUnitWebConfig(classes = {WebTestConfig.class})
class MainServletTest {
    static final String TEST_STRING = "TEST_STRING";
    static final Gson gson = new Gson();

    @Autowired
    WebApplicationContext context;
    MockMvc mockMvc;

    @Autowired
    PostService postService;

    @BeforeEach
    void setUp() {
        DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(context);
        mockMvc = builder.build();
    }

    @AfterEach
    void tearDown() {
        context = null;
        postService = null;
        mockMvc = null;
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void get_all_posts_empty_success() throws Exception {
        final var jsonEmptyArray = "[]";
        final var path = "/api/posts";

        mockMvc.perform(get(path))
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(APPLICATION_JSON),
                        content().string(jsonEmptyArray)
                );
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void get_all_posts_list_success() throws Exception {
        final var path = "/api/posts";
        final var postNames = List.of("POST1", "POST2", "POST3");
        postNames.forEach(name -> postService.save(new PostDto(0, name)));

        final var response = mockMvc.perform(get(path))
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(APPLICATION_JSON_VALUE)
                ).andReturn().getResponse();

        final var responsePostList = gson.fromJson(response.getContentAsString(), new TypeToken<List<PostDto>>() {
        });
        final var responsePostNamesCount = responsePostList.stream()
                .filter(post -> postNames.contains(post.getContent()))
                .count();
        assertThat(responsePostNamesCount, is((long) postNames.size()));
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void get_all_posts_without_removed_success() throws Exception {
        final var path = "/api/posts";
        final var postList = Stream.of("DELETED1", "DELETED2", "POST3", "POST4", "POST5")
                .map(name -> new PostDto(0, name))
                .map(post -> postService.save(post))
                .collect(Collectors.toList());

        postList.stream()
                .filter(post -> post.getContent().startsWith("DELETED"))
                .forEach(post -> postService.removeById(post.getId()));

        final var response = mockMvc.perform(get(path))
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(APPLICATION_JSON_VALUE)
                ).andReturn().getResponse();

        final var responsePostList = gson.fromJson(response.getContentAsString(), new TypeToken<List<PostDto>>() {
        });
        final var deletedPostCount = responsePostList.stream()
                .filter(post -> post.getContent().startsWith("DELETED"))
                .count();
        assertThat(responsePostList.size(), lessThan(postList.size()));
        assertThat(deletedPostCount, is(0L));
    }

    @Test
    void get_post_not_found_success() throws Exception {
        final var path = "/api/posts/999";
        mockMvc.perform(get(path)).andExpect(status().isNotFound());
    }

    @Test
    void get_post_found_success() throws Exception {
        final var post = postService.save(new PostDto(0, TEST_STRING));
        final var requestedId = post.getId();
        final var path = "/api/posts/" + requestedId;

        final var response = mockMvc.perform(get(path))
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(APPLICATION_JSON_VALUE)
                ).andReturn().getResponse();

        final var storedPost = gson.fromJson(response.getContentAsString(), PostDto.class);

        assertThat(storedPost.getContent(), equalTo(TEST_STRING));
        assertThat(storedPost.getId(), is(requestedId));
    }

    @Test
    void post_add_post_success() throws Exception {
        final var post = new PostDto(0, TEST_STRING);
        final var path = "/api/posts";

        final var response = mockMvc.perform(post(path)
                .contentType(APPLICATION_JSON_VALUE)
                .content(gson.toJson(post))
        ).andExpectAll(
                status().isOk(),
                content().contentTypeCompatibleWith(APPLICATION_JSON_VALUE)
        ).andReturn().getResponse();

        final var responsePost = gson.fromJson(response.getContentAsString(), PostDto.class);
        final var storedPost = postService.getById(responsePost.getId());

        assertThat(responsePost.getContent(), equalTo(TEST_STRING));
        assertThat(responsePost.getId(), greaterThan(0L));
        assertThat(storedPost.getId(), is(responsePost.getId()));
    }

    @Test
    void update_existing_post_success() throws Exception {
        final var path = "/api/posts";
        final var UPDATE_STRING = "UPDATED";
        var storedPost = postService.save(new PostDto(0, TEST_STRING));
        final var postUpdate = new Post(storedPost.getId(), UPDATE_STRING);

        final var response = mockMvc.perform(post(path)
                .contentType(APPLICATION_JSON_VALUE)
                .content(gson.toJson(postUpdate))
        ).andExpectAll(
                status().isOk(),
                content().contentTypeCompatibleWith(APPLICATION_JSON_VALUE)
        ).andReturn().getResponse();

        final var responsePost = gson.fromJson(response.getContentAsString(), PostDto.class);
        assertThat(responsePost.getContent(), equalTo(postUpdate.getContent()));
        assertThat(responsePost.getId(), is(postUpdate.getId()));

        storedPost = postService.getById(storedPost.getId());
        assertThat(storedPost.getContent(), equalTo(postUpdate.getContent()));
    }

    @Test
    void update_deleted_post_failure() throws Exception {
        final var path = "/api/posts";
        final var UPDATE_STRING = "UPDATED";
        final var storedPost = postService.save(new PostDto(0, TEST_STRING));
        postService.removeById(storedPost.getId());
        final var postUpdate = new PostDto(storedPost.getId(), UPDATE_STRING);

        mockMvc.perform(post(path)
                .contentType(APPLICATION_JSON_VALUE)
                .content(gson.toJson(postUpdate))
        ).andExpectAll(
                status().isNotFound()
        );
    }

    @Test
    void delete_post_success() throws Exception {
        final var storedPost = postService.save(new PostDto(0, TEST_STRING));
        final var path = "/api/posts/" + storedPost.getId();

        mockMvc.perform(get(path)).andExpect(status().isOk());
        mockMvc.perform(delete(path)).andExpect(status().isOk());
        mockMvc.perform(get(path)).andExpect(status().isNotFound());
    }
}