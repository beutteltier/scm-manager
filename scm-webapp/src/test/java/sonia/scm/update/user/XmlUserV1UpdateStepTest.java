package sonia.scm.update.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.TempDirectory;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.security.AssignedPermission;
import sonia.scm.store.ConfigurationEntryStore;
import sonia.scm.store.InMemoryConfigurationEntryStoreFactory;
import sonia.scm.update.UpdateStepTestUtil;
import sonia.scm.update.properties.V1Properties;
import sonia.scm.update.properties.V1Property;
import sonia.scm.user.User;
import sonia.scm.user.xml.XmlUserDAO;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static sonia.scm.store.InMemoryConfigurationEntryStoreFactory.create;

@ExtendWith(MockitoExtension.class)
@ExtendWith(TempDirectory.class)
class XmlUserV1UpdateStepTest {

  @Mock
  XmlUserDAO userDAO;

  @Captor
  ArgumentCaptor<User> userCaptor;

  InMemoryConfigurationEntryStoreFactory storeFactory = create();

  XmlUserV1UpdateStep updateStep;

  private UpdateStepTestUtil testUtil;

  @BeforeEach
  void mockScmHome(@TempDirectory.TempDir Path tempDir) {
    testUtil = new UpdateStepTestUtil(tempDir);
    updateStep = new XmlUserV1UpdateStep(testUtil.getContextProvider(), userDAO, storeFactory);
  }

  @Nested
  class WithExistingDatabase {

    @BeforeEach
    void captureStoredRepositories() {
      doNothing().when(userDAO).add(userCaptor.capture());
    }

    @BeforeEach
    void createUserV1XML() throws IOException {
      testUtil.copyConfigFile("sonia/scm/update/user/users.xml");
    }

    @Test
    void shouldCreateNewPermissionsForV1AdminUser() throws JAXBException {
      updateStep.doUpdate();
      Optional<AssignedPermission> assignedPermission =
        storeFactory.<AssignedPermission>get("security")
          .getAll()
          .values()
          .stream()
          .filter(a -> a.getName().equals("scmadmin"))
          .findFirst();
      assertThat(assignedPermission.get().getPermission().getValue()).contains("*");
      assertThat(assignedPermission.get().isGroupPermission()).isFalse();
    }

    @Test
    void shouldCreateNewUserFromUsersV1Xml() throws JAXBException {
      updateStep.doUpdate();
      verify(userDAO, times(5)).add(any());
    }

    @Test
    void shouldMapAttributesFromUsersV1Xml() throws JAXBException {
      updateStep.doUpdate();
      Optional<User> user = userCaptor.getAllValues().stream().filter(u -> u.getName().equals("scmadmin")).findFirst();
      assertThat(user)
        .get()
        .hasFieldOrPropertyWithValue("name", "scmadmin")
        .hasFieldOrPropertyWithValue("mail", "scm-admin@scm-manager.com")
        .hasFieldOrPropertyWithValue("displayName", "SCM Administrator")
        .hasFieldOrPropertyWithValue("active", false)
        .hasFieldOrPropertyWithValue("password", "ff8f5c593a01f9fcd3ed48b09a4b013e8d8f3be7")
        .hasFieldOrPropertyWithValue("type", "xml")
        .hasFieldOrPropertyWithValue("lastModified", 1558597367492L)
        .hasFieldOrPropertyWithValue("creationDate", 1558597074732L);
    }

    @Test
    void shouldExtractProperties() throws JAXBException {
      updateStep.doUpdate();
      ConfigurationEntryStore<V1Properties> propertiesStore = storeFactory.<V1Properties>get("user-properties-v1");
      assertThat(propertiesStore.get("dent"))
        .isNotNull()
        .extracting(V1Properties::getProperties)
        .first()
        .asList()
        .contains(
          new V1Property("born.on", "earth"),
          new V1Property("last.seen", "end of the universe"));
    }
  }

  @Test
  void shouldNotFailForMissingConfigDir() throws JAXBException {
    updateStep.doUpdate();
  }
}
