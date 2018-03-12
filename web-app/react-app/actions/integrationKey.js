// @flow

import axios from 'axios'
import {parseError} from './utils/parseApiResponse'
import createLink from '../helpers/createLink'
import {error, success} from 'react-notification-system-redux'
import type {IntegrationKey} from '../flowtype/integration-key-types.js'
import type {ErrorInUi} from '../flowtype/common-types.js'
import ownWeb3 from '../utils/web3Instance'

export const GET_AND_REPLACE_INTEGRATION_KEYS_REQUEST = 'GET_AND_REPLACE_INTEGRATION_KEYS_REQUEST'
export const GET_AND_REPLACE_INTEGRATION_KEYS_SUCCESS = 'GET_AND_REPLACE_INTEGRATION_KEYS_SUCCESS'
export const GET_AND_REPLACE_INTEGRATION_KEYS_FAILURE = 'GET_AND_REPLACE_INTEGRATION_KEYS_FAILURE'

export const GET_INTEGRATION_KEYS_BY_SERVICE_REQUEST = 'GET_INTEGRATION_KEYS_BY_SERVICE_REQUEST'
export const GET_INTEGRATION_KEYS_BY_SERVICE_SUCCESS = 'GET_INTEGRATION_KEYS_BY_SERVICE_SUCCESS'
export const GET_INTEGRATION_KEYS_BY_SERVICE_FAILURE = 'GET_INTEGRATION_KEYS_BY_SERVICE_FAILURE'

export const CREATE_INTEGRATION_KEY_REQUEST = 'CREATE_INTEGRATION_KEY_REQUEST'
export const CREATE_INTEGRATION_KEY_SUCCESS = 'CREATE_INTEGRATION_KEY_SUCCESS'
export const CREATE_INTEGRATION_KEY_FAILURE = 'CREATE_INTEGRATION_KEY_FAILURE'

export const DELETE_INTEGRATION_KEY_REQUEST = 'DELETE_INTEGRATION_KEY_REQUEST'
export const DELETE_INTEGRATION_KEY_SUCCESS = 'DELETE_INTEGRATION_KEY_SUCCESS'
export const DELETE_INTEGRATION_KEY_FAILURE = 'DELETE_INTEGRATION_KEY_FAILURE'

export const CREATE_IDENTITY_REQUEST = 'CREATE_IDENTITY_REQUEST'
export const CREATE_IDENTITY_SUCCESS = 'CREATE_IDENTITY_SUCCESS'
export const CREATE_IDENTITY_FAILURE = 'CREATE_IDENTITY_FAILURE'

const apiUrl = 'api/v1/integration_keys'

export const getAndReplaceIntegrationKeys = () => (dispatch: Function) => {
    dispatch(getAndReplaceIntegrationKeysRequest())
    return axios.get(createLink(apiUrl))
        .then(({data}) => {
            dispatch(getAndReplaceIntegrationKeysSuccess(data))
            dispatch(success({
                message: 'IntegrationKey created successfully!'
            }))
        })
        .catch(res => {
            const e = parseError(res)
            dispatch(getAndReplaceIntegrationKeysFailure(e))
            dispatch(error({
                title: e.message
            }))
            throw e
        })
}

export const getIntegrationKeysByService = (service: $ElementType<IntegrationKey, 'service'>) => (dispatch: Function) => {
    dispatch(getIntegrationKeysByServiceRequest(service))
    return axios.get(createLink(apiUrl), {
        params: {
            service
        }
    })
        .then(({data}) => dispatch(getIntegrationKeysByServiceSuccess(service, data)))
        .catch(res => {
            const e = parseError(res)
            dispatch(getIntegrationKeysByServiceFailure(service, e))
            dispatch(error({
                title: e.message
            }))
            throw e
        })
}

export const createIntegrationKey = (integrationKey: IntegrationKey) => (dispatch: Function) => {
    dispatch(createIntegrationKeyRequest())
    return axios.post(createLink(apiUrl), integrationKey)
        .then(({data}) => dispatch(createIntegrationKeySuccess(data)))
        .catch(res => {
            const e = parseError(res)
            dispatch(createIntegrationKeyFailure(e))
            dispatch(error({
                title: e.message
            }))
            throw e
        })
}

export const deleteIntegrationKey = (id: $ElementType<IntegrationKey, 'id'>) => (dispatch: Function) => {
    if (!id) {
        throw new Error('No id!')
    }
    dispatch(deleteIntegrationKeyRequest(id))
    return axios.delete(createLink(`${apiUrl}/${id}`))
        .then(() => dispatch(deleteIntegrationKeySuccess(id)))
        .catch(res => {
            const e = parseError(res)
            dispatch(deleteIntegrationKeyFailure(e))
            dispatch(error({
                title: e.message
            }))
            throw e
        })
}

