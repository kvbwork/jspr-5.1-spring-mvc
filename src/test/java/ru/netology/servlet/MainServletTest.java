package ru.netology.servlet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.DefaultMockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.netology.model.Post;
import ru.netology.repository.PostRepository;

import java.util.List;

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
    PostRepository postRepository;

    @BeforeEach
    void setUp() {
        DefaultMockMvcBuilder builder = MockMvcBuilders.webAppContextSetup(context);
        mockMvc = builder.build();
    }

    @AfterEach
    void tearDown() {
        context = null;
        postRepository = null;
        mockMvc = null;
    }

    @Test
    void get_all_posts_empty_success() throws Exception {
        final var jsonEmptyArray = "[]";
        final var path = "/api/posts";

        mockMvc.perform(
                get(path)
        ).andExpectAll(
                status().isOk(),
                content().contentTypeCompatibleWith(APPLICATION_JSON),
                content().string(jsonEmptyArray)
        );
    }

    @Test
    void get_all_posts_list_success() throws Exception {
        final var path = "/api/posts";
        final var postNames = List.of("POST1", "POST2", "POST3");
        postNames.forEach(name -> postRepository.save(new Post(0, name)));

        final var response = mockMvc.perform(
                        get(path)
                )
                .andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(APPLICATION_JSON_VALUE)
                ).andReturn().getResponse();

        final var responsePostList = gson.fromJson(response.getContentAsString(), new TypeToken<List<Post>>() {
        });
        assertThat(responsePostList.stream()
                        .filter(post -> postNames.contains(post.getContent()))
                        .count(),
                is((long) postNames.size())
        );
    }


    @Test
    void get_post_not_found_success() throws Exception {
        final var path = "/api/posts/999";

        mockMvc.perform(
                get(path)
        ).andExpectAll(
                status().isNotFound()
        );
    }

    @Test
    void get_post_found_success() throws Exception {
        final var post = postRepository.save(new Post(0, TEST_STRING));
        final var requestedId = post.getId();
        final var path = "/api/posts/" + requestedId;

        final var response = mockMvc.perform(
                        get(path)
                ).andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(APPLICATION_JSON_VALUE)
                )
                .andReturn().getResponse();

        final var storedPost = gson.fromJson(response.getContentAsString(), Post.class);

        assertThat(storedPost.getContent(), equalTo(TEST_STRING));
        assertThat(storedPost.getId(), is(requestedId));
    }

    @Test
    void post_add_post_success() throws Exception {
        final var post = new Post(0, TEST_STRING);
        final var path = "/api/posts";

        final var response = mockMvc.perform(
                post(path)
                        .contentType(APPLICATION_JSON_VALUE)
                        .content(gson.toJson(post))
        ).andExpectAll(
                status().isOk(),
                content().contentTypeCompatibleWith(APPLICATION_JSON_VALUE)
        ).andReturn().getResponse();

        final var responsePost = gson.fromJson(response.getContentAsString(), Post.class);
        final var storedPost = postRepository.getById(responsePost.getId()).orElseThrow();

        assertThat(responsePost.getContent(), equalTo(TEST_STRING));
        assertThat(responsePost.getId(), greaterThan(0L));
        assertThat(storedPost.getId(), is(responsePost.getId()));
    }

    @Test
    void post_update_post_success() throws Exception {
        final var path = "/api/posts";
        final var UPDATE_STRING = "UPDATED";
        var storedPost = postRepository.save(new Post(0, TEST_STRING));
        final var postUpdate = new Post(storedPost.getId(), UPDATE_STRING);

        final var response = mockMvc.perform(
                        post(path)
                                .contentType(APPLICATION_JSON_VALUE)
                                .content(gson.toJson(postUpdate))
                ).andExpectAll(
                        status().isOk(),
                        content().contentTypeCompatibleWith(APPLICATION_JSON_VALUE)
                )
                .andReturn().getResponse();

        final var responsePost = gson.fromJson(response.getContentAsString(), Post.class);
        assertThat(responsePost.getContent(), equalTo(postUpdate.getContent()));
        assertThat(responsePost.getId(), is(postUpdate.getId()));

        storedPost = postRepository.getById(storedPost.getId()).orElseThrow();
        assertThat(storedPost.getContent(), equalTo(postUpdate.getContent()));
    }


    @Test
    void delete_post_success() throws Exception {
        final var storedPost = postRepository.save(new Post(0, TEST_STRING));
        final var path = "/api/posts/" + storedPost.getId();

        final var postExists = postRepository.getById(storedPost.getId()).isPresent();

        mockMvc.perform(
                delete(path)
        ).andExpectAll(
                status().isOk()
        );

        final var postDeleted = postRepository.getById(storedPost.getId()).isEmpty();

        assertThat(postExists, is(true));
        assertThat(postDeleted, is(true));
    }
}