package fr.sharingcraftsman.user.api.authentication;

import fr.sharingcraftsman.user.api.client.ClientDTO;
import fr.sharingcraftsman.user.api.common.AuthorizationVerifierService;
import fr.sharingcraftsman.user.common.DateConverter;
import fr.sharingcraftsman.user.common.DateService;
import fr.sharingcraftsman.user.domain.authentication.AccessToken;
import fr.sharingcraftsman.user.domain.authentication.Credentials;
import fr.sharingcraftsman.user.domain.authentication.InvalidToken;
import fr.sharingcraftsman.user.domain.authentication.ports.AccessTokenRepository;
import fr.sharingcraftsman.user.domain.client.Client;
import fr.sharingcraftsman.user.domain.common.Username;
import fr.sharingcraftsman.user.domain.user.User;
import fr.sharingcraftsman.user.domain.user.ports.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationServiceTest {
  @Mock
  private UserRepository userRepository;
  @Mock
  private AuthorizationVerifierService authorizationVerifierService;
  @Mock
  private AccessTokenRepository accessTokenRepository;
  @Mock
  private DateService dateService;

  private AuthenticationService authenticationService;
  private ClientDTO clientDTO;
  private AccessToken validToken;
  private TokenDTO token;
  private User user;

  @Before
  public void setUp() throws Exception {
    given(dateService.nowInDate()).willReturn(DateConverter.fromLocalDateTimeToDate(LocalDateTime.of(2017, Month.DECEMBER, 24, 12, 0)));
    given(dateService.getDayAt(any(Integer.class))).willReturn(LocalDateTime.of(2017, Month.DECEMBER, 30, 12, 0));
    given(authorizationVerifierService.isUnauthorizedClient(any(ClientDTO.class))).willReturn(false);

    authenticationService = new AuthenticationService(userRepository, accessTokenRepository, dateService, authorizationVerifierService);
    clientDTO = ClientDTO.from("client", "secret");
    validToken = AccessToken.from("aaa", "bbb", dateService.getDayAt(8));
    token = TokenDTO.from("john@doe.fr", "aaa");
    user = User.from("john@doe.fr", "T49xWf/l7gatvfVwethwDw==");
  }

  @Test
  public void should_login_user() throws Exception {
    given(userRepository.findUserFromCredentials(any(Credentials.class))).willReturn(user);
    given(dateService.getDayAt(any(Integer.class))).willReturn(LocalDateTime.of(2017, Month.DECEMBER, 25, 12, 0));
    given(accessTokenRepository.createNewToken(any(Client.class), any(User.class), any(AccessToken.class))).willReturn(validToken);

    ResponseEntity response = authenticationService.login(clientDTO, LoginDTO.from("john@doe.fr", "password", true));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void should_return_ok_if_token_is_valid() throws Exception {
    given(accessTokenRepository.findTokenFromAccessToken(any(Client.class), any(Username.class), any(AccessToken.class))).willReturn(validToken);
    given(dateService.now()).willReturn(LocalDateTime.of(2017, Month.DECEMBER, 25, 12, 0));

    ResponseEntity response = authenticationService.checkToken(clientDTO, token);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void should_get_unauthorized_if_token_is_not_valid() throws Exception {
    given(accessTokenRepository.findTokenFromAccessToken(any(Client.class), any(Username.class), any(AccessToken.class))).willReturn(new InvalidToken());

    ResponseEntity response = authenticationService.checkToken(clientDTO, token);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void should_return_unauthorized_if_token_has_expired() throws Exception {
    given(accessTokenRepository.findTokenFromAccessToken(any(Client.class), any(Username.class), any(AccessToken.class))).willReturn(validToken);
    given(dateService.now()).willReturn(LocalDateTime.of(2018, Month.JANUARY, 12, 12, 0));

    ResponseEntity response = authenticationService.checkToken(clientDTO, token);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  public void should_return_ok_when_logout() throws Exception {
    given(accessTokenRepository.findTokenFromAccessToken(any(Client.class), any(Username.class), any(AccessToken.class))).willReturn(validToken);
    given(dateService.now()).willReturn(LocalDateTime.of(2017, Month.DECEMBER, 25, 12, 0));

    ResponseEntity response = authenticationService.logout(clientDTO, token);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void should_generate_new_token_from_refresh_token() throws Exception {
    given(userRepository.findUserFromUsername(Username.from("john@doe.fr"))).willReturn(user);
    given(dateService.getDayAt(any(Integer.class))).willReturn(LocalDateTime.of(2017, Month.DECEMBER, 25, 12, 0));
    given(accessTokenRepository.createNewToken(any(Client.class), any(User.class), any(AccessToken.class))).willReturn(validToken);
    given(accessTokenRepository.findTokenFromRefreshToken(any(Client.class), any(Username.class), any(AccessToken.class))).willReturn(validToken);
    given(dateService.now()).willReturn(LocalDateTime.of(2017, Month.DECEMBER, 25, 12, 0));

    ResponseEntity response = authenticationService.refreshToken(clientDTO, TokenDTO.from("john@doe.fr", "", "bbb"));

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(TokenDTO.from("john@doe.fr", "aaa", "bbb", 1514631600000L));
    verify(accessTokenRepository).deleteTokensOf(any(User.class), any(Client.class));
  }
}
