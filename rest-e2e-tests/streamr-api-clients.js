const fetch = require('node-fetch')
const url = require('url')
const querystring = require('querystring')

class StreamrApiRequest {
    constructor(options) {
        this.baseUrl = options.baseUrl || 'https://www.streamr.com/api/v1/'
        this.logging = options.logging || false
        this.authToken = null
        this.queryParams = ''
    }

    methodAndPath(method, relativePath) {
        this.method = method
        this.relativePath = relativePath
        return this
    }

    withAuthToken(authToken) {
        this.authToken = `Token ${authToken}`
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
        return this
    }

    call() {
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
        if (this.body) {
            headers['Content-type'] = 'application/json'
        }
        if (this.authToken) {
            headers['Authorization'] = this.authToken
        }

        if (this.logging) {
            console.info(
                this.method,
                apiUrl,
                '\n\n' + Object.keys(headers)
                    .map(key => `${key}: ${headers[key]}`)
                    .join('\n'),
                this.body ? '\n\n' + JSON.stringify(JSON.parse(this.body), null, 4) : '',
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
    }

    create(body) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('POST', 'streams')
            .withBody(body)
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

    challenge() {
        return new StreamrApiRequest(this.options)
            .methodAndPath('POST', 'login/challenge')
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

module.exports = (baseUrl, logging) => {
    const options = {
        baseUrl,
        logging
    }

    return {
        api: {
            v1: {
                categories: new Categories(options),
                integration_keys: new IntegrationKeys(options),
                login: new Login(options),
                products: new Products(options),
                streams: new Streams(options),
                subscriptions: new Subscriptions(options)
            }
        }
    }
}