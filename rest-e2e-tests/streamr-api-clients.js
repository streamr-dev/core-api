const fetch = require('node-fetch')
const url = require('url')
const querystring = require('querystring')

class StreamrApiRequest {
    constructor(options) {
        this.baseUrl = options.baseUrl || 'https://www.streamr.com/api/v1/'
        this.logging = options.logging || false
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

        if (this.logging) {
            console.info(this.method, apiUrl, this.body ? '\n' + JSON.stringify(JSON.parse(this.body), null, 4) : '')
        }

        return fetch(apiUrl, {
            method: this.method,
            body: this.body,
            headers: {
                'Accept': 'application/json',
                'Content-type': 'application/json',
                'Authorization': this.authToken
            }
        })
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

    setDeploying(id, body) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('POST', `products/${id}/setDeploying`)
            .withBody(body)
    }

    setDeployed(id, body) {
        return new StreamrApiRequest(this.options)
            .methodAndPath('POST', `products/${id}/setDeployed`)
            .withBody(body)
    }

    setUndeploying() {

    }

    setUndeployed() {

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
                products: new Products(options)
            }
        }
    }
}