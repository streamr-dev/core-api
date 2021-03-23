import fetch from 'node-fetch'
import url from 'url'
import querystring from 'querystring'
import FormData from 'form-data'
import { getSessionToken } from './test-utilities'
import { EthereumAccount } from './EthereumAccount'

class StreamrApiRequest {

    baseUrl: string
    logging: boolean
    authenticatedUser?: EthereumAccount
    contentType?: string
    queryParams: string
    headers: any
    relativePath?: string
    methodId?: string
    body?: any

    constructor(options: any) {
        this.baseUrl = options.baseUrl
        this.logging = options.logging
        this.authenticatedUser = undefined
        this.contentType = undefined
        this.queryParams = ''
        this.headers = {}
    }

    method(methodId: string) {
        this.methodId = methodId
        return this
    }

    endpoint(...pathParts: string[]) {
        this.relativePath = pathParts.map(part => encodeURIComponent(part)).join('/')
        return this
    }

    withAuthenticatedUser(authenticatedUser: EthereumAccount|undefined) {
        this.authenticatedUser = authenticatedUser
        return this
    }

    withQueryParams(queryParams: any) {
        if (queryParams) {
            this.queryParams = '?' + querystring.stringify(queryParams)
        }
        return this
    }

    withBody(body: any) {
        this.body = JSON.stringify(body)
        this.contentType = 'application/json'
        return this
    }

    withFormData(formData: any) {
        this.body = formData
        this.headers = Object.assign(this.headers, formData.getHeaders())
        return this
    }

    withRawBody(body: any) {
        this.body = body
        this.contentType = undefined
        return this
    }

    withHeader(key: string, value: string) {
        this.headers = {...this.headers}
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
            ...this.headers,
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
                '\n\n',
            )
        }

        return fetch(apiUrl, {
            method: this.methodId,
            body: this.body,
            headers: headers,
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

    options: any

    constructor(options: any) {
        this.options = options
    }

    list() {
        return new StreamrApiRequest(this.options)
            .method('GET')
            .endpoint('categories')
    }
}

class Products {

    options: any
    permissions: Permissions

    constructor(options: any) {
        this.options = options
        this.permissions = new Permissions('products', options)
    }

    list(queryParams: any) {
        return new StreamrApiRequest(this.options)
            .method('GET')
            .endpoint('products')
            .withQueryParams(queryParams)
    }

    get(id: string) {
        return new StreamrApiRequest(this.options)
            .method('GET')
            .endpoint('products', id)
    }

    create(body: any) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('products')
            .withBody(body)
    }

    update(id: string, body: any) {
        return new StreamrApiRequest(this.options)
            .method('PUT')
            .endpoint('products', id)
            .withBody(body)
    }

    setDeploying(id: string) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('products', id, 'setDeploying')
    }

    setDeployed(id: string, body: any) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('products', id, 'setDeployed')
            .withBody(body)
    }

    setUndeploying(id: string) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('products', id, 'setUndeploying')
    }

    setUndeployed(id: string, body: any) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('products', id, 'setUndeployed')
            .withBody(body)
    }

    deployFree(id: string) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('products', id, 'deployFree')
    }

    undeployFree(id: string) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('products', id, 'undeployFree')
    }

    uploadImage(id: string, fileBytes: any) {
        const formData = new FormData()
        formData.append('file', fileBytes)

        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('products', id, 'images')
            .withFormData(formData)
    }

    listStreams(id: string) {
        return new StreamrApiRequest(this.options)
            .method('GET')
            .endpoint('products', id, 'streams')
    }

    addStream(id: string, streamId: string) {
        return new StreamrApiRequest(this.options)
            .method('PUT')
            .endpoint('products', id, 'streams', streamId)
    }

    removeStream(id: string, streamId: string) {
        return new StreamrApiRequest(this.options)
            .method('DELETE')
            .endpoint('products', id, 'streams', streamId)
    }
}

class Streams {

    options: any
    permissions: Permissions

    constructor(options: any) {
        this.options = options
        this.permissions = new Permissions('streams', options)
    }

    setFields(id: string, body: any) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('streams', id, 'fields')
            .withBody(body)
    }

    grantPublic(id: string, operation: string) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('streams', id, 'permissions')
            .withBody({
                anonymous: true,
                operation,
            })
    }

    grant(id: string, targetUser: string, operation: string) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('streams', id, 'permissions')
            .withBody({
                user: targetUser,
                operation,
            })
    }

    getOwnPermissions(id: string) {
        return new StreamrApiRequest(this.options)
            .method('GET')
            .endpoint('streams', id, 'permissions', 'me')
    }

    delete(streamId: string, permissionId: number) {
        return new StreamrApiRequest(this.options)
            .method('DELETE')
            .endpoint('streams', streamId, 'permissions', String(permissionId))
    }

    list(queryParams: any) {
        return new StreamrApiRequest(this.options)
            .method('GET')
            .endpoint('streams')
            .withQueryParams(queryParams)
    }
}

class Canvases {

    options: any

    constructor(options: any) {
        this.options = options
    }

    create(body: any) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('canvases')
            .withBody(body)
    }

    get(id: string) {
        return new StreamrApiRequest(this.options)
            .method('GET')
            .endpoint('canvases', id)
    }

    start(id: string) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('canvases', id, 'start')
    }

    stop(id: string) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('canvases', id, 'stop')
    }

    getRuntimeState(id: string, path: string) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('canvases', id, path, 'request')
            .withBody({
                type: 'json',
            })
    }
}

class Subscriptions {

    options: any

    constructor(options: any) {
        this.options = options
    }

    create(body: any) {
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

class IntegrationKeys {

    options: any

    constructor(options: any) {
        this.options = options
    }

    create(body: any) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('integration_keys')
            .withBody(body)
    }
}

class Permissions {

    resourcesName: string
    options: any

    constructor(resourcesName: string, options: any) {
        this.resourcesName = resourcesName
        this.options = options
    }

    getOwnPermissions(id: string) {
        return new StreamrApiRequest(this.options)
            .method('GET')
            .endpoint(this.resourcesName, id, 'permissions', 'me')
    }
}

class DataUnions {

    options: any

    constructor(options: any) {
        this.options = options
    }

    list() {
        return new StreamrApiRequest(this.options)
            .method('GET')
            .endpoint('dataunions')
    }

    approveJoinRequest(id: string, contractAddress: string) {
        return new StreamrApiRequest(this.options)
            .method('PUT')
            .endpoint('dataunions', contractAddress, 'joinRequests', id)
            .withBody({state: 'ACCEPTED'})
    }
}

class StorageNodes {

    options: any

    constructor(options: any) {
        this.options = options
    }

    findStreamsByStorageNode(address: string) {
        return new StreamrApiRequest(this.options)
            .method('GET')
            .endpoint('storageNodes', address, 'streams')
    }

    findStorageNodesByStream(id: string) {
        return new StreamrApiRequest(this.options)
            .method('GET')
            .endpoint('streams', id, 'storageNodes')
    }

    addStorageNodeToStream(storageNodeAddress: string, streamId: string) {
        return new StreamrApiRequest(this.options)
            .method('POST')
            .endpoint('streams', streamId, 'storageNodes')
            .withBody({address: storageNodeAddress})
    }

    removeStorageNodeFromStream(storageNodeAddress: string, streamId: string) {
        return new StreamrApiRequest(this.options)
            .method('DELETE')
            .endpoint('streams', streamId, 'storageNodes', storageNodeAddress)
    }
}

class NotFound {

    options: any

    constructor(options: any) {
        this.options = options
    }

    notFound() {
        return new StreamrApiRequest(this.options)
            .method('GET')
            .endpoint('page-not-found')
    }
}

const LOGGING_ENABLED = process.env.LOGGING_ENABLED || false
const options = {
    baseUrl: 'http://localhost/api/v1/',
    logging: LOGGING_ENABLED,
}
export default {
    api: {
        v1: {
            canvases: new Canvases(options),
            categories: new Categories(options),
            integration_keys: new IntegrationKeys(options),
            products: new Products(options),
            streams: new Streams(options),
            subscriptions: new Subscriptions(options),
            dataunions: new DataUnions(options),
            storagenodes: new StorageNodes(options),
            not_found: new NotFound(options),
        },
    },
}
