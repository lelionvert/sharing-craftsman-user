package fr.sharingcraftsman.user.domain.company;

import fr.sharingcraftsman.user.common.DateService;
import fr.sharingcraftsman.user.domain.authentication.Credentials;
import fr.sharingcraftsman.user.domain.authentication.CredentialsException;
import fr.sharingcraftsman.user.domain.common.Username;
import fr.sharingcraftsman.user.domain.ports.company.Company;
import fr.sharingcraftsman.user.domain.utils.Crypter;
import fr.sharingcraftsman.user.domain.utils.CrypterFactory;

public class Organisation implements Company {
  public static Crypter crypter = CrypterFactory.getCrypter();
  private HumanResourceAdministrator humanResourceAdministrator;
  private DateService dateService;

  public Organisation(HumanResourceAdministrator humanResourceAdministrator, DateService dateService) {
    this.humanResourceAdministrator = humanResourceAdministrator;
    this.dateService = dateService;
  }

  @Override
  public void createNewCollaborator(Credentials credentials) throws CollaboratorException, CredentialsException {
    if (collaboratorExists(credentials.getUsername()))
      throw new AlreadyExistingCollaboratorException("Collaborator already exists with username: " + credentials.getUsernameContent());

    Credentials encryptedCredentials = Credentials.buildEncryptedCredentials(credentials.getUsername(), credentials.getPassword(), credentials.stayLogged());
    Collaborator newCollaborator = Collaborator.from(encryptedCredentials);
    humanResourceAdministrator.createNewCollaborator(newCollaborator);
  }

  @Override
  public ChangePasswordKey createChangePasswordKeyFor(Credentials credentials) {
    humanResourceAdministrator.deleteChangePasswordKeyOf(credentials);
    ChangePasswordKey changePasswordKey = ChangePasswordKey.from(
            (new CollaboratorBuilder())
                    .withUsername(credentials.getUsername())
                    .withPassword(null)
                    .build(),
            crypter.encrypt(credentials.getUsernameContent()),
            dateService.getDayAt(1)
    );
    return humanResourceAdministrator.createChangePasswordKeyFor(changePasswordKey);
  }

  @Override
  public void changePassword(Credentials credentials, ChangePassword changePassword) throws CollaboratorException {
    Person person = humanResourceAdministrator.findFromCredentials(credentials);

    if (!person.isKnown())
      throw new UnknownCollaboratorException("Unknown collaborator");

    checkChangePasswordKeyValidity(changePassword, (Collaborator) person);

    ((Collaborator) person).setPassword(changePassword.getNewPassword().getEncryptedVersion());
    humanResourceAdministrator.updateCollaborator((Collaborator) person);
    humanResourceAdministrator.deleteChangePasswordKeyOf(credentials);
  }

  private void checkChangePasswordKeyValidity(ChangePassword changePassword, Collaborator person) throws InvalidChangePasswordKeyException {
    if (!changePassword.getChangePasswordKey().equals(person.getChangePasswordKey()) || person.getChangePasswordKeyExpirationDate().isBefore(dateService.now()))
      throw new InvalidChangePasswordKeyException("Invalid token to change password");
  }

  private boolean collaboratorExists(Username username) {
    return humanResourceAdministrator.getCollaborator(username).isKnown();
  }
}
