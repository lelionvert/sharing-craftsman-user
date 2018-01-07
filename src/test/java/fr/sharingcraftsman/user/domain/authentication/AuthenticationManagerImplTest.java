package fr.sharingcraftsman.user.domain.authentication;

import fr.sharingcraftsman.user.common.DateService;
import fr.sharingcraftsman.user.domain.authentication.ports.AccessTokenRepository;
import fr.sharingcraftsman.user.domain.client.Client;
import fr.sharingcraftsman.user.domain.common.Username;
import fr.sharingcraftsman.user.domain.user.User;
import fr.sharingcraftsman.user.domain.user.CollaboratorBuilder;
import fr.sharingcraftsman.user.domain.user.ports.UserRepository;
import fr.sharingcraftsman.user.domain.authentication.ports.AuthenticationManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDateTime;
import java.time.Month;

import static fr.sharingcraftsman.user.domain.authentication.AccessToken.validTokenBuilder;
import static fr.sharingcraftsman.user.domain.common.Password.passwordBuilder;
import static fr.sharingcraftsman.user.domain.common.Username.usernameBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationManagerImplTest {
  private AuthenticationManager identifier;

  @Mock
  private DateService dateService;

  @Mock
  private UserRepository userRepository;

  @Mock
  private AccessTokenRepository accessTokenRepository;
  private AccessToken oAuthToken;
  private Client client;
  private Credentials credentials;
  private User user;

  @Before
  public void setUp() throws Exception {
    given(dateService.now()).willReturn(LocalDateTime.of(2017, Month.DECEMBER, 25, 12, 0));
    given(dateService.getDayAt(any(Integer.class))).willReturn(LocalDateTime.of(2017, Month.DECEMBER, 25, 12, 0));
    identifier = new AuthenticationManagerImpl(userRepository, accessTokenRepository, dateService);
    oAuthToken = validTokenBuilder
            .withAccessToken("aaa")
            .withRefreshToken("bbb")
            .expiringThe(LocalDateTime.of(2017, Month.DECEMBER, 25, 12, 0))
            .build();
    client = Client.knownClient("client", "secret");
    credentials = Credentials.buildEncryptedCredentials(usernameBuilder.from("john@doe.fr"), passwordBuilder.from("password"), false);
    user = (new CollaboratorBuilder())
            .withUsername(usernameBuilder.from("john@doe.fr"))
            .withPassword(passwordBuilder.from("password"))
            .build();
  }

  @Test
  public void should_generate_token_when_identifying() throws Exception {
    AccessToken token = validTokenBuilder
            .withAccessToken("aaa")
            .withRefreshToken("bbb")
            .expiringThe(dateService.getDayAt(8))
            .build();
    given(userRepository.findUserFromCredentials(any(Credentials.class))).willReturn(user);
    given(accessTokenRepository.createNewToken(any(Client.class), any(User.class), any(AccessToken.class))).willReturn(token);
    credentials.setStayLogged(true);

    BaseToken expectedBaseToken = identifier.login(credentials, client);

    assertThat(expectedBaseToken).isEqualTo(token);
  }

  @Test
  public void should_validate_token() throws Exception {
    given(accessTokenRepository.findTokenFromAccessToken(client, credentials, oAuthToken)).willReturn(oAuthToken);

    boolean isValid = identifier.isTokenValid(credentials, client, oAuthToken);

    assertThat(isValid).isTrue();
  }

  @Test
  public void should_not_validate_access_token_if_does_not_exists() throws Exception {
    given(accessTokenRepository.findTokenFromAccessToken(client, credentials, oAuthToken)).willReturn(new InvalidToken());

    boolean isValid = identifier.isTokenValid(credentials, client, oAuthToken);

    assertThat(isValid).isFalse();
  }

  @Test
  public void should_not_validate_token_if_is_expired() throws Exception {
    AccessToken token = validTokenBuilder
            .withAccessToken("aaa")
            .withRefreshToken("")
            .expiringThe(null)
            .build();
    AccessToken fetchedToken = validTokenBuilder
            .withAccessToken("aaa")
            .withRefreshToken("bbb")
            .expiringThe(LocalDateTime.of(2017, Month.DECEMBER, 10, 12, 0))
            .build();
    given(accessTokenRepository.findTokenFromAccessToken(client, credentials, token)).willReturn(fetchedToken);

    boolean isValid = identifier.isTokenValid(credentials, client, token);

    assertThat(isValid).isFalse();
  }

  @Test
  public void should_delete_token_when_logout() throws Exception {
    given(accessTokenRepository.findTokenFromAccessToken(client, credentials, oAuthToken)).willReturn(oAuthToken);
    given(userRepository.findUserFromUsername(credentials.getUsername())).willReturn(user);

    identifier.logout(credentials, client, oAuthToken);

    verify(accessTokenRepository).deleteTokensOf(user, client);
  }

  @Test
  public void should_validate_refresh_token() throws Exception {
    given(accessTokenRepository.findTokenFromRefreshToken(client, credentials, oAuthToken)).willReturn(oAuthToken);

    boolean isValid = identifier.isRefreshTokenValid(credentials, client, oAuthToken);

    assertThat(isValid).isTrue();
  }

  @Test
  public void should_not_validate_refresh_token_if_does_not_exists() throws Exception {
    given(accessTokenRepository.findTokenFromRefreshToken(client, credentials, oAuthToken)).willReturn(new InvalidToken());

    boolean isValid = identifier.isRefreshTokenValid(credentials, client, oAuthToken);

    assertThat(isValid).isFalse();
  }

  @Test
  public void should_not_validate_refresh_token_if_is_expired() throws Exception {
    AccessToken token = validTokenBuilder
            .withAccessToken("")
            .withRefreshToken("bbb")
            .expiringThe(null)
            .build();
    AccessToken fetchedToken = validTokenBuilder
            .withAccessToken("aaa")
            .withRefreshToken("bbb")
            .expiringThe(LocalDateTime.of(2017, Month.DECEMBER, 10, 12, 0))
            .build();
    given(accessTokenRepository.findTokenFromRefreshToken(client, credentials, token)).willReturn(fetchedToken);

    boolean isValid = identifier.isRefreshTokenValid(credentials, client, token);

    assertThat(isValid).isFalse();
  }

  @Test
  public void should_generate_new_token_when_request_with_refresh_token() throws Exception {
    AccessToken token = validTokenBuilder
            .withAccessToken("aaa")
            .withRefreshToken("bbb")
            .expiringThe(dateService.getDayAt(8))
            .build();
    given(userRepository.findUserFromUsername(any(Username.class))).willReturn(user);
    given(accessTokenRepository.createNewToken(any(Client.class), any(User.class), any(AccessToken.class))).willReturn(token);
    credentials.setStayLogged(true);

    BaseToken expectedBaseToken = identifier.createNewToken(credentials, client);

    assertThat(expectedBaseToken).isEqualTo(token);
  }
}