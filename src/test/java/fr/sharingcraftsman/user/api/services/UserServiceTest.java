package fr.sharingcraftsman.user.api.services;

import fr.sharingcraftsman.user.api.models.ChangePasswordKeyDTO;
import fr.sharingcraftsman.user.api.models.LoginDTO;
import fr.sharingcraftsman.user.api.models.ClientDTO;
import fr.sharingcraftsman.user.api.models.TokenDTO;
import fr.sharingcraftsman.user.common.DateService;
import fr.sharingcraftsman.user.domain.authentication.Credentials;
import fr.sharingcraftsman.user.domain.authentication.InvalidToken;
import fr.sharingcraftsman.user.domain.authentication.TokenAdministrator;
import fr.sharingcraftsman.user.domain.authentication.ValidToken;
import fr.sharingcraftsman.user.domain.client.Client;
import fr.sharingcraftsman.user.domain.client.ClientStock;
import fr.sharingcraftsman.user.domain.company.ChangePasswordKey;
import fr.sharingcraftsman.user.domain.company.Collaborator;
import fr.sharingcraftsman.user.domain.company.HumanResourceAdministrator;
import fr.sharingcraftsman.user.domain.company.UnknownCollaborator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.util.Date;

import static fr.sharingcraftsman.user.domain.authentication.ValidToken.validTokenBuilder;
import static fr.sharingcraftsman.user.domain.common.Password.passwordBuilder;
import static fr.sharingcraftsman.user.domain.common.Username.usernameBuilder;
import static fr.sharingcraftsman.user.domain.company.Collaborator.collaboratorBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {
  @Mock
  private HumanResourceAdministrator humanResourceAdministrator;
  @Mock
  private ClientStock clientStock;
  @Mock
  private DateService dateService;
  @Mock
  private TokenAdministrator tokenAdministrator;

  private UserService userService;
  private ClientDTO clientDTO;
  private ValidToken validToken;

  @Before
  public void setUp() throws Exception {
    clientDTO = new ClientDTO("secret", "clientsecret");
    given(dateService.nowInDate()).willReturn(Date.from(LocalDateTime.of(2017, Month.DECEMBER, 24, 12, 0).atZone(ZoneId.systemDefault()).toInstant()));
    given(dateService.getDayAt(any(Integer.class))).willReturn(LocalDateTime.of(2017, Month.DECEMBER, 27, 12, 0));
    given(dateService.now()).willReturn(LocalDateTime.of(2017, Month.DECEMBER, 26, 12, 0));
    userService = new UserService(humanResourceAdministrator, clientStock, tokenAdministrator, dateService);
    validToken = validTokenBuilder
            .withAccessToken("aaa")
            .withRefreshToken("bbb")
            .expiringThe(dateService.getDayAt(8))
            .build();
  }

  @Test
  public void should_register_user() throws Exception {
    given(clientStock.findClient(any(Client.class))).willReturn(Client.knownClient("client", "clietnsercret"));
    given(humanResourceAdministrator.getCollaborator(usernameBuilder.from("john@doe.fr"))).willReturn(new UnknownCollaborator());
    LoginDTO loginDTO = new LoginDTO("john@doe.fr", "password");

    ResponseEntity response = userService.registerUser(clientDTO, loginDTO);

    verify(humanResourceAdministrator).createNewCollaborator(Collaborator.from(Credentials.buildEncryptedCredentials(usernameBuilder.from("john@doe.fr"), passwordBuilder.from("password"), false)));
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  public void should_get_invalid_credential_username_when_username_is_not_specified() throws Exception {
    given(clientStock.findClient(any(Client.class))).willReturn(Client.knownClient("client", "clietnsercret"));
    LoginDTO loginDTO = new LoginDTO("", "password");

    ResponseEntity response = userService.registerUser(clientDTO, loginDTO);

    verify(humanResourceAdministrator, never()).createNewCollaborator(any(Collaborator.class));
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isEqualTo("Username cannot be empty");
  }

  @Test
  public void should_get_invalid_credential_password_when_username_is_not_specified() throws Exception {
    given(clientStock.findClient(any(Client.class))).willReturn(Client.knownClient("client", "clietnsercret"));
    LoginDTO loginDTO = new LoginDTO("john@doe.fr", "");

    ResponseEntity response = userService.registerUser(clientDTO, loginDTO);

    verify(humanResourceAdministrator, never()).createNewCollaborator(any(Collaborator.class));
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isEqualTo("Password cannot be empty");
  }

  @Test
  public void should_get_user_already_exists_when_using_already_existing_username() throws Exception {
    given(clientStock.findClient(any(Client.class))).willReturn(Client.knownClient("client", "clietnsercret"));
    given(humanResourceAdministrator.getCollaborator(usernameBuilder.from("john@doe.fr"))).willReturn(
            collaboratorBuilder
                    .withUsername(usernameBuilder.from("john@doe.fr"))
                    .withPassword(passwordBuilder.from("password"))
                    .build()
    );
    LoginDTO loginDTO = new LoginDTO("john@doe.fr", "password");

    ResponseEntity response = userService.registerUser(clientDTO, loginDTO);

    verify(humanResourceAdministrator, never()).createNewCollaborator(any(Collaborator.class));
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(response.getBody()).isEqualTo("Collaborator already exists with username: john@doe.fr");
  }

  @Test
  public void should_get_unknown_client_response_when_client_is_not_known() throws Exception {
    given(clientStock.findClient(any(Client.class))).willReturn(Client.unkownClient());
    LoginDTO loginDTO = new LoginDTO("john@doe.fr", "password");

    ResponseEntity response = userService.registerUser(clientDTO, loginDTO);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    assertThat(response.getBody()).isEqualTo("Unknown client");
  }

  @Test
  public void should_get_change_password_token_when_requesting_to_change_password() throws Exception {
    Collaborator collaborator = collaboratorBuilder
            .withUsername(usernameBuilder.from("john@doe.fr"))
            .withPassword(passwordBuilder.from("password"))
            .build();
    ChangePasswordKey key = new ChangePasswordKey(collaborator, "aaa", LocalDateTime.of(2017, 12, 25, 12, 0));
    given(humanResourceAdministrator.createChangePasswordKeyFor(any(ChangePasswordKey.class))).willReturn(key);
    given(clientStock.findClient(any(Client.class))).willReturn(Client.knownClient("client", "clietnsercret"));
    given(tokenAdministrator.findTokenFor(any(Client.class), any(Credentials.class), any(ValidToken.class))).willReturn(validToken);
    TokenDTO tokenDTO = new TokenDTO("john@doe.fr", "aaa");

    ResponseEntity response = userService.requestChangePassword(clientDTO, tokenDTO);

    System.out.println(response);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isEqualTo(new ChangePasswordKeyDTO("aaa"));
  }

  @Test
  public void should_get_unauthorized_if_access_token_is_invalid_when_requesting_password_change() throws Exception {
    given(clientStock.findClient(any(Client.class))).willReturn(Client.knownClient("client", "clietnsercret"));
    given(tokenAdministrator.findTokenFor(any(Client.class), any(Credentials.class), any(ValidToken.class))).willReturn(new InvalidToken());
    TokenDTO tokenDTO = new TokenDTO("john@doe.fr", "aaa");

    ResponseEntity response = userService.requestChangePassword(clientDTO, tokenDTO);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }
}
