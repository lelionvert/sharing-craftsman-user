package fr.sharingcraftsman.user.api.controllers;

import fr.sharingcraftsman.user.UserApplication;
import fr.sharingcraftsman.user.api.models.Login;
import fr.sharingcraftsman.user.api.models.OAuthToken;
import fr.sharingcraftsman.user.api.services.LoginService;
import fr.sharingcraftsman.user.utils.Mapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;
import java.time.Month;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {UserApplication.class})
@WebMvcTest(LoginController.class)
@TestPropertySource(locations = "classpath:application-test.properties")
public class LoginControllerTest {
  @Autowired
  private MockMvc mvc;

  @Autowired
  private WebApplicationContext context;

  @MockBean
  private LoginService loginService;

  @Before
  public void setup() {
    this.mvc = MockMvcBuilders
            .webAppContextSetup(context)
            .build();
  }

  @Test
  public void should_log_in_and_get_token() throws Exception {
    OAuthToken oAuthToken = new OAuthToken("aaa", "bbb", LocalDateTime.of(2018, Month.JANUARY, 2, 12, 0));
    given(loginService.login(any(Login.class))).willReturn(ResponseEntity.ok(oAuthToken));

    Login login = new Login("client", "clientSecret", "john@doe.fr", "password", true);

    this.mvc.perform(post("/users/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(Mapper.fromObjectToJsonString(login)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken", not(empty())));
  }
}
