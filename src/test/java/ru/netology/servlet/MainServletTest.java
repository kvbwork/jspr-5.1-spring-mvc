package ru.netology.servlet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import ru.netology.model.Post;
import ru.netology.repository.PostRepository;

import java.io.UnsupportedEncodingException;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class MainServletTest {
    static final String TEST_STRING = "TEST_STRING";
    static final Gson gson = new Gson();

    GenericApplicationContext context;
    PostRepository postRepository;
    MockHttpServletRequest request;
    MockHttpServletResponse response;
    MainServlet sut;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        sut = new MainServlet();
        sut.init();
        context = sut.getContext();
        postRepository = context.getBean(PostRepository.class);
    }

    @AfterEach
    void tearDown() {
        request = null;
        response = null;
        postRepository = null;
        sut = null;
        context.close();
        context = null;
    }

    @Test
    void get_all_posts_empty_success() throws UnsupportedEncodingException {
        final var jsonEmptyArray = "[]";
        request.setMethod("GET");
        request.setServletPath("/api/posts");

        sut.service(request, response);

        assertThat(response.getContentType(), equalTo(APPLICATION_JSON_VALUE));
        assertThat(response.getContentAsString(), equalTo(jsonEmptyArray));
    }

    @Test
    void get_all_posts_list_success() throws UnsupportedEncodingException {
        final var postList = List.of(
                new Post(0, "POST1"),
                new Post(0, "POST2"),
                new Post(0, "POST3")
        );
        postList.forEach(post -> postRepository.save(post));

        request.setMethod("GET");
        request.setServletPath("/api/posts");

        sut.service(request, response);
        var responsePostList = gson.fromJson(response.getContentAsString(), new TypeToken<List<Post>>() {
        });

        assertThat(response.getContentType(), equalTo(APPLICATION_JSON_VALUE));
        assertThat(responsePostList.size(), is(postList.size()));
    }


    @Test
    void get_post_not_found_success() {
        request.setMethod("GET");
        request.setServletPath("/api/posts/999");

        sut.service(request, response);
        assertThat(response.getStatus(), is(HttpStatus.NOT_FOUND.value()));
    }

    @Test
    void get_post_found_success() throws UnsupportedEncodingException {
        var post = postRepository.save(new Post(0, TEST_STRING));
        var requestedId = post.getId();
        request.setMethod("GET");
        request.setServletPath("/api/posts/" + requestedId);

        sut.service(request, response);
        var storedPost = gson.fromJson(response.getContentAsString(), Post.class);

        assertThat(response.getContentType(), equalTo(APPLICATION_JSON_VALUE));
        assertThat(response.getStatus(), is(HttpStatus.OK.value()));
        assertThat(storedPost.getContent(), equalTo(TEST_STRING));
        assertThat(storedPost.getId() == requestedId, is(true));
    }

    @Test
    void post_add_post_success() throws UnsupportedEncodingException {
        var post = new Post(0, TEST_STRING);
        request.setMethod("POST");
        request.setServletPath("/api/posts");
        request.setContentType(APPLICATION_JSON_VALUE);
        request.setContent(gson.toJson(post).getBytes());

        sut.service(request, response);
        var responsePost = gson.fromJson(response.getContentAsString(), Post.class);
        var storedPost = postRepository.getById(responsePost.getId()).orElseThrow();

        assertThat(response.getContentType(), equalTo(APPLICATION_JSON_VALUE));
        assertThat(response.getStatus(), is(HttpStatus.CREATED.value()));
        assertThat(responsePost.getContent(), equalTo(TEST_STRING));
        assertThat(responsePost.getId() > 0, is(true));
        assertThat(storedPost.getId() == responsePost.getId(), is(true));
    }

    @Test
    void post_update_post_success() throws UnsupportedEncodingException {
        final var UPDATE_STRING = "UPDATED";
        var storedPost = postRepository.save(new Post(0, TEST_STRING));
        var postUpdate = new Post(storedPost.getId(), UPDATE_STRING);

        request.setMethod("POST");
        request.setServletPath("/api/posts");
        request.setContentType(APPLICATION_JSON_VALUE);
        request.setContent(gson.toJson(postUpdate).getBytes());

        sut.service(request, response);
        var responsePost = gson.fromJson(response.getContentAsString(), Post.class);

        assertThat(response.getContentType(), equalTo(APPLICATION_JSON_VALUE));
        assertThat(response.getStatus(), is(HttpStatus.OK.value()));
        assertThat(responsePost.getContent(), equalTo(UPDATE_STRING));
        assertThat(responsePost.getId(), is(postUpdate.getId()));

        storedPost = postRepository.getById(storedPost.getId()).orElseThrow();
        assertThat(storedPost.getContent(), equalTo(UPDATE_STRING));
    }


    @Test
    void delete_post_success() {
        var storedPost = postRepository.save(new Post(0, TEST_STRING));
        request.setMethod("DELETE");
        request.setServletPath("/api/posts/" + storedPost.getId());

        var postExists = postRepository.getById(storedPost.getId()).isPresent();
        sut.service(request, response);
        var postDeleted = postRepository.getById(storedPost.getId()).isEmpty();

        assertThat(response.getStatus(), is(HttpStatus.NO_CONTENT.value()));
        assertThat(postExists, is(true));
        assertThat(postDeleted, is(true));

    }
}