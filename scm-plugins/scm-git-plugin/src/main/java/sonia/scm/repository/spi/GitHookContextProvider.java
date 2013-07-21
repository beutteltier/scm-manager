/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository.spi;

//~--- non-JDK imports --------------------------------------------------------

import org.eclipse.jgit.transport.ReceiveCommand;
import org.eclipse.jgit.transport.ReceivePack;

import sonia.scm.repository.RepositoryHookType;
import sonia.scm.repository.api.GitHookMessageProvider;
import sonia.scm.repository.api.HookMessageProvider;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitHookContextProvider extends HookContextProvider
{

  /**
   * Constructs ...
   *
   *
   * @param receivePack
   * @param receiveCommands
   * @param type
   */
  public GitHookContextProvider(ReceivePack receivePack,
    List<ReceiveCommand> receiveCommands, RepositoryHookType type)
  {
    this.receivePack = receivePack;
    this.receiveCommands = receiveCommands;
    this.type = type;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public HookChangesetProvider getChangesetProvider()
  {
    return new GitHookChangesetProvider(receivePack, receiveCommands, type);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public HookMessageProvider createMessageProvider()
  {
    return new GitHookMessageProvider(receivePack);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private List<ReceiveCommand> receiveCommands;

  /** Field description */
  private ReceivePack receivePack;

  /** Field description */
  private RepositoryHookType type;
}
