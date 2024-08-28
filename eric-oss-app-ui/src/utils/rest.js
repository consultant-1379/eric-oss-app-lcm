/**
 * calls fetch request on the url given
 * @param url - url to request
 * @param method - the HTTP method to use GET | POST | PUT | DELETE | PATCH
 * @param options - options to provide to the fetch funtion (e.g. header, body, ...)
 * @returns {Promise<any>} Promise that resolves with the json data of the response
 */
async function fetchData(url, method, options = {}) {

    const response = await fetch(url, {
        method: method,
        ...options
    });
    
    if (response.ok) {
        // Try to parse the data and return an error in case of failure
        let data;
        try {
            const contentType = response.headers.get('Content-Type');
            if (contentType && contentType.includes('application/json')) {
                data = await response.json();
            } else {
                data = await response.text();
            }
        } catch(err) {
            console.error(`Unable to parse reponse to ${method} '${url}': status: ${response.status}, error: '${err}'`);
            return {
                status: 500,
                ok: false,
                data: {
                    message: 'Unable to parse server response',
                    originStatus: response.status
                }
            }
        }

        return {
            status: response.status,
            ok: true,
            data: data
        }
    } else {
        let data = await response.text();
        try {
            data = JSON.parse(data);
            if (data.detail) {
                data = {message: data.detail}
            }
        } catch(err) {
            // keep the response as is if not json
            data = {message: `An internal error occurred while sending the request: ${data}`}
        }

        console.error(`Error on request ${method} '${url}': status: ${response.status}, error: '${data.message}'`);
        return {
            status: response.status,
            ok: false,
            data: data
        }
    }
}

export { fetchData };
