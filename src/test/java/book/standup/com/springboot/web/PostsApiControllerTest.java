package book.standup.com.springboot.web;

import book.standup.com.springboot.domain.posts.Posts;
import book.standup.com.springboot.domain.posts.PostsRepository;
import book.standup.com.springboot.web.dto.PostsSaveRequestDto;
import book.standup.com.springboot.web.dto.PostsUpdateRequestDto;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostsApiControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PostsRepository postsRepository;

    @After
    public void tearDown() throws Exception {
        postsRepository.deleteAll();
    }

    //setting
    String title = "title";
    String content = "content";
    String author = "author";

    @Test
    public void registPosts() throws Exception {

        PostsSaveRequestDto requestDto = PostsSaveRequestDto.builder().title(title).content(content).author(author).build();
        String url = "http://localhost:" + port + "/api/v1/posts";

        //request & response
        ResponseEntity<Long> responseEntity = restTemplate.postForEntity(url, requestDto, Long.class);

        //check
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isGreaterThan(0L);

        List<Posts> all = postsRepository.findAll();
        assertThat(all.get(0).getTitle()).isEqualTo(title);
        assertThat(all.get(0).getContent()).isEqualTo(content);

    }

    @Test
    public void modifyPosts() throws Exception {

        // given
        Posts savedPosts = postsRepository.save(Posts.builder().title(title).content(content).author(author).build());

        Long updateId = savedPosts.getId();
        String expectedTitle = "title2";
        String expectedContent = "content2";

        PostsUpdateRequestDto requestDto = PostsUpdateRequestDto.builder().title(expectedTitle).content(expectedContent).build();

        String url = "http://localhost:" + port + "/api/v1/posts/" + updateId;

        HttpEntity<PostsUpdateRequestDto> requestEntity = new HttpEntity<>(requestDto);

        // when
        ResponseEntity<Long> responseEntity = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Long.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isGreaterThan(0L);

        List<Posts> all = postsRepository.findAll();
        assertThat(all.get(0).getTitle()).isEqualTo(expectedTitle);
        assertThat(all.get(0).getContent()).isEqualTo(expectedContent);

    }

    @Test
    public void deletePosts() {

        // 1. 등록
        PostsSaveRequestDto requestDto = PostsSaveRequestDto.builder().title(title).content(content).author(author).build();
        String urlRegister = "http://localhost:" + port + "/api/v1/posts";

        //request & response
        ResponseEntity<Long> responseSaveEntity = restTemplate.postForEntity(urlRegister, requestDto, Long.class);

        //check
        assertThat(responseSaveEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseSaveEntity.getBody()).isGreaterThan(0L);

        List<Posts> all = postsRepository.findAll();
        Long registedId = all.get(0).getId();
        assertThat(all.get(0).getTitle()).isEqualTo(title);
        assertThat(all.get(0).getContent()).isEqualTo(content);

        // 2. 삭제
        String urlDelete = "http://localhost:" + port + "/api/v1/posts/" + registedId;
        // when
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpEntity entity = new HttpEntity(httpHeaders);
        ResponseEntity<Long> responseDeleteEntity = restTemplate.exchange(urlDelete, HttpMethod.DELETE, entity, Long.class);
        //check
        assertThat(responseDeleteEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseDeleteEntity.getBody()).isGreaterThan(0L);

    }

}
