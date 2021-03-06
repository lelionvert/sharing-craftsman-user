package fr.sharingcraftsman.user.api.admin;

import fr.sharingcraftsman.user.api.authentication.TokenDTO;
import fr.sharingcraftsman.user.api.client.ClientDTO;
import fr.sharingcraftsman.user.api.common.AuthorizationVerifierService;
import fr.sharingcraftsman.user.domain.authorization.UserAuthorizationManagerImpl;
import fr.sharingcraftsman.user.domain.authorization.Groups;
import fr.sharingcraftsman.user.domain.authorization.ports.UserAuthorizationManager;
import fr.sharingcraftsman.user.domain.authorization.ports.AuthorizationRepository;
import fr.sharingcraftsman.user.domain.authorization.ports.UserAuthorizationRepository;
import fr.sharingcraftsman.user.domain.common.Username;
import fr.sharingcraftsman.user.domain.common.UsernameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UserAuthorizationAdminService {
  protected final Logger log = LoggerFactory.getLogger(this.getClass());
  private UserAuthorizationManager userAuthorizationManager;
  private AuthorizationVerifierService authorizationVerifierService;

  @Autowired
  public UserAuthorizationAdminService(
          UserAuthorizationRepository userAuthorizationRepository,
          AuthorizationRepository authorizationRepository,
          AuthorizationVerifierService authorizationVerifierService) {
    userAuthorizationManager = new UserAuthorizationManagerImpl(userAuthorizationRepository, authorizationRepository);
    this.authorizationVerifierService = authorizationVerifierService;
  }

  ResponseEntity addGroupToUser(ClientDTO clientDTO, TokenDTO tokenDTO, UserGroupDTO userGroupDTO) {
    log.info("[UserAuthorizationAdminService::addGroupToUser] Client: " + clientDTO.getName() + ", Token: " + tokenDTO.getUsername() + ", User: " + userGroupDTO.getUsername() + ", Group: " + userGroupDTO.getGroup());

    ResponseEntity isUnauthorized = authorizationVerifierService.isUnauthorizedAdmin(clientDTO, tokenDTO);
    if (isUnauthorized != null) return isUnauthorized;

    try {
      userAuthorizationManager.addGroupToUser(Username.from(userGroupDTO.getUsername()), Groups.valueOf(userGroupDTO.getGroup()));
      return ResponseEntity.ok().build();
    } catch (UsernameException e) {
      return logAndSendBadRequest(e);
    }
  }

  ResponseEntity removeGroupToUser(ClientDTO clientDTO, TokenDTO tokenDTO, UserGroupDTO userGroupDTO) {
    log.info("[UserAuthorizationAdminService::removeGroupToUser] Client: " + clientDTO.getName() + ", Token: " + tokenDTO.getUsername() + ", User: " + userGroupDTO.getUsername() + ", Group: " + userGroupDTO.getGroup());

    ResponseEntity isUnauthorized = authorizationVerifierService.isUnauthorizedAdmin(clientDTO, tokenDTO);
    if (isUnauthorized != null) return isUnauthorized;

    try {
      userAuthorizationManager.removeGroupFromUser(Username.from(userGroupDTO.getUsername()), Groups.valueOf(userGroupDTO.getGroup()));
      return ResponseEntity.ok().build();
    } catch (UsernameException e) {
      return logAndSendBadRequest(e);
    }
  }

  private ResponseEntity logAndSendBadRequest(UsernameException e) {
    log.warn("Error: " + e.getMessage());
    return ResponseEntity
            .badRequest()
            .body(e.getMessage());
  }
}
