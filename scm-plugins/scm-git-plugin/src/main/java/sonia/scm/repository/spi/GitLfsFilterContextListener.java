package sonia.scm.repository.spi;

import com.google.common.io.ByteStreams;
import org.eclipse.jgit.attributes.FilterCommand;
import org.eclipse.jgit.attributes.FilterCommandRegistry;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.util.SystemReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.plugin.Extension;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Pattern;

@Extension
public class GitLfsFilterContextListener implements ServletContextListener {

  public static final Pattern COMMAND_NAME_PATTERN = Pattern.compile("git-lfs (smudge|clean) -- .*");

  private static final Logger LOG = LoggerFactory.getLogger(GitLfsFilterContextListener.class);

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    try {
      SystemReader.getInstance().getSystemConfig().setString("filter", "lfs", "clean", "git-lfs clean -- %f");
      SystemReader.getInstance().getSystemConfig().setString("filter", "lfs", "smudge", "git-lfs smudge -- %f");
      SystemReader.getInstance().getSystemConfig().setString("filter", "lfs", "process", "git-lfs filter-process");
      SystemReader.getInstance().getSystemConfig().setString("filter", "lfs", "required", "true");
    } catch (Exception e) {
      LOG.error("could not set git config; git lfs support may not work correctly", e);
    }
    FilterCommandRegistry.register(COMMAND_NAME_PATTERN, NoOpFilterCommand::new);
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    FilterCommandRegistry.unregister(COMMAND_NAME_PATTERN);
  }

  private static class NoOpFilterCommand extends FilterCommand {
    NoOpFilterCommand(Repository db, InputStream in, OutputStream out) {
      super(in, out);
    }

    @Override
    public int run() throws IOException {
      ByteStreams.copy(in, out);
      in.close();
      out.close();
      return -1;
    }
  }
}
