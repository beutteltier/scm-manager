//@flow
import React from "react";

type Props = {
  label: string,
  action: () => void
};

class NavAction extends React.Component<Props> {
  render() {
    const { label, action } = this.props;
    return (
      <li>
        <a onClick={action} href="javascript:void(0);">{label}</a>
      </li>
    );
  }
}

export default NavAction;
