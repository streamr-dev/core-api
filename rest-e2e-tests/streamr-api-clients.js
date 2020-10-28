const fetch = require('node-fetch')
const url = require('url')
const querystring = require('querystring')
const FormData = require('form-data')
const StreamrClient = require('streamr-client')
const getSessionToken = require('./test-utilities.js').getSessionToken

class StreamrApiRequest {
    constructor(options) {
        this.baseUrl = options.baseUrl || 'https://www.streamr.com/api/v1/'
        if (!this.baseUrl.endsWith('/')) {
            this.baseUrl += '/'
        }

        this.logging = options.logging || false
        this.authenticatedUser = null
        this.contentType = null
        this.queryParams = ''
        this.headers = {}
    }

    method(methodId) {
        this.methodId = methodId
        return this
    }

    endpoint(...pathParts) {
        this.relativePath = pathParts.map(part => encodeURIComponent(part)).join('/')
        return this
    }

    withAuthenticatedUser(authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
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

    withFormData(formData) {
        this.body = formData
        this.headers = Object.assign(this.headers, formData.getHeaders())
        return this
    }

    withRawBody(body) {
        this.body = body
        this.contentType = null
        return this
    }

    withHeader(key, value) {
		this.headers = { ...this.headers }
		this.headers[key] = value
        return this
    }

    async call() {
        if (!this.methodId) {
            throw 'Method not set.'
        }
        if (!this.relativePath) {
            throw 'Relative path not set.'
        }

        const apiUrl = url.resolve(this.baseUrl, this.relativePath) + this.queryParams

        let headers = {
			'Accept': 'application/json',
			...this.headers
        }
        if (this.body && this.contentType) {
            headers['Content-type'] = this.contentType
        }
        if (this.authenticatedUser) {
            const sessionToken = await getSessionToken(this.authenticatedUser)
            headers['Authorization'] = `Bearer ${sessionToken}`
        }

        if (this.logging) {
            console.info(
                this.methodId,
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
            method: this.methodId,
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
            .method('GET')
            .endpoint('categories')
    }
}

class Products {
    constructor(options) {
        this.options = options
        this.permissions = new Permissions('products', options)
    }

    list(queryParams) {
        return new StreamrApiRequest(this.options)
            .method('GET')
            .endpoint('products')
            .withQueryParams(queryParams)
    }

    get(id) {
        return new StreamrApiRequest(this.options)
            .method('GET')
            .endpoint('products', id)
    }

    create(body) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('products')
            .withBody(body)
    }

    update(id, body) {
        return new StreamrApiRequest(this.options)
            .method('PUT')
            .endpoint('products', id)
            .withBody(body)
    }

    setDeploying(id) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('products', id, 'setDeploying')
    }

    setDeployed(id, body) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('products', id, 'setDeployed')
            .withBody(body)
    }

    setUndeploying(id) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('products', id, 'setUndeploying')
    }

    setUndeployed(id, body) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('products', id, 'setUndeployed')
            .withBody(body)
    }

    deployFree(id) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('products', id, 'deployFree')
    }

    undeployFree(id) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('products', id, 'undeployFree')
    }

    uploadImage(id, fileBytes) {
        const formData = new FormData()
        formData.append('file', fileBytes)

        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('products', id, 'images')
            .withFormData(formData)
    }

    listStreams(id) {
        return new StreamrApiRequest(this.options)
            .method('GET')
            .endpoint('products', id, 'streams')
    }

    addStream(id, streamId) {
        return new StreamrApiRequest(this.options)
            .method('PUT')
            .endpoint('products', id, 'streams', streamId)
    }

    removeStream(id, streamId) {
        return new StreamrApiRequest(this.options)
            .method('DELETE')
            .endpoint('products', id, 'streams', streamId)
    }
}

class Streams {
    constructor(options) {
        this.options = options
        this.permissions = new Permissions('streams', options)
    }

    get(id) {
        return new StreamrApiRequest(this.options)
            .method('GET')
            .endpoint('streams', id)
    }

    setFields(id, body) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('streams', id, 'fields')
            .withBody(body)
    }

    grantPublic(id, operation) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('streams', id, 'permissions')
            .withBody({
                anonymous: true,
                operation,
            })
    }

    grant(id, targetUser, operation) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('streams', id, 'permissions')
            .withBody({
                user: targetUser,
                operation,
            })
    }

    getOwnPermissions(id) {
        return new StreamrApiRequest(this.options)
            .method('GET')
            .endpoint('streams', id, 'permissions', 'me')
    }

    getValidationInfo(id) {
        return new StreamrApiRequest(this.options)
            .method('GET')
            .endpoint('streams', id, 'validation')
    }

    getPublishers(id) {
        return new StreamrApiRequest(this.options)
            .method('GET')
            .endpoint('streams', id, 'publishers')
    }

    getSubscribers(id) {
        return new StreamrApiRequest(this.options)
            .method('GET')
            .endpoint('streams', id, 'subscribers')
    }

    isPublisher(streamId, address) {
        return new StreamrApiRequest(this.options)
            .method('GET')
            .endpoint('streams', streamId, 'publisher', address)
    }

    isSubscriber(streamId, address) {
        return new StreamrApiRequest(this.options)
            .method('GET')
            .endpoint('streams', streamId, 'subscriber', address)
    }

    delete(streamId, permissionId) {
        return new StreamrApiRequest(this.options)
            .method('DELETE')
            .endpoint('streams', streamId, 'permissions', permissionId)
    }
}

class Canvases {
    constructor(options) {
        this.options = options
    }

    create(body) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('canvases')
            .withBody(body)
    }

    get(id) {
        return new StreamrApiRequest(this.options)
            .method('GET')
            .endpoint('canvases', id)
    }

    start(id) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('canvases', id, 'start')
    }

    stop(id) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('canvases', id, 'stop')
    }

    getRuntimeState(id, path) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('canvases', id, path, 'request')
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
            .method('POST')
            .endpoint('subscriptions')
            .withBody(body)
    }

    list() {
        return new StreamrApiRequest(this.options)
            .method('GET')
            .endpoint('subscriptions')
    }
}

class Login {
    constructor(options) {
        this.options = options
    }

    challenge(address) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('login', 'challenge', address)
            .withBody()
    }
}

class IntegrationKeys {
    constructor(options) {
        this.options = options
    }

    create(body) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('integration_keys')
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
            .method('GET')
            .endpoint(this.resourcesName, id, 'permissions', 'me')
    }
}

class DataUnions {
    constructor(options) {
        this.options = options
    }

    list() {
        return new StreamrApiRequest(this.options)
            .method('GET')
            .endpoint('dataunions')
    }
}

class StorageNodes {
    constructor(options) {
        this.options = options
    }

    findStreamsByStorageNode(address) {
        return new StreamrApiRequest(this.options)
            .method('GET')
            .endpoint('storageNodes', address, 'streams')
    }

    findStorageNodesByStream(id) {
        return new StreamrApiRequest(this.options)
            .method('GET')
            .endpoint('streams', id, 'storageNodes')
    }

    addStorageNodeToStream(storageNodeAddress, streamId) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('streams', streamId, 'storageNodes')
            .withBody({address: storageNodeAddress})
    }

    removeStorageNodeFromStream(storageNodeAddress, streamId) {
        return new StreamrApiRequest(this.options)
            .method('DELETE')
            .endpoint('streams', streamId, 'storageNodes', storageNodeAddress)
    }
}

class NotFound {
    constructor(options) {
        this.options = options
    }

    notFound() {
        return new StreamrApiRequest(this.options)
            .method('GET')
            .endpoint('page-not-found')
    }
}

module.exports = (baseUrl, logging) => {
    const options = {
        // Append a trailing "/" if not present
        baseUrl: baseUrl.slice(-1) === "/" ? baseUrl : baseUrl + '/',
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
                storagenodes: new StorageNodes(options),
                not_found: new NotFound(options),
            }
        }
    }
}
