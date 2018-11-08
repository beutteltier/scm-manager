//@flow
import { apiClient } from "@scm-manager/ui-components";
const CONTENT_TYPE_PASSWORD_OVERWRITE =
  "application/vnd.scmm-passwordOverwrite+json;v=2";

export function updatePassword(url: string, password: string) {
  return apiClient
    .put(url, { newPassword: password }, CONTENT_TYPE_PASSWORD_OVERWRITE)
    .then(response => {
      return response;
    })
    .catch(err => {
      return { error: err };
    });
}