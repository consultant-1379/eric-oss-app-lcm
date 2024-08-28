const hostname = new URL(location);
let onBoardingUrl = '';
let appLcmUrl = '';
let permissionUrl = '';

if (hostname.hostname === "localhost") {
    onBoardingUrl = `${'http://'}${hostname.hostname}${':8081'}`;
    appLcmUrl = `${'http://'}${hostname.hostname}${':8080'}`;
    permissionUrl = `${'http://'}${hostname.hostname}${':8000'}`;
} else {
    onBoardingUrl = "/onboarding";
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
    getApp: {
      request: `${appLcmUrl}${'/v3/apps/7b8319dd-0198-4d4c-acf2-3fe352144951'}`,
      method: 'GET',
    },
    enableDisable: {
      request: `${appLcmUrl}${'/v3/apps/7b8319dd-0198-4d4c-acf2-3fe352144951/mode'}`,
      method: 'PUT',
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
	permissions: {
	    onboardApp: {
	        request: `${permissionUrl}${'/userpermission/v1/permission'}`,
			method: 'POST',
	    },
	},
};

export { restCalls };