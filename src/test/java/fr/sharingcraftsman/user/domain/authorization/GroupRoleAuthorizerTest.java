package fr.sharingcraftsman.user.domain.authorization;

import fr.sharingcraftsman.user.domain.authentication.Credentials;
import fr.sharingcraftsman.user.domain.common.Username;
import fr.sharingcraftsman.user.domain.ports.authorization.Authorizer;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static fr.sharingcraftsman.user.domain.common.Username.usernameBuilder;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class GroupRoleAuthorizerTest {
  private Authorizer authorizer;
  private Credentials credentials;
  @Mock
  private GroupAdministrator groupAdministrator;
  @Mock
  private RoleAdministrator roleAdministrator;

  @Before
  public void setUp() throws Exception {
    authorizer = new GroupRoleAuthorizer(groupAdministrator, roleAdministrator);

    credentials = Credentials.buildCredentials(usernameBuilder.from("john@doe.fr"), null, false);
  }

  @Test
  public void should_get_authorizations_of_collaborator() throws Exception {
    given(groupAdministrator.findGroupsOf(credentials.getUsername())).willReturn(Collections.singletonList(new Group("USERS")));
    given(roleAdministrator.getRolesOf("USERS")).willReturn(Collections.singletonList(new Role("ROLE_USER")));

    Authorizations authorizations = authorizer.getAuthorizationsOf(credentials);

    Role role = new Role("ROLE_USER");
    Group group = new Group("USERS");
    group.addRole(role);
    Authorizations expectedAuthorizations = new Authorizations();
    expectedAuthorizations.addGroup(group);
    assertThat(authorizations).isEqualTo(expectedAuthorizations);
  }

  @Test
  public void should_add_given_group_to_collaborator() throws Exception {
    authorizer.addGroup(credentials, Groups.USERS);

    verify(groupAdministrator).addGroup(credentials.getUsername(), Groups.USERS);
  }

  @Test
  public void should_not_add_group_if_collaborator_already_has_the_group() throws Exception {
    given(groupAdministrator.findGroupsOf(credentials.getUsername())).willReturn(Collections.singletonList(new Group("USERS")));

    authorizer.addGroup(credentials, Groups.USERS);

    verify(groupAdministrator, never()).addGroup(any(Username.class), any(Groups.class));
  }

  @Test
  public void should_get_all_groups_and_roles() throws Exception {
    authorizer.getAllRolesWithTheirGroups();

    verify(roleAdministrator).getAllRolesWithTheirGroups();
  }
}