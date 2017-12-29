package fr.sharingcraftsman.user.domain.company;

import fr.sharingcraftsman.user.domain.authentication.Credentials;
import fr.sharingcraftsman.user.domain.common.Username;

public interface HumanResourceAdministrator {
  void createNewCollaborator(Collaborator collaborator);

  Person getCollaborator(Username username);

  Person findFromCredentials(Credentials credentials);

  void deleteChangePasswordKeyOf(Credentials credentials);

  ChangePasswordKey createChangePasswordKeyFor(ChangePasswordKey changePasswordKey);

  void updateCollaborator(Collaborator collaborator);
}
