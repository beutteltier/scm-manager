package sonia.scm.repository.spi;

import org.junit.Test;
import sonia.scm.repository.api.DiffFile;
import sonia.scm.repository.api.DiffResult;
import sonia.scm.repository.api.Hunk;

import java.io.IOException;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;

public class GitDiffResultCommandTest extends AbstractGitCommandTestBase {

  @Test
  public void shouldReturnOldAndNewRevision() throws IOException {
    DiffResult diffResult = createDiffResult("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");

    assertThat(diffResult.getNewRevision()).isEqualTo("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
    assertThat(diffResult.getOldRevision()).isEqualTo("592d797cd36432e591416e8b2b98154f4f163411");
  }

  @Test
  public void shouldReturnFilePaths() throws IOException {
    DiffResult diffResult = createDiffResult("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
    Iterator<DiffFile> iterator = diffResult.iterator();
    DiffFile a = iterator.next();
    assertThat(a.getNewPath()).isEqualTo("a.txt");
    assertThat(a.getOldPath()).isEqualTo("a.txt");

    DiffFile b = iterator.next();
    assertThat(b.getOldPath()).isEqualTo("b.txt");
    assertThat(b.getNewPath()).isEqualTo("/dev/null");
  }

  @Test
  public void shouldReturnFileRevisions() throws IOException {
    DiffResult diffResult = createDiffResult("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
    Iterator<DiffFile> iterator = diffResult.iterator();

    DiffFile a = iterator.next();
    assertThat(a.getOldRevision()).isEqualTo("78981922613b2afb6025042ff6bd878ac1994e85");
    assertThat(a.getNewRevision()).isEqualTo("1dc60c7504f4326bc83b9b628c384ec8d7e57096");

    DiffFile b = iterator.next();
    assertThat(b.getOldRevision()).isEqualTo("61780798228d17af2d34fce4cfbdf35556832472");
    assertThat(b.getNewRevision()).isEqualTo("0000000000000000000000000000000000000000");
  }

  @Test
  public void shouldReturnFileHunks() throws IOException {
    DiffResult diffResult = createDiffResult("3f76a12f08a6ba0dc988c68b7f0b2cd190efc3c4");
    Iterator<DiffFile> iterator = diffResult.iterator();

    DiffFile a = iterator.next();
    Iterator<Hunk> hunks = a.iterator();

    Hunk hunk = hunks.next();
    assertThat(hunk.getOldStart()).isEqualTo(1);
    assertThat(hunk.getOldLineCount()).isEqualTo(1);

    assertThat(hunk.getNewStart()).isEqualTo(1);
    assertThat(hunk.getNewLineCount()).isEqualTo(1);
  }

  @Test
  public void shouldReturnFileHunksWithFullFileRange() throws IOException {
    DiffResult diffResult = createDiffResult("fcd0ef1831e4002ac43ea539f4094334c79ea9ec");
    Iterator<DiffFile> iterator = diffResult.iterator();

    DiffFile a = iterator.next();
    Iterator<Hunk> hunks = a.iterator();

    Hunk hunk = hunks.next();
    assertThat(hunk.getOldStart()).isEqualTo(1);
    assertThat(hunk.getOldLineCount()).isEqualTo(1);

    assertThat(hunk.getNewStart()).isEqualTo(1);
    assertThat(hunk.getNewLineCount()).isEqualTo(2);
  }

  private DiffResult createDiffResult(String s) throws IOException {
    GitDiffResultCommand gitDiffResultCommand = new GitDiffResultCommand(createContext(), repository);
    DiffCommandRequest diffCommandRequest = new DiffCommandRequest();
    diffCommandRequest.setRevision(s);

    return gitDiffResultCommand.getDiffResult(diffCommandRequest);
  }
}