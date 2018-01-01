package fr.sharingcraftsman.user.domain.admin;

import fr.sharingcraftsman.user.domain.common.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

public class AdminCollaborator {
  private final Username username;
  private final Password password;
  private final Name firstname;
  private final Name lastname;
  private final Email email;
  private final Link website;
  private final Link github;
  private final Link linkedin;
  private final String changePasswordKey;
  private final LocalDateTime changePasswordKeyExpirationDate;
  private final boolean isActive;
  private final LocalDateTime creationDate;
  private final LocalDateTime lastUpdateDate;

  public AdminCollaborator(String username, String password, String firstname, String lastname, String email, String website, String github, String linkedin, String changePasswordKey, Date changePasswordExpirationDate, boolean active, Date creationDate, Date lastUpdateDate) {
    this.username = new Username(username);
    this.password = new Password(password);
    this.firstname = Name.of(firstname);
    this.lastname = Name.of(lastname);
    this.email = Email.from(email);
    this.website = Link.to(website);
    this.github = Link.to(github);
    this.linkedin = Link.to(linkedin);
    this.changePasswordKey = changePasswordKey;
    this.changePasswordKeyExpirationDate = changePasswordExpirationDate != null ? fromDateToLocalDatetime(changePasswordExpirationDate) : null;
    this.isActive = active;
    this.creationDate = fromDateToLocalDatetime(creationDate);
    this.lastUpdateDate = fromDateToLocalDatetime(lastUpdateDate);
  }

  public String getUsername() {
    return username.getUsername();
  }

  public String getPassword() {
    return password.getPassword();
  }

  public String getFirstname() {
    return firstname.getName();
  }

  public String getLastname() {
    return lastname.getName();
  }

  public String getEmail() {
    return email.getEmail();
  }

  public String getWebsite() {
    return website.getLink();
  }

  public String getGithub() {
    return github.getLink();
  }

  public String getLinkedin() {
    return linkedin.getLink();
  }

  public String getChangePasswordKey() {
    return changePasswordKey;
  }

  public long getChangePasswordKeyExpirationDate() {
    return changePasswordKeyExpirationDate != null ? fromLocalDatetimeToLong(changePasswordKeyExpirationDate) : 0;
  }

  public boolean isActive() {
    return isActive;
  }

  public long getCreationDate() {
    return fromLocalDatetimeToLong(creationDate);
  }

  public long getLastUpdateDate() {
    return fromLocalDatetimeToLong(lastUpdateDate);
  }

  public static AdminCollaborator from(String username, String password, String firstname, String lastname, String email, String website, String github, String linkedin, String changePasswordKey, Date changePasswordExpirationDate, boolean active, Date creationDate, Date lastUpdateDate) {
    return new AdminCollaborator(username, password, firstname, lastname, email, website, github, linkedin, changePasswordKey, changePasswordExpirationDate, active, creationDate, lastUpdateDate);
  }

  private long fromLocalDatetimeToLong(LocalDateTime localDateTime) {
    ZonedDateTime zdt = localDateTime.atZone(ZoneId.systemDefault());
    return zdt.toInstant().toEpochMilli();
  }

  private LocalDateTime fromDateToLocalDatetime(Date date) {
    return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
  }
}
