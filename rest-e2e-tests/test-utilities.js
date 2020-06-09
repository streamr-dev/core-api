const assert = require('chai').assert

async function assertResponseIsError(response, statusCode, programmaticCode, includeInMessage) {
    const json = response.data
    assert.equal(response.status, statusCode)
    assert.equal(json.code, programmaticCode)
    if (includeInMessage) {
        assert.include(json.message, includeInMessage)
    }
}

module.exports = {
    assertResponseIsError: assertResponseIsError
}
