package fr.sharingcraftsman.user.domain.company;

import fr.sharingcraftsman.user.domain.common.Username;

public interface HumanResourceAdministrator {
  void createNewCollaborator(Collaborator collaborator);

  Person getCollaborator(Username username);
}
