const hostname = new URL(location);
let onBoardingUrl = '';
let appLcmUrl = '';
if (hostname.hostname === "localhost") {
    onBoardingUrl = `${'http://'}${hostname.hostname}${':8081'}`;
    appLcmUrl = `${'http://'}${hostname.hostname}${':8080'}`;
} else {
    onBoardingUrl = "/app-onboarding";
    appLcmUrl = "/app-lifecycle-management";
}
const restCalls = {
  onBoarding: {
    onboardApp: {
      request: `${onBoardingUrl}${'/v2/app-packages'}`,
      method: 'POST',
    },
    getOnboardingJobs: {
      request: `${onBoardingUrl}${'/v2/onboarding-jobs'}`,
      method: 'GET',
    },
    deleteOnboardingJob: {
      request: `${onBoardingUrl}${'/v2/onboarding-jobs'}`,
      method: 'DELETE',
    },
  },
  appLcm: {
    getAllApps: {
      request: `${appLcmUrl}${'/v3/apps'}`,
      method: 'GET',
    },
    getAppInstance: {
      request: `${appLcmUrl}${'/v3/app-instances'}`,
      method: 'GET',
    },
    deleteApp: {
      request: `${appLcmUrl}${'/v3/apps'}`,
      method: 'DELETE',
    },
    initApp: {
      request: `${appLcmUrl}${'/v3/apps'}`,
      method: 'POST',
    },
  },
};

const getAppIdForArtifacts = function() { // eslint-disable-line
  var hash = window.location.hash.substring(31);
  var params = {}
  hash.split('&').map(hk => {
    let temp = hk.split('=');
      params[temp[0]] = temp[1]
  });
  return params.appid;
}

const getAppId = function() { // eslint-disable-line
  let hrefValue = window.location.href;
  let matchID = hrefValue.split('=').pop().split('&')[0]

  return matchID;
}

const getComponentName = function() { // eslint-disable-line
  var hash = window.location.hash.substring(31);
  var params = {}
  hash.split('&').map(hk => {
    let temp = hk.split('=');
      params[temp[0]] = temp[1]
  });
  return params.component;
}

export { restCalls, getAppId, getComponentName, getAppIdForArtifacts };