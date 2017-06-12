// @flow

import axios from 'axios'
import parseError from './utils/parseError'
import {showError, showSuccess} from './notification'

export const GET_AND_REPLACE_INTEGRATION_KEYS_REQUEST = 'GET_ALL_INTEGRATION_KEYS_REQUEST'
export const GET_AND_REPLACE_INTEGRATION_KEYS_SUCCESS = 'GET_ALL_INTEGRATION_KEYS_SUCCESS'
export const GET_AND_REPLACE_INTEGRATION_KEYS_FAILURE = 'GET_ALL_INTEGRATION_KEYS_FAILURE'

export const GET_INTEGRATION_KEYS_BY_TYPE_REQUEST = 'GET_INTEGRATION_KEYS_BY_TYPE_REQUEST'
export const GET_INTEGRATION_KEYS_BY_TYPE_SUCCESS = 'GET_INTEGRATION_KEYS_BY_TYPE_SUCCESS'
export const GET_INTEGRATION_KEYS_BY_TYPE_FAILURE = 'GET_INTEGRATION_KEYS_BY_TYPE_FAILURE'

export const CREATE_INTEGRATION_KEY_REQUEST = 'CREATE_INTEGRATION_KEY_REQUEST'
export const CREATE_INTEGRATION_KEY_SUCCESS = 'CREATE_INTEGRATION_KEY_SUCCESS'
export const CREATE_INTEGRATION_KEY_FAILURE = 'CREATE_INTEGRATION_KEY_FAILURE'

export const DELETE_INTEGRATION_KEY_REQUEST = 'DELETE_INTEGRATION_KEY_REQUEST'
export const DELETE_INTEGRATION_KEY_SUCCESS = 'DELETE_INTEGRATION_KEY_SUCCESS'
export const DELETE_INTEGRATION_KEY_FAILURE = 'DELETE_INTEGRATION_KEY_FAILURE'

const apiUrl = 'api/v1/integrationkeys'

declare var Streamr: any

import type {IntegrationKey} from '../flowtype/integration-key-types.js'
import type {ApiError as Err} from '../flowtype/common-types.js'

export const getAndReplaceIntegrationKeys = () => (dispatch: Function) => {
    dispatch(getAndReplaceIntegrationKeysRequest())
    return axios.get(Streamr.createLink({
        uri: apiUrl
    }))
        .then(({data}) => {
            dispatch(getAndReplaceIntegrationKeysSuccess(data))
            dispatch(showSuccess({
                message: 'IntegrationKey created successfully!'
            }))
        })
        .catch(res => {
            const e = parseError(res)
            dispatch(getAndReplaceIntegrationKeysFailure(e))
            dispatch(showError({
                title: e.message
            }))
            throw e
        })
}

export const getIntegrationKeysByService = (service: string) => (dispatch: Function) => {
    dispatch(getIntegrationKeysByServiceRequest(service))
    return axios.get(Streamr.createLink({
        uri: apiUrl
    }), {
        params: {
            service
        }
    })
        .then(({data}) => dispatch(getIntegrationKeysByServiceSuccess(service, data)))
        .catch(res => {
            const e = parseError(res)
            dispatch(getIntegrationKeysByServiceFailure(service, e))
            dispatch(showError({
                title: e.message
            }))
            throw e
        })
}

export const createIntegrationKey = (integrationKey: IntegrationKey) => (dispatch: Function) => {
    dispatch(createIntegrationKeyRequest())
    return axios.post(Streamr.createLink({
        uri: apiUrl
    }), integrationKey)
        .then(({data}) => dispatch(createIntegrationKeySuccess(data)))
        .catch(res => {
            const e = parseError(res)
            dispatch(createIntegrationKeyFailure(e))
            dispatch(showError({
                title: e.message
            }))
            throw e
        })
}

export const deleteIntegrationKey = (id: string) => (dispatch: Function) => {
    dispatch(deleteIntegrationKeyRequest(id))
    return axios.delete(Streamr.createLink({
        uri: `${apiUrl}/${id}`
    }))
        .then(() => dispatch(deleteIntegrationKeySuccess(id)))
        .catch(res => {
            const e = parseError(res)
            dispatch(deleteIntegrationKeyFailure(e))
            dispatch(showError({
                title: e.message
            }))
            throw e
        })
}

const getAndReplaceIntegrationKeysRequest = () => ({
    type: GET_AND_REPLACE_INTEGRATION_KEYS_REQUEST,
})

const getIntegrationKeysByServiceRequest = (service: string) => ({
    type: GET_INTEGRATION_KEYS_BY_TYPE_REQUEST,
    service
})

const createIntegrationKeyRequest = () => ({
    type: CREATE_INTEGRATION_KEY_REQUEST,
})

const deleteIntegrationKeyRequest = (id: string) => ({
    type: DELETE_INTEGRATION_KEY_REQUEST,
    id
})

const getAndReplaceIntegrationKeysSuccess = (integrationKeys: Array<IntegrationKey>) => ({
    type: GET_AND_REPLACE_INTEGRATION_KEYS_SUCCESS,
    integrationKeys
})

const getIntegrationKeysByServiceSuccess = (service: string, integrationKeys: Array<IntegrationKey>) => ({
    type: GET_INTEGRATION_KEYS_BY_TYPE_SUCCESS,
    integrationKeys,
    service
})

const createIntegrationKeySuccess = (integrationKey: IntegrationKey) => ({
    type: CREATE_INTEGRATION_KEY_SUCCESS,
    integrationKey
})

const deleteIntegrationKeySuccess = (id: string) => ({
    type: DELETE_INTEGRATION_KEY_SUCCESS,
    id
})

const getAndReplaceIntegrationKeysFailure = (error: Err) => ({
    type: GET_AND_REPLACE_INTEGRATION_KEYS_FAILURE,
    error
})

const getIntegrationKeysByServiceFailure = (service: string, error: Err) => ({
    type: GET_INTEGRATION_KEYS_BY_TYPE_FAILURE,
    error,
    service
})

const createIntegrationKeyFailure = (error: Err) => ({
    type: CREATE_INTEGRATION_KEY_FAILURE,
    error
})

const deleteIntegrationKeyFailure = (error: Err) => ({
    type: DELETE_INTEGRATION_KEY_FAILURE,
    error
})
