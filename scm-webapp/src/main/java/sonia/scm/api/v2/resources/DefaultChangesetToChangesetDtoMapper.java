/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;
import sonia.scm.repository.Branch;
import sonia.scm.repository.Changeset;
import sonia.scm.repository.ChangesetTrailers;
import sonia.scm.repository.Person;
import sonia.scm.repository.Repository;
import sonia.scm.repository.Tag;
import sonia.scm.repository.Trailer;
import sonia.scm.repository.api.Command;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.EdisonHalAppender;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Link.link;
import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class DefaultChangesetToChangesetDtoMapper extends HalAppenderMapper implements InstantAttributeMapper, ChangesetToChangesetDtoMapper {

  @Inject
  private RepositoryServiceFactory serviceFactory;

  @Inject
  private ResourceLinks resourceLinks;

  @Inject
  private BranchCollectionToDtoMapper branchCollectionToDtoMapper;

  @Inject
  private ChangesetToParentDtoMapper changesetToParentDtoMapper;

  @Inject
  private TagCollectionToDtoMapper tagCollectionToDtoMapper;

  @Inject
  private Set<ChangesetTrailers> changesetTrailersSet;

  abstract TrailerDto map(Trailer trailer);

  abstract PersonDto map(Person person);

  @AfterMapping
  void appendTrailerPersons(Changeset changeset, @MappingTarget ChangesetDto target, @Context Repository repository) {
    List<TrailerDto> collectedTrailers = new ArrayList<>();
    changesetTrailersSet.stream()
      .flatMap(changesetTrailers -> changesetTrailers.getTrailers(repository, changeset).stream())
      .map(this::map)
      .forEach(collectedTrailers::add);
    target.setTrailers(collectedTrailers);
  }

  @AfterMapping
  void removeTrailerFromChangesetDescription(@MappingTarget ChangesetDto target) {
    StringBuilder builder = new StringBuilder();
    try (Scanner scanner = new Scanner(target.getDescription())) {
      scanner.useDelimiter(Pattern.compile("[\\n]"));
      while (scanner.hasNext()) {
        String line = scanner.next();
        if (!line.contains("-by:")) {
          builder.append(line).append("\n");
        }
      }
    }

    target.setDescription(builder.toString());
  }

  @ObjectFactory
  ChangesetDto createDto(@Context Repository repository, Changeset source) {
    String namespace = repository.getNamespace();
    String name = repository.getName();

    Embedded.Builder embeddedBuilder = embeddedBuilder();

    Links.Builder linksBuilder = linkingTo()
      .self(resourceLinks.changeset().self(repository.getNamespace(), repository.getName(), source.getId()))
      .single(link("diff", resourceLinks.diff().self(namespace, name, source.getId())))
      .single(link("sources", resourceLinks.source().self(namespace, name, source.getId())))
      .single(link("modifications", resourceLinks.modifications().self(namespace, name, source.getId())));

    try (RepositoryService repositoryService = serviceFactory.create(repository)) {
      if (repositoryService.isSupported(Command.TAGS)) {
        embeddedBuilder.with("tags", tagCollectionToDtoMapper.getTagDtoList(namespace, name,
          getListOfObjects(source.getTags(), tagName -> new Tag(tagName, source.getId()))));
      }
      if (repositoryService.isSupported(Command.BRANCHES)) {
        embeddedBuilder.with("branches", branchCollectionToDtoMapper.getBranchDtoList(namespace, name,
          getListOfObjects(source.getBranches(), branchName -> Branch.normalBranch(branchName, source.getId()))));
      }

      if (repositoryService.isSupported(Command.DIFF_RESULT)) {
        linksBuilder.single(link("diffParsed", resourceLinks.diff().parsed(namespace, name, source.getId())));
      }
    }
    embeddedBuilder.with("parents", getListOfObjects(source.getParents(), parent -> changesetToParentDtoMapper.map(new Changeset(parent, 0L, null), repository)));

    applyEnrichers(new EdisonHalAppender(linksBuilder, embeddedBuilder), source, repository);

    return new ChangesetDto(linksBuilder.build(), embeddedBuilder.build());
  }

  private <T> List<T> getListOfObjects(List<String> list, Function<String, T> mapFunction) {
    return list
      .stream()
      .map(mapFunction)
      .collect(Collectors.toList());
  }
}