export const createIdentity = (integrationKey: IntegrationKey) => (dispatch: Function) => {
    dispatch(createIdentityRequest(integrationKey))
    if (!ownWeb3) {
        dispatch(createIdentityFailure({
            message: 'MetaMask browser extension is not installed'
        }))
        dispatch(error({
            title: 'Create identity failed',
            message: 'MetaMask browser extension is not installed',
        }))
        return
    }
    if (ownWeb3.eth.defaultAccount == null) {
        dispatch(createIdentityFailure({
            message: 'MetaMask browser extension is locked'
        }))
        dispatch(error({
            title: 'Create identity failed',
            message: 'MetaMask browser extension is locked',
        }))
        return
    }
    let challenge
    axios.post(createLink('api/v1/login/challenge')).then(({data}) => {
        challenge = data
        return ownWeb3.eth.personal.sign(data.challenge, ownWeb3.eth.defaultAccount)
    })
        .then((signature) => {
            const input = {
                ...integrationKey,
                signature: signature,
                address: ownWeb3.eth.defaultAccount,
                challenge: {
                    ...challenge
                },
            }
            return axios.post(createLink(apiUrl), input)
                .then((response) => {
                    dispatch(createIdentitySuccess(response.data))
                    dispatch(success({
                        title: 'Success!',
                        message: 'New identity created',
                    }))
                })
        })
        .catch((response) => {
            const err = parseError(response)
            dispatch(createIdentityFailure(err))
            dispatch(error({
                title: 'Create identity failed',
                message: err.message,
            }))
            throw err
        })
}

const getAndReplaceIntegrationKeysRequest = () => ({
    type: GET_AND_REPLACE_INTEGRATION_KEYS_REQUEST,
})

const getIntegrationKeysByServiceRequest = (service: $ElementType<IntegrationKey, 'service'>) => ({
    type: GET_INTEGRATION_KEYS_BY_SERVICE_REQUEST,
    service
})

const createIntegrationKeyRequest = () => ({
    type: CREATE_INTEGRATION_KEY_REQUEST,
})

const deleteIntegrationKeyRequest = (id: $ElementType<IntegrationKey, 'id'>) => ({
    type: DELETE_INTEGRATION_KEY_REQUEST,
    id
})

const getAndReplaceIntegrationKeysSuccess = (integrationKeys: Array<IntegrationKey>) => ({
    type: GET_AND_REPLACE_INTEGRATION_KEYS_SUCCESS,
    integrationKeys
})

const getIntegrationKeysByServiceSuccess = (service: $ElementType<IntegrationKey, 'service'>, integrationKeys: Array<IntegrationKey>) => ({
    type: GET_INTEGRATION_KEYS_BY_SERVICE_SUCCESS,
    integrationKeys,
    service
})

const createIntegrationKeySuccess = (integrationKey: IntegrationKey) => ({
    type: CREATE_INTEGRATION_KEY_SUCCESS,
    integrationKey
})

const deleteIntegrationKeySuccess = (id: $ElementType<IntegrationKey, 'id'>) => ({
    type: DELETE_INTEGRATION_KEY_SUCCESS,
    id
})

const getAndReplaceIntegrationKeysFailure = (error: ErrorInUi) => ({
    type: GET_AND_REPLACE_INTEGRATION_KEYS_FAILURE,
    error
})

const getIntegrationKeysByServiceFailure = (service: string, error: ErrorInUi) => ({
    type: GET_INTEGRATION_KEYS_BY_SERVICE_FAILURE,
    error,
    service
})

const createIntegrationKeyFailure = (error: ErrorInUi) => ({
    type: CREATE_INTEGRATION_KEY_FAILURE,
    error
})

const deleteIntegrationKeyFailure = (error: ErrorInUi) => ({
    type: DELETE_INTEGRATION_KEY_FAILURE,
    error
})

const createIdentityRequest = (integrationKey: IntegrationKey) => ({
    type: CREATE_IDENTITY_REQUEST,
    integrationKey,
})

const createIdentitySuccess = (integrationKey: IntegrationKey) => ({
    type: CREATE_IDENTITY_SUCCESS,
    integrationKey,
})

const createIdentityFailure = (error: ErrorInUi) => ({
    type: CREATE_IDENTITY_FAILURE,
    error,
})
