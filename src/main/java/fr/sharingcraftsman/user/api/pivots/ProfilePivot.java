package fr.sharingcraftsman.user.api.pivots;

import fr.sharingcraftsman.user.api.models.ProfileDTO;
import fr.sharingcraftsman.user.domain.common.Email;
import fr.sharingcraftsman.user.domain.common.Link;
import fr.sharingcraftsman.user.domain.common.Name;
import fr.sharingcraftsman.user.domain.common.UsernameException;
import fr.sharingcraftsman.user.domain.company.KnownProfile;
import fr.sharingcraftsman.user.domain.company.Profile;
import fr.sharingcraftsman.user.domain.company.ProfileBuilder;

import static fr.sharingcraftsman.user.domain.common.Username.usernameBuilder;

public class ProfilePivot {
  public static Profile fromApiToDomain(String username, ProfileDTO profileDTO) throws UsernameException {
    return new ProfileBuilder()
            .withUsername(usernameBuilder.from(username))
            .withFirstname(Name.of(profileDTO.getFirstname()))
            .withLastname(Name.of(profileDTO.getLastname()))
            .withEmail(Email.from(profileDTO.getEmail()))
            .withWebsite(Link.to(profileDTO.getWebsite()))
            .withGithub(Link.to(profileDTO.getGithub()))
            .withLinkedin(Link.to(profileDTO.getLinkedin()))
            .build();
  }

  public static ProfileDTO fromDomainToApi(KnownProfile profile) {
    return new ProfileDTO(
            profile.getFirstnameContent(),
            profile.getLastnameContent(),
            profile.getEmailContent(),
            profile.getWebsiteContent(),
            profile.getGithubContent(),
            profile.getLinkedinContent()
    );
  }
}
