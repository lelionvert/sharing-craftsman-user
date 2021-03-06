package fr.sharingcraftsman.user.domain.user;

import fr.sharingcraftsman.user.domain.common.*;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@EqualsAndHashCode
@ToString
public class Profile extends AbstractProfile {
  private Username username;
  private Name firstname;
  private Name lastname;
  private Email email;
  private Link website;
  private Link github;
  private Link linkedin;
  private Name picture;

  private Profile(Username username, Name firstname, Name lastname, Email email, Link website, Link github, Link linkedin, Name picture) {
    this.username = username;
    this.firstname = firstname;
    this.lastname = lastname;
    this.email = email;
    this.website = website;
    this.github = github;
    this.linkedin = linkedin;
    this.picture = picture;
  }

  public Username getUsername() {
    return username;
  }

  public String getUsernameContent() {
    return username.getUsername();
  }

  public String getFirstnameContent() {
    return firstname.getName();
  }

  public String getLastnameContent() {
    return lastname.getName();
  }

  public Email getEmail() {
    return email;
  }

  public String getEmailContent() {
    return email.getEmail();
  }

  public String getWebsiteContent() {
    return website.getLink();
  }

  public String getGithubContent() {
    return github.getLink();
  }

  public String getLinkedinContent() {
    return linkedin.getLink();
  }

  public String getPictureContent() {
    return picture.getName();
  }

  List<ValidationError> validate() {
    List<ValidationError> errors = new ArrayList<>();

    if (!email.isValid())
      errors.add(new ValidationError("Invalid email"));

    return errors;
  }

  public void updateFields(Profile profile) {
    firstname = profile.firstname;
    lastname = profile.lastname;
    email = profile.email;
    website = profile.website;
    github = profile.github;
    linkedin = profile.linkedin;
    picture = profile.picture;
  }

  @Override
  public boolean isKnown() {
    return true;
  }

  public static Profile from(Username username, Name firstname, Name lastname, Email email, Link website, Link github, Link linkedin, Name picture) {
    return new Profile(username, firstname, lastname, email, website, github, linkedin, picture);
  }
}
