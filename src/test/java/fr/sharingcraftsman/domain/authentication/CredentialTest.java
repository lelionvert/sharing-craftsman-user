package fr.sharingcraftsman.domain.authentication;

import fr.sharingcraftsman.user.domain.authentication.CredentialsException;
import fr.sharingcraftsman.user.domain.authentication.Credentials;
import fr.sharingcraftsman.user.domain.utils.AESCrypter;
import fr.sharingcraftsman.user.domain.utils.Crypter;
import org.junit.Before;
import org.junit.Test;

import static fr.sharingcraftsman.user.domain.common.Password.passwordBuilder;
import static fr.sharingcraftsman.user.domain.common.Username.usernameBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class CredentialTest {
  private Crypter crypter;

  @Before
  public void setUp() throws Exception {
    crypter = new AESCrypter();
  }

  @Test
  public void should_crypt_password_when_created() throws Exception {
    Credentials credentials = Credentials.buildEncryptedCredentials(usernameBuilder.from("john@doe.fr"), passwordBuilder.from("password"));

    assertThat(credentials.getPassword()).isEqualTo(passwordBuilder.from("T49xWf/l7gatvfVwethwDw=="));
  }

  @Test
  public void should_throw_username_exception_when_username_is_empty() throws Exception {
    try {
      Credentials credentials = Credentials.buildEncryptedCredentials(usernameBuilder.from(""), passwordBuilder.from("john@doe.fr"));
      fail("Should have thrown a UsernameException");
    } catch (CredentialsException e) {
      assertThat(e.getMessage()).isEqualTo("Username cannot be empty");
    }
  }

  @Test
  public void should_throw_password_exception_when_password_is_empty() throws Exception {
    try {
      Credentials credentials = Credentials.buildEncryptedCredentials(usernameBuilder.from("john@doe.fr"), passwordBuilder.from(""));
      fail("Should have throws a PasswordException");
    } catch (CredentialsException e) {
      assertThat(e.getMessage()).isEqualTo("Password cannot be empty");
    }
  }
}
