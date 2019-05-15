//@flow
import React from "react";
import { translate } from "react-i18next";
import type { Role } from "@scm-manager/ui-types";
import ExtensionPoint from "@scm-manager/ui-extensions/lib/ExtensionPoint";
import PermissionRoleDetailsTable from "./PermissionRoleDetailsTable";

type Props = {
  role: Role,

  // context props
  t: string => string,
};

class PermissionRoleDetails extends React.Component<Props> {
  render() {
    const { role } = this.props;

    return (

      <div>
        <PermissionRoleDetailsTable role={role}/>
        <hr/>
        <div className="content">
          <ExtensionPoint
            name="roles.repositoryRole-details.information"
            renderAll={true}
            props={{ role }}
          />
        </div>
      </div>

    );
  }
}

export default translate("config")(PermissionRoleDetails);
