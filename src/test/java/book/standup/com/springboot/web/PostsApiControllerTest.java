package book.standup.com.springboot.web;

import book.standup.com.springboot.domain.posts.Posts;
import book.standup.com.springboot.domain.posts.PostsRepository;
import book.standup.com.springboot.web.dto.PostsSaveRequestDto;
import book.standup.com.springboot.web.dto.PostsUpdateRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostsApiControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PostsRepository postsRepository;

    @Autowired
    private WebApplicationContext context;


    private MockMvc mvc;

    // @SpringBootTest 에서 MockMvc 를 사용하기 위한 setup : 테스트 시작 전에 항상 MockMvc 를 만들어준다.
    @Before
    public void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @After
    public void tearDown() throws Exception {
        postsRepository.deleteAll();
    }

    //setting
    String title = "title";
    String content = "content";
    String author = "author";

    @Test
    // @WithMockUser : MockMvc 에서만 사용이 가능하다
    @WithMockUser(roles="USER")
    public void registPosts() throws Exception {

        PostsSaveRequestDto requestDto = PostsSaveRequestDto.builder().title(title).content(content).author(author).build();
        String url = "http://localhost:" + port + "/api/v1/posts";

        //when
        mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                /* object 를 json 형태로 변환
                 * 참고 : https://www.baeldung.com/jackson-object-mapper-tutorial */
                .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        /* 인증되지 않은 사용자를 기준으로 테스트할 때의 로직
        //request & response
        ResponseEntity<Long> responseEntity = restTemplate.postForEntity(url, requestDto, Long.class);

        //check
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isGreaterThan(0L); */

        List<Posts> all = postsRepository.findAll();
        assertThat(all.get(0).getTitle()).isEqualTo(title);
        assertThat(all.get(0).getContent()).isEqualTo(content);

    }

    @Test
    @WithMockUser(roles="USER")
    public void modifyPosts() throws Exception {

        // given
        Posts savedPosts = postsRepository.save(Posts.builder().title(title).content(content).author(author).build());

        Long updateId = savedPosts.getId();
        String expectedTitle = "title2";
        String expectedContent = "content2";

        PostsUpdateRequestDto requestDto = PostsUpdateRequestDto.builder().title(expectedTitle).content(expectedContent).build();

        String url = "http://localhost:" + port + "/api/v1/posts/" + updateId;

        /* 인증되지 않은 사용자를 기준으로 테스트할 때의 로직
        HttpEntity<PostsUpdateRequestDto> requestEntity = new HttpEntity<>(requestDto);

        // when
        ResponseEntity<Long> responseEntity = restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Long.class);

        // then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isGreaterThan(0L); */

        //when
        mvc.perform(put(url)
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(new ObjectMapper().writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        List<Posts> all = postsRepository.findAll();
        assertThat(all.get(0).getTitle()).isEqualTo(expectedTitle);
        assertThat(all.get(0).getContent()).isEqualTo(expectedContent);

    }

}
