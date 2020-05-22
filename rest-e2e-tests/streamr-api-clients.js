const fetch = require('node-fetch')
const url = require('url')
const querystring = require('querystring')
const FormData = require('form-data')
const StreamrClient = require('streamr-client')

class StreamrApiRequest {
    constructor(options) {
        this.baseUrl = options.baseUrl || 'https://www.streamr.com/api/v1/'
        if (!this.baseUrl.endsWith('/')) {
            this.baseUrl += '/'
        }

        this.logging = options.logging || false
        this.authHeader = null
        this.contentType = null
        this.queryParams = ''
    }

    methodAndPath(method, relativePath) {
        this.method = method
        this.relativePath = relativePath
        return this
    }

    withApiKey(apiKey) {
        this.authHeader = `Token ${apiKey}`
        return this
    }

    withSessionToken(sessionToken) {
        this.authHeader = `Bearer ${sessionToken}`
        return this
    }

    withQueryParams(queryParams) {
        if (queryParams) {
            this.queryParams = '?' + querystring.stringify(queryParams)
        }
        return this
    }

    withBody(body) {
        this.body = JSON.stringify(body)
        this.contentType = 'application/json'
        return this
    }

    withRawBody(body) {
        this.body = body
        this.contentType = null
        return this
    }

    async call() {
        if (!this.method) {
            throw 'Method not set.'
        }
        if (!this.relativePath) {
            throw 'Relative path not set.'
        }

        const apiUrl = url.resolve(this.baseUrl, this.relativePath) + this.queryParams

        const headers = {
            'Accept': 'application/json'
        }
        if (this.body && this.contentType) {
            headers['Content-type'] = this.contentType
        }
        if (this.authHeader) {
            headers['Authorization'] = this.authHeader
        }

        if (this.logging) {
            console.info(
                this.method,
                apiUrl,
                '\n\n' + Object.keys(headers)
                    .map(key => `${key}: ${headers[key]}`)
                    .join('\n'),
                typeof this.body === 'string' ? '\n\n' + JSON.stringify(JSON.parse(this.body), null, 4) : '\n\n' + JSON.stringify(this.body, null, 4),
                //this.body ? '\n\n' + JSON.stringify(JSON.parse(this.body), null, 4) : '',
                '\n\n'
            )
        }

        return fetch(apiUrl, {
            method: this.method,
            body: this.body,
            headers: headers
        })
    }

    async execute() {
        const response = await this.call()
        if (response.status == 204) {
            return true
        }
        const json = await response.json()
        if (Math.floor(response.status / 100) != 2) {
            const jsonAsString = JSON.stringify(json)
            throw Error(`Failed to execute. HTTP status ${response.status}: ${jsonAsString}`)
        }
        return json
    }
}

class Categories {
    constructor(options) {
        this.options = options
    }

    list() {
        return new StreamrApiRequest(this.options)
            .methodAndPath('GET', 'categories')
    }
}

class Products {
    constructor(options) {
        this.options = options
        this.permissions = new Permissions('products', options)
    }

    list(queryParams) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('GET', 'products')
            .withQueryParams(queryParams)
    }

    get(id) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('GET', `products/${id}`)
    }

    create(body) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('POST', 'products')
            .withBody(body)
    }

    update(id, body) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('PUT', `products/${id}`)
            .withBody(body)
    }

    setDeploying(id) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('POST', `products/${id}/setDeploying`)
    }

    setDeployed(id, body) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('POST', `products/${id}/setDeployed`)
            .withBody(body)
    }

    setUndeploying(id) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('POST', `products/${id}/setUndeploying`)
    }

    setUndeployed(id, body) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('POST', `products/${id}/setUndeployed`)
            .withBody(body)
    }

    deployFree(id) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('POST', `products/${id}/deployFree`)
    }

    undeployFree(id) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('POST', `products/${id}/undeployFree`)
    }

    uploadImage(id, fileBytes) {
        const formData = new FormData()
        formData.append('file', fileBytes)

        return new StreamrApiRequest(this.options)
            .methodAndPath('POST', `products/${id}/images`)
            .withRawBody(formData)
    }

    listStreams(id) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('GET', `products/${id}/streams`)
    }

    addStream(id, streamId) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('PUT', `products/${id}/streams/${streamId}`)
    }

    removeStream(id, streamId) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('DELETE', `products/${id}/streams/${streamId}`)
    }
}

class Streams {
    constructor(options) {
        this.options = options
        this.permissions = new Permissions('streams', options)
    }

    create(body) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('POST', 'streams')
            .withBody(body)
    }

    get(id) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('GET', `streams/${id}`)
    }

    setFields(id, body) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('POST', `streams/${id}/fields`)
            .withBody(body)
    }

    uploadCsvFile(id, fileBytes) {
        const formData = new FormData()
        formData.append('file', fileBytes)

        return new StreamrApiRequest(this.options)
            .methodAndPath('POST', `streams/${id}/uploadCsvFile`)
            .withRawBody(formData)
    }

    confirmCsvUpload(id, body) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('POST', `streams/${id}/confirmCsvFileUpload`)
            .withBody(body)
    }

    grantPublic(id, operation) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('POST', `streams/${id}/permissions`)
            .withBody({
                anonymous: true,
                operation,
            })
    }

    grant(id, targetUser, operation) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('POST', `streams/${id}/permissions`)
            .withBody({
                user: targetUser,
                operation,
            })
    }

    getOwnPermissions(id) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('GET', `streams/${id}/permissions/me`)
    }

    getValidationInfo(id) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('GET', `streams/${id}/validation`)
    }

    getPublishers(id) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('GET', `streams/${id}/publishers`)
    }

    getSubscribers(id) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('GET', `streams/${id}/subscribers`)
    }

    isPublisher(streamId, address) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('GET', `streams/${streamId}/publisher/${address}`)
    }

    isSubscriber(streamId, address) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('GET', `streams/${streamId}/subscriber/${address}`)
    }
}

class Canvases {
    constructor(options) {
        this.options = options
    }

    create(body) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('POST', 'canvases')
            .withBody(body)
    }

    get(id) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('GET', `canvases/${id}`)
    }

    start(id) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('POST', `canvases/${id}/start`)
    }

    stop(id) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('POST', `canvases/${id}/stop`)
    }

    getRuntimeState(id, path) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('POST', `canvases/${id}/${path}/request`)
            .withBody({
                type: 'json'
            })
    }
}

class Subscriptions {
    constructor(options) {
        this.options = options
    }

    create(body) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('POST', 'subscriptions')
            .withBody(body)
    }

    list() {
        return new StreamrApiRequest(this.options)
            .methodAndPath('GET', 'subscriptions')
    }
}

class Login {
    constructor(options) {
        this.options = options
    }

    challenge(address) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('POST', `login/challenge/${address}`)
            .withBody()
    }
}

class IntegrationKeys {
    constructor(options) {
        this.options = options
    }

    create(body) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('POST', 'integration_keys')
            .withBody(body)
    }
}

class Permissions {
    constructor(resourcesName, options) {
        this.resourcesName = resourcesName
        this.options = options
    }

    getOwnPermissions(id) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('GET', `${this.resourcesName}/${id}/permissions/me`)
    }
}

class DataUnions {
    constructor(options) {
        this.options = options
    }

    list() {
        return new StreamrApiRequest(this.options)
            .methodAndPath('GET', 'dataunions')
    }
}

class NotFound {
    constructor(options) {
        this.options = options
    }

    notFound() {
        return new StreamrApiRequest(this.options)
            .methodAndPath('GET', 'page-not-found')
    }

    withApiKey(apiKey) {
        this.authHeader = `Token ${apiKey}`
        return this
    }

    withSessionToken(sessionToken) {
        this.authHeader = `Bearer ${sessionToken}`
        return this
    }
}

module.exports = (baseUrl, logging) => {
    const options = {
        baseUrl,
        logging
    }

    return {
        api: {
            v1: {
                canvases: new Canvases(options),
                categories: new Categories(options),
                integration_keys: new IntegrationKeys(options),
                login: new Login(options),
                products: new Products(options),
                streams: new Streams(options),
                subscriptions: new Subscriptions(options),
                dataunions: new DataUnions(options),
                not_found: new NotFound(options),
            }
        }
    }
}
