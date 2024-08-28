import { APPLICATION_JSON_HEADER, API_GAS_RBAC, POST } from "../constants"
import { fetchData } from "../rest"

export const hasResource = async (resource, methods) => {
  const url = API_GAS_RBAC;
  const requestBody = {
    response_mode: 'permissions',
    permission: [resource]
  }
  const options = {
      headers: APPLICATION_JSON_HEADER,
      body: JSON.stringify(requestBody)
  };

  let res;
  try {
      res = await fetchData(url, POST, options);
  } catch(err) {
      console.error(`Unable to retrieve the current user's permissions ${err}`);
      return false;
  }

  if (res.ok) {
      let permissions = res.data;
      if (!permissions || permissions.length != 1) {
          console.error(`Invalid response received from RBAC APIs ${JSON.stringify(permissions)}`);
          return false;
      }

      // Reject user if they do not have have GET scope/s
      const scopes = permissions[0].scopes || [];

      return methods.every((p) => scopes.includes(p));
  } else {
    console.error(`Access Denied! The current user does not have permission to access this page.`);
    return false;
  }
}