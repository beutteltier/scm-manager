package sonia.scm.repository.spi;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.event.ScmEventBus;
import sonia.scm.repository.Branch;
import sonia.scm.repository.PostReceiveRepositoryHookEvent;
import sonia.scm.repository.PreReceiveRepositoryHookEvent;
import sonia.scm.repository.api.BranchRequest;
import sonia.scm.repository.api.HookContext;
import sonia.scm.repository.api.HookContextFactory;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class GitBranchCommandTest extends AbstractGitCommandTestBase {

  @Mock
  private HookContextFactory hookContextFactory;
  @Mock
  private ScmEventBus eventBus;

  @Test
  public void shouldCreateBranchWithDefinedSourceBranch() throws IOException {
    GitContext context = createContext();

    Branch source = findBranch(context, "mergeable");

    BranchRequest branchRequest = new BranchRequest();
    branchRequest.setParentBranch(source.getName());
    branchRequest.setNewBranch("new_branch");

    createCommand().branch(branchRequest);

    Branch newBranch = findBranch(context, "new_branch");
    assertThat(newBranch.getRevision()).isEqualTo(source.getRevision());
  }

  private Branch findBranch(GitContext context, String name) throws IOException {
    List<Branch> branches = readBranches(context);
    return branches.stream().filter(b -> name.equals(b.getName())).findFirst().get();
  }

  @Test
  public void shouldCreateBranch() throws IOException {
    GitContext context = createContext();

    assertThat(readBranches(context)).filteredOn(b -> b.getName().equals("new_branch")).isEmpty();

    BranchRequest branchRequest = new BranchRequest();
    branchRequest.setNewBranch("new_branch");

    createCommand().branch(branchRequest);

    assertThat(readBranches(context)).filteredOn(b -> b.getName().equals("new_branch")).isNotEmpty();
  }

  @Test
  public void shouldDeleteBranch() throws IOException {
    GitContext context = createContext();
    String branchToBeDeleted = "squash";
    createCommand().deleteOrClose(branchToBeDeleted);
    assertThat(readBranches(context)).filteredOn(b -> b.getName().equals(branchToBeDeleted)).isEmpty();
  }

  @Test
  public void shouldThrowExceptionWhenDeletingDefaultBranch() {
    String branchToBeDeleted = "master";
    assertThrows(CannotDeleteDefaultBranchException.class, () -> createCommand().deleteOrClose(branchToBeDeleted));
  }

  private GitBranchCommand createCommand() {
    return new GitBranchCommand(createContext(), repository, hookContextFactory, eventBus);
  }

  private List<Branch> readBranches(GitContext context) throws IOException {
    return new GitBranchesCommand(context, repository).getBranches();
  }

  @Test
  public void shouldPostCreateEvents() {
    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
    doNothing().when(eventBus).post(captor.capture());
    when(hookContextFactory.createContext(any(), any())).thenAnswer(this::createMockedContext);

    BranchRequest branchRequest = new BranchRequest();
    branchRequest.setParentBranch("mergeable");
    branchRequest.setNewBranch("new_branch");

    createCommand().branch(branchRequest);

    List<Object> events = captor.getAllValues();
    assertThat(events.get(0)).isInstanceOf(PreReceiveRepositoryHookEvent.class);
    assertThat(events.get(1)).isInstanceOf(PostReceiveRepositoryHookEvent.class);

    PreReceiveRepositoryHookEvent event = (PreReceiveRepositoryHookEvent) events.get(0);
    assertThat(event.getContext().getBranchProvider().getCreatedOrModified()).containsExactly("new_branch");
    assertThat(event.getContext().getBranchProvider().getDeletedOrClosed()).isEmpty();
  }

  @Test
  public void shouldPostDeleteEvents() {
    ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
    doNothing().when(eventBus).post(captor.capture());
    when(hookContextFactory.createContext(any(), any())).thenAnswer(this::createMockedContext);

    createCommand().deleteOrClose("squash");

    List<Object> events = captor.getAllValues();
    assertThat(events.get(0)).isInstanceOf(PreReceiveRepositoryHookEvent.class);
    assertThat(events.get(1)).isInstanceOf(PostReceiveRepositoryHookEvent.class);

    PreReceiveRepositoryHookEvent event = (PreReceiveRepositoryHookEvent) events.get(0);
    assertThat(event.getContext().getBranchProvider().getDeletedOrClosed()).containsExactly("squash");
    assertThat(event.getContext().getBranchProvider().getCreatedOrModified()).isEmpty();
  }

  private HookContext createMockedContext(InvocationOnMock invocation) {
    HookContext mock = mock(HookContext.class);
    when(mock.getBranchProvider()).thenReturn(((HookContextProvider) invocation.getArgument(0)).getBranchProvider());
    return mock;
  }
}
