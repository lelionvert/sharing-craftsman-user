package fr.sharingcraftsman.user.domain.authentication;

import fr.sharingcraftsman.user.common.DateService;
import fr.sharingcraftsman.user.domain.authentication.ports.AccessTokenRepository;
import fr.sharingcraftsman.user.domain.client.Client;
import fr.sharingcraftsman.user.domain.user.*;
import fr.sharingcraftsman.user.domain.authentication.ports.AuthenticationManager;
import fr.sharingcraftsman.user.domain.user.exceptions.UnknownUserException;
import fr.sharingcraftsman.user.domain.user.exceptions.UserException;
import fr.sharingcraftsman.user.domain.user.ports.UserRepository;

import java.security.SecureRandom;
import java.util.Base64;

import static fr.sharingcraftsman.user.domain.authentication.AccessToken.validTokenBuilder;

public class AuthenticationManagerImpl implements AuthenticationManager {
  private final int LONG_VALIDITY_OFFSET = 8;
  private final int SHORT_VALIDITY_OFFSET = 1;

  private UserRepository userRepository;
  private AccessTokenRepository accessTokenRepository;
  private DateService dateService;

  public AuthenticationManagerImpl(UserRepository userRepository, AccessTokenRepository accessTokenRepository, DateService dateService) {
    this.userRepository = userRepository;
    this.accessTokenRepository = accessTokenRepository;
    this.dateService = dateService;
  }

  @Override
  public BaseToken login(Credentials credentials, Client client) throws UserException {
    BaseUser baseUser = userRepository.findUserFromCredentials(credentials);
    verifyCollaboratorIsKnown(baseUser);
    User user = (User) baseUser;
    accessTokenRepository.deleteTokensOf(user, client);
    return generateToken(credentials, client, user);
  }

  @Override
  public boolean isTokenValid(Credentials credentials, Client client, AccessToken token) {
    BaseToken foundBaseToken = accessTokenRepository.findTokenFromAccessToken(client, credentials, token);
    return verifyTokenValidity(foundBaseToken);
  }

  @Override
  public void logout(Credentials credentials, Client client, AccessToken token) {
    if (isTokenValid(credentials, client, token)) {
      deleteToken(credentials, client);
    }
  }

  @Override
  public boolean isRefreshTokenValid(Credentials credentials, Client client, AccessToken token) {
    BaseToken foundBaseToken = accessTokenRepository.findTokenFromRefreshToken(client, credentials, token);
    return verifyTokenValidity(foundBaseToken);
  }

  @Override
  public void deleteToken(Credentials credentials, Client client, AccessToken token) {
    deleteToken(credentials, client);
  }

  @Override
  public BaseToken createNewToken(Credentials credentials, Client client) throws UserException {
    BaseUser baseUser = userRepository.findUserFromUsername(credentials.getUsername());
    verifyCollaboratorIsKnown(baseUser);
    User user = (User) baseUser;
    return generateToken(credentials, client, user);
  }

  private void verifyCollaboratorIsKnown(BaseUser baseUser) throws UnknownUserException {
    if (!baseUser.isKnown())
      throw new UnknownUserException("Unknown collaborator");
  }

  private BaseToken generateToken(Credentials credentials, Client client, User user) {
    AccessToken token = validTokenBuilder
            .withAccessToken(generateKey(client.getName() + user.getUsername()))
            .withRefreshToken(generateKey(client.getName() + user.getUsername()))
            .expiringThe(dateService.getDayAt(credentials.stayLogged() ? LONG_VALIDITY_OFFSET : SHORT_VALIDITY_OFFSET))
            .build();

    return accessTokenRepository.createNewToken(client, user, token);
  }

  private void deleteToken(Credentials credentials, Client client) {
    User user = (User) userRepository.findUserFromUsername(credentials.getUsername());
    accessTokenRepository.deleteTokensOf(user, client);
  }

  private boolean verifyTokenValidity(BaseToken foundBaseToken) {
    if (foundBaseToken.isValid()) {
      AccessToken validToken = (AccessToken) foundBaseToken;
      if (validToken.getExpirationDate().isBefore(dateService.now()))
        return false;
    }

    return foundBaseToken.isValid();
  }

  private String generateKey(String seed) {
    SecureRandom random = new SecureRandom(seed.getBytes());
    byte bytes[] = new byte[96];
    random.nextBytes(bytes);
    Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
    return encoder.encodeToString(bytes);
  }
}