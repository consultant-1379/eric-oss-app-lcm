const hostname = new URL(location);
let onBoardingUrl = '';
let appLcmUrl = '';
if (hostname.hostname === "localhost") {
    onBoardingUrl = `${'http://'}${hostname.hostname}${':8081'}`;
    appLcmUrl = `${'http://'}${hostname.hostname}${':8080'}`;
} else {
    onBoardingUrl = "/app-manager/onboarding";
    appLcmUrl = "/app-manager/lcm";
}
const restCallsV1 = {
  onBoarding: {
    getAllApps: {
      request: `${onBoardingUrl}${'/v1/apps'}`,
      method: 'GET',
    },
    onboardApp: {
      request: `${onBoardingUrl}${'/v1/apps'}`,
      method: 'POST',
    },
  },
  appLcm: {
    getAppInstance: {
      request: `${appLcmUrl}${'/app-lcm/v1/app-instances'}`,
      method: 'GET',
    },
    deleteApp: {
      request: `${appLcmUrl}${'/app-lcm/v1/apps'}`,
      method: 'DELETE',
    },
  },
};
const getAppIdV1 = function() { // eslint-disable-line
  const matchID = window.location.href.match(/[0-9]+$/);
  let appID = 0;
  if (!matchID) {
    appID = 1
  } else {
    appID = parseInt(matchID[0], 10);
  }
  return appID;
}
export { restCallsV1, getAppIdV1 };