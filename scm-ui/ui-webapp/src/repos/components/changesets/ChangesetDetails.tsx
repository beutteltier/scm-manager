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
import React, { FC, useState } from "react";
import { Trans, useTranslation, WithTranslation, withTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { Changeset, ParentChangeset, Repository } from "@scm-manager/ui-types";
import {
  AvatarImage,
  AvatarWrapper,
  Button,
  ChangesetAuthor,
  ChangesetDescription,
  ChangesetDiff,
  ChangesetId,
  changesets,
  ChangesetTags,
  DateFromNow,
  FileControlFactory,
  Icon,
  Level,
  SignatureIcon
} from "@scm-manager/ui-components";
import ContributorTable from "./ContributorTable";
import { Link as ReactLink } from "react-router-dom";

type Props = WithTranslation & {
  changeset: Changeset;
  repository: Repository;
  fileControlFactory?: FileControlFactory;
};

type State = {
  collapsed: boolean;
};

const RightMarginP = styled.p`
  margin-right: 1em;
`;

const BottomMarginLevel = styled(Level)`
  margin-bottom: 1rem !important;
`;

const countContributors = (changeset: Changeset) => {
  if (changeset.contributors) {
    return changeset.contributors.length + 1;
  }
  return 1;
};

const FlexRow = styled.div`
  display: flex;
  flex-direction: row;
`;

const ContributorLine = styled.div`
  display: flex;
  cursor: pointer;
`;

const ContributorColumn = styled.p`
  flex-grow: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
`;

const CountColumn = styled.p`
  text-align: right;
  white-space: nowrap;
`;

const ContributorDetails = styled.div`
  display: flex;
  flex-direction: column;
  margin-bottom: 1rem;
`;

const ContributorToggleLine = styled.p`
  cursor: pointer;
  /** margin-bottom is inherit from content p **/
  margin-bottom: 0.5rem !important;
`;

const ChangesetSummary = styled.div`
  display: flex;
  justify-content: space-between;
`;

const SeparatedParents = styled.div`
  margin-left: 1em;
  a + a:before {
    content: ",\\00A0";
    color: #4a4a4a;
  }
`;

const Contributors: FC<{ changeset: Changeset }> = ({ changeset }) => {
  const [t] = useTranslation("repos");
  const [open, setOpen] = useState(false);
  const signatureIcon = changeset?.signatures && changeset.signatures.length > 0 && (
    <SignatureIcon className="mx-2" signatures={changeset.signatures} />
  );

  if (open) {
    return (
      <ContributorDetails>
        <FlexRow>
          <ContributorToggleLine onClick={e => setOpen(!open)} className="is-ellipsis-overflow">
            <Icon name="angle-down" /> {t("changeset.contributors.list")}
          </ContributorToggleLine>
          {signatureIcon}
        </FlexRow>
        <ContributorTable changeset={changeset} />
      </ContributorDetails>
    );
  }

  return (
    <>
      <ContributorLine onClick={e => setOpen(!open)}>
        <ContributorColumn className="is-ellipsis-overflow">
          <Icon name="angle-right" /> <ChangesetAuthor changeset={changeset} />
        </ContributorColumn>
        {signatureIcon}
        <CountColumn className={"is-hidden-mobile"}>
          (
          <span className="has-text-link">
            {t("changeset.contributors.count", { count: countContributors(changeset) })}
          </span>
          )
        </CountColumn>
      </ContributorLine>
    </>
  );
};

class ChangesetDetails extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      collapsed: false
    };
  }

  render() {
    const { changeset, repository, fileControlFactory, t } = this.props;
    const { collapsed } = this.state;

    const description = changesets.parseDescription(changeset.description);
    const id = <ChangesetId repository={repository} changeset={changeset} link={false} />;
    const date = <DateFromNow date={changeset.date} />;
    const parents = changeset._embedded.parents.map((parent: ParentChangeset, index: number) => (
      <ReactLink title={parent.id} to={parent.id} key={index}>
        {parent.id.substring(0, 7)}
      </ReactLink>
    ));

    return (
      <>
        <div className={classNames("content", "is-marginless")}>
          <h4>
            <ExtensionPoint
              name="changeset.description"
              props={{
                changeset,
                value: description.title
              }}
              renderAll={false}
            >
              <ChangesetDescription changeset={changeset} value={description.title} />
            </ExtensionPoint>
          </h4>
          <article className="media">
            <AvatarWrapper>
              <RightMarginP className={classNames("image", "is-64x64")}>
                <AvatarImage person={changeset.author} />
              </RightMarginP>
            </AvatarWrapper>
            <div className="media-content">
              <Contributors changeset={changeset} />
              <ChangesetSummary className="is-ellipsis-overflow">
                <p>
                  <Trans i18nKey="repos:changeset.summary" components={[id, date]} />
                </p>
                {parents?.length > 0 && (
                  <SeparatedParents>
                    {t("changeset.parents.label", { count: parents?.length }) + ": "}
                    {parents}
                  </SeparatedParents>
                )}
              </ChangesetSummary>
            </div>
            <div className="media-right">
              <ChangesetTags changeset={changeset} />
            </div>
          </article>
          <p>
            {description.message.split("\n").map((item, key) => {
              return (
                <span key={key}>
                  <ExtensionPoint
                    name="changeset.description"
                    props={{
                      changeset,
                      value: item
                    }}
                    renderAll={false}
                  >
                    <ChangesetDescription changeset={changeset} value={item} />
                  </ExtensionPoint>
                  <br />
                </span>
              );
            })}
          </p>
        </div>
        <div>
          <BottomMarginLevel
            right={
              <Button
                action={this.collapseDiffs}
                color="default"
                icon={collapsed ? "eye" : "eye-slash"}
                label={t("changesets.collapseDiffs")}
                reducedMobile={true}
              />
            }
          />
          <ChangesetDiff changeset={changeset} fileControlFactory={fileControlFactory} defaultCollapse={collapsed} />
        </div>
      </>
    );
  }

  collapseDiffs = () => {
    this.setState(state => ({
      collapsed: !state.collapsed
    }));
  };
}

export default withTranslation("repos")(ChangesetDetails);
