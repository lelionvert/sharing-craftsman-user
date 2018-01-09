package fr.sharingcraftsman.user.infrastructure.repositories;

import fr.sharingcraftsman.user.UserApplication;
import fr.sharingcraftsman.user.infrastructure.models.UserEntity;
import org.assertj.core.util.Lists;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {UserApplication.class})
@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
public class UserEntityRepositoryTest {
  @Autowired
  private TestEntityManager entityManager;

  @Autowired
  private UserJpaRepository userJpaRepository;

  @After
  public void tearDown() throws Exception {
    userJpaRepository.deleteAll();
  }

  @Test
  public void should_save_a_new_user() throws Exception {
    userJpaRepository.save(new UserEntity("john@doe.fr", "T49xWf/l7gatvfVwethwDw=="));

    UserEntity expectedUserEntity = new UserEntity("john@doe.fr", "T49xWf/l7gatvfVwethwDw==");
    expectedUserEntity.setId(1);
    assertThat(Lists.newArrayList(userJpaRepository.findAll())).containsExactly(
            expectedUserEntity
    );
  }

  @Test
  public void should_get_user_by_username() throws Exception {
    entityManager.persist(new UserEntity("john@doe.fr", "password"));
    entityManager.persist(new UserEntity("hello@world.fr", "toto"));

    UserEntity expectedUserEntity = new UserEntity("hello@world.fr", "toto");
    expectedUserEntity.setId(2);
    assertThat(userJpaRepository.findByUsername("hello@world.fr")).isEqualTo(expectedUserEntity);
  }
}
