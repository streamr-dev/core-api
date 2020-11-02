const StreamrClient = require('streamr-client')
const getStreamrClient = require('./test-utilities.js').getStreamrClient
const ITERATIONS = 50

describe('Login API', function() {

	this.timeout(40000)

	it('survives a race condition when user does many simultaneous logins', () => {
		const user = StreamrClient.generateEthereumAccount()
		return Promise.all(Array.from({ length: ITERATIONS }, () => Promise.resolve(getStreamrClient(user).session.getSessionToken())))
	})
});
