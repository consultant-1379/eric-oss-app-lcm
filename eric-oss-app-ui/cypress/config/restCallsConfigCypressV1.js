const onBoardingUrl = "http://localhost:8081";
const appLcmUrl = "http://localhost:8080";

const restCallsV1 = {
	onBoarding: {
		getAllApps: {
			request: `${onBoardingUrl}${'/v1/apps'}`,
			method: 'GET',
		},
		getById: {
			request: `${onBoardingUrl}${'/v1/apps/1'}`,
			method: 'GET',
		},
		putById: {
			request: `${onBoardingUrl}${'/v1/apps/1'}`,
			method: 'PUT',
		},
	},
	appLcm: {
		deleteApp: {
			request: `${appLcmUrl}${'/app-lcm/v1/apps/1'}`,
			method: 'DELETE',
		},
	},
};

export { restCallsV1 };