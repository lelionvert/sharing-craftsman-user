package fr.sharingcraftsman.user.api.services;

import fr.sharingcraftsman.user.api.models.AdminUserDTO;
import fr.sharingcraftsman.user.api.models.ClientDTO;
import fr.sharingcraftsman.user.api.models.TokenDTO;
import fr.sharingcraftsman.user.api.pivots.AuthorizationPivot;
import fr.sharingcraftsman.user.api.pivots.ClientPivot;
import fr.sharingcraftsman.user.domain.admin.AdminCollaborator;
import fr.sharingcraftsman.user.domain.admin.HRAdminManager;
import fr.sharingcraftsman.user.domain.admin.OrganisationAdmin;
import fr.sharingcraftsman.user.domain.authentication.Credentials;
import fr.sharingcraftsman.user.domain.authentication.CredentialsException;
import fr.sharingcraftsman.user.domain.authorization.*;
import fr.sharingcraftsman.user.domain.client.ClientAdministrator;
import fr.sharingcraftsman.user.domain.client.ClientStock;
import fr.sharingcraftsman.user.domain.common.Username;
import fr.sharingcraftsman.user.domain.ports.admin.CompanyAdmin;
import fr.sharingcraftsman.user.domain.ports.authorization.Authorizer;
import fr.sharingcraftsman.user.domain.ports.client.ClientManager;
import fr.sharingcraftsman.user.domain.utils.SimpleSecretGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static fr.sharingcraftsman.user.domain.common.Username.usernameBuilder;

@Service
public class AdminService {
  private final Logger log = LoggerFactory.getLogger(this.getClass());
  private ClientManager clientManager;
  private CompanyAdmin company;
  private Authorizer authorizer;

  @Autowired
  public AdminService(
          HRAdminManager hrAdminManager,
          ClientStock clientStock,
          GroupAdministrator groupAdministrator,
          RoleAdministrator roleAdministrator) {
    clientManager = new ClientAdministrator(clientStock, new SimpleSecretGenerator());
    company = new OrganisationAdmin(hrAdminManager);
    authorizer = new GroupRoleAuthorizer(groupAdministrator, roleAdministrator);
  }

  public ResponseEntity getUsers(ClientDTO clientDTO, TokenDTO tokenDTO) {
    if (isAuthorizedClient(clientDTO, tokenDTO)) return new ResponseEntity<>("Unknown client", HttpStatus.UNAUTHORIZED);

    HttpStatus isAdmin = isAdmin(tokenDTO);
    if (!isAdmin.equals(HttpStatus.OK)) return new ResponseEntity<>("Unauthorized user", isAdmin);

    List<AdminCollaborator> collaborators = company.getAllCollaborators();
    List<AdminUserDTO> users = collaborators.stream()
            .map(collaborator -> new AdminUserDTO(
                    collaborator.getUsername(),
                    collaborator.getPassword(),
                    collaborator.getFirstname(),
                    collaborator.getLastname(),
                    collaborator.getEmail(),
                    collaborator.getWebsite(),
                    collaborator.getGithub(),
                    collaborator.getLinkedin(),
                    collaborator.getChangePasswordKey(),
                    collaborator.getChangePasswordKeyExpirationDate(),
                    collaborator.isActive(),
                    collaborator.getCreationDate(),
                    collaborator.getLastUpdateDate()
            ))
            .collect(Collectors.toList());
    users.forEach(user -> {
      Credentials credentials = Credentials.buildCredentials(new Username(user.getUsername()), null, false);
      Authorizations authorizations = authorizer.getAuthorizationsOf(credentials);
      user.setAuthorizations(AuthorizationPivot.fromDomainToApi(authorizations));
    });

    return ResponseEntity.ok(users);
  }

  private boolean isAuthorizedClient(ClientDTO clientDTO, TokenDTO tokenDTO) {
    if (!clientManager.clientExists(ClientPivot.fromApiToDomain(clientDTO))) {
      log.warn("User " + tokenDTO.getUsername() + " is trying to access restricted admin area with client: " + clientDTO.getName());
      return true;
    }
    return false;
  }

  private HttpStatus isAdmin(TokenDTO tokenDTO) {
    try {
      Credentials credentials = Credentials.buildCredentials(usernameBuilder.from(tokenDTO.getUsername()), null, false);
      Authorizations requesterAuthorizations = authorizer.getAuthorizationsOf(credentials);

      Optional<Group> adminGroup = requesterAuthorizations.getGroups()
              .stream()
              .filter(group -> group.getName().contains("ADMIN"))
              .findAny();
      if (adminGroup.isPresent()) {
        if (adminGroup.get()
                .getRoles()
                .stream()
                .noneMatch(role -> role.getRole().contains("ADMIN"))) {
          return HttpStatus.UNAUTHORIZED;
        }
      } else {
        return HttpStatus.UNAUTHORIZED;
      }
    } catch (CredentialsException e) {
      log.warn("Error with getting authorizations " + tokenDTO.getUsername() + ": " + e.getMessage());
      return HttpStatus.BAD_REQUEST;
    }
    return HttpStatus.OK;
  }
}
