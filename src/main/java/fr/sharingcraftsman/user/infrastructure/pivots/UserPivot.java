package fr.sharingcraftsman.user.infrastructure.pivots;

import fr.sharingcraftsman.user.domain.admin.UserForAdmin;
import fr.sharingcraftsman.user.domain.authentication.exceptions.CredentialsException;
import fr.sharingcraftsman.user.domain.common.Email;
import fr.sharingcraftsman.user.domain.common.Link;
import fr.sharingcraftsman.user.domain.common.Name;
import fr.sharingcraftsman.user.domain.common.UsernameException;
import fr.sharingcraftsman.user.domain.user.Profile;
import fr.sharingcraftsman.user.domain.user.User;
import fr.sharingcraftsman.user.infrastructure.models.UserEntity;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

import static fr.sharingcraftsman.user.domain.common.Password.passwordBuilder;
import static fr.sharingcraftsman.user.domain.common.Username.usernameBuilder;

public class UserPivot {
  public static UserEntity fromDomainToInfra(User user) {
    return new UserEntity(user.getUsername(), user.getPassword());
  }

  public static User fromInfraToDomain(UserEntity userEntity) throws CredentialsException {
    String changePasswordKey = userEntity.getChangePasswordKey() != null ? userEntity.getChangePasswordKey() : "";
    LocalDateTime changePasswordKeyExpirationDate = userEntity.getChangePasswordExpirationDate() != null ? LocalDateTime.ofInstant(userEntity.getChangePasswordExpirationDate().toInstant(), ZoneId.systemDefault()) : null;

    return User.from(
            usernameBuilder.from(userEntity.getUsername()),
            userEntity.getPassword() != null ? passwordBuilder.from(userEntity.getPassword()) : null,
            changePasswordKey,
            changePasswordKeyExpirationDate);
  }

  public static UserEntity fromDomainToInfra(UserForAdmin collaborator) {
    return new UserEntity(
            collaborator.getUsernameContent(),
            collaborator.getFirstname(),
            collaborator.getLastname(),
            collaborator.getEmail(),
            collaborator.getWebsite(),
            collaborator.getGithub(),
            collaborator.getLinkedin()
    );
  }

  public static List<UserForAdmin> fromInfraToAdminDomain(List<UserEntity> userEntities) {
    return userEntities.stream()
            .map(user -> UserForAdmin.from(
                    user.getUsername(),
                    user.getPassword(),
                    user.getFirstname(),
                    user.getLastname(),
                    user.getEmail(),
                    user.getWebsite(),
                    user.getGithub(),
                    user.getLinkedin(),
                    user.getChangePasswordKey(),
                    user.getChangePasswordExpirationDate(),
                    user.isActive(),
                    user.getCreationDate(),
                    user.getLastUpdateDate()
            ))
            .collect(Collectors.toList());
  }

  public static UserForAdmin fromInfraToAdminDomain(UserEntity userEntity) {
    return UserForAdmin.from(
            userEntity.getUsername(),
            userEntity.getPassword(),
            userEntity.getFirstname(),
            userEntity.getLastname(),
            userEntity.getEmail(),
            userEntity.getWebsite(),
            userEntity.getGithub(),
            userEntity.getLinkedin(),
            userEntity.getChangePasswordKey(),
            userEntity.getChangePasswordExpirationDate(),
            userEntity.isActive(),
            userEntity.getCreationDate(),
            userEntity.getLastUpdateDate());
  }

  public static Profile fromInfraToDomainProfile(UserEntity userEntity) throws UsernameException {
    return Profile.from(
            usernameBuilder.from(userEntity.getUsername()),
            Name.of(userEntity.getFirstname()),
            Name.of(userEntity.getLastname()),
            Email.from(userEntity.getEmail()),
            Link.to(userEntity.getWebsite()),
            Link.to(userEntity.getGithub()),
            Link.to(userEntity.getLinkedin()));
  }

  public static UserEntity fromDomainToInfraProfile(Profile profile) {
    return new UserEntity(profile.getUsernameContent(), profile.getFirstnameContent(), profile.getLastnameContent(), profile.getEmailContent(), profile.getWebsiteContent(), profile.getGithubContent(), profile.getLinkedinContent());
  }
}
