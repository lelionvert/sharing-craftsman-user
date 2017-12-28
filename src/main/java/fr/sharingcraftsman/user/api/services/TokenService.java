package fr.sharingcraftsman.user.api.services;

import fr.sharingcraftsman.user.api.models.Login;
import fr.sharingcraftsman.user.api.models.OAuthToken;
import fr.sharingcraftsman.user.api.pivots.ClientPivot;
import fr.sharingcraftsman.user.api.pivots.LoginPivot;
import fr.sharingcraftsman.user.api.pivots.TokenPivot;
import fr.sharingcraftsman.user.common.DateService;
import fr.sharingcraftsman.user.domain.authentication.*;
import fr.sharingcraftsman.user.domain.client.Client;
import fr.sharingcraftsman.user.domain.client.ClientAdministrator;
import fr.sharingcraftsman.user.domain.client.ClientStock;
import fr.sharingcraftsman.user.domain.company.CollaboratorException;
import fr.sharingcraftsman.user.domain.company.HumanResourceAdministrator;
import fr.sharingcraftsman.user.domain.ports.authentication.Authenticator;
import fr.sharingcraftsman.user.domain.ports.client.ClientManager;
import fr.sharingcraftsman.user.domain.utils.SimpleSecretGenerator;
import fr.sharingcraftsman.user.infrastructure.adapters.ClientAdapter;
import fr.sharingcraftsman.user.infrastructure.adapters.TokenAdapter;
import fr.sharingcraftsman.user.infrastructure.adapters.UserAdapter;
import fr.sharingcraftsman.user.infrastructure.repositories.ClientRepository;
import fr.sharingcraftsman.user.infrastructure.repositories.TokenRepository;
import fr.sharingcraftsman.user.infrastructure.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import static fr.sharingcraftsman.user.domain.common.Username.usernameBuilder;

@Service
public class TokenService {
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private ClientManager clientManager;
  private Authenticator authenticator;

  @Autowired
  public TokenService(UserRepository userRepository, TokenRepository tokenRepository, ClientRepository clientRepository, DateService dateService) {
    HumanResourceAdministrator humanResourceAdministrator = new UserAdapter(userRepository, dateService);
    TokenAdministrator tokenAdministrator = new TokenAdapter(tokenRepository);
    authenticator = new OAuthAuthenticator(humanResourceAdministrator, tokenAdministrator, dateService);

    ClientStock clientStock = new ClientAdapter(clientRepository);
    clientManager = new ClientAdministrator(clientStock, new SimpleSecretGenerator());
  }

  public ResponseEntity login(Login login) {
    if (!clientManager.clientExists(ClientPivot.fromApiToDomain(login))) {
      log.warn("User " + login.getUsername() + " is trying to log in with unauthorized client: " + login.getClient());
      return new ResponseEntity<>("Unknown client", HttpStatus.UNAUTHORIZED);
    }

    try {
      log.info("User " + login.getUsername() + " is logging");
      Credentials credentials = LoginPivot.fromApiToDomainWithEncryption(login);
      Client client = ClientPivot.fromApiToDomain(login);
      OAuthToken token = TokenPivot.fromDomainToApi((ValidToken) authenticator.login(credentials, client), credentials);
      return ResponseEntity.ok(token);
    } catch (CredentialsException | CollaboratorException e) {
      log.warn("Error with login " + login.getUsername() + ": " + e.getMessage());
      return ResponseEntity
              .badRequest()
              .body(e.getMessage());
    }
  }

  public ResponseEntity checkToken(OAuthToken token) {
    try {
      log.info("Validating token of " + token.getUsername() + " with value " + token.getAccessToken());
      Credentials credentials = Credentials.buildCredentials(usernameBuilder.from(token.getUsername()), null, false);
      Client client = new Client(token.getClient(), "", false);

      if (authenticator.isTokenValid(credentials, client, TokenPivot.fromApiToDomain(token))) {
        return ResponseEntity.ok().build();
      } else {
        return new ResponseEntity<>("Invalid token", HttpStatus.UNAUTHORIZED);
      }
    } catch (CredentialsException e) {
      log.warn("Error with check token " + token.getUsername() + ": " + e.getMessage());
      return ResponseEntity
              .badRequest()
              .body(e.getMessage());
    }
  }
}