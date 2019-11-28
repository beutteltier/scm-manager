import React, { ReactNode } from "react";
import classNames from "classnames";

type Props = {
  className?: string;
  left?: ReactNode;
  right?: ReactNode;
};

export default class Level extends React.Component<Props> {
  render() {
    const { className, left, right } = this.props;
    return (
      <div className={classNames("level", className)}>
        <div className="level-left">{left}</div>
        <div className="level-right">{right}</div>
      </div>
    );
  }
}
