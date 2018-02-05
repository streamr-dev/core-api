// @flow

import axios from 'axios'
import path from 'path'
import {parseError} from './utils/parseApiResponse'
import createLink from '../helpers/createLink'

import {error} from 'react-notification-system-redux'

export const GET_RESOURCE_KEYS_REQUEST = 'GET_RESOURCE_KEYS_REQUEST'
export const GET_RESOURCE_KEYS_SUCCESS = 'GET_RESOURCE_KEYS_SUCCESS'
export const GET_RESOURCE_KEYS_FAILURE = 'GET_RESOURCE_KEYS_FAILURE'

export const ADD_RESOURCE_KEY_REQUEST = 'ADD_RESOURCE_KEY_REQUEST'
export const ADD_RESOURCE_KEY_SUCCESS = 'ADD_RESOURCE_KEY_SUCCESS'
export const ADD_RESOURCE_KEY_FAILURE = 'ADD_RESOURCE_KEY_FAILURE'

export const REMOVE_RESOURCE_KEY_REQUEST = 'REMOVE_RESOURCE_KEY_REQUEST'
export const REMOVE_RESOURCE_KEY_SUCCESS = 'REMOVE_RESOURCE_KEY_SUCCESS'
export const REMOVE_RESOURCE_KEY_FAILURE = 'REMOVE_RESOURCE_KEY_FAILURE'

import type {Key, ResourceType, ResourceId} from '../flowtype/key-types'
import type {ErrorInUi} from '../flowtype/common-types'

export const getResourceKeys = (resourceType: ResourceType, resourceId: ResourceId) => (dispatch: Function) => {
    dispatch(getResourceKeysRequest())
    return axios.get(createLink(getApiUrl(resourceType, resourceId)))
        .then(({data}) => dispatch(getResourceKeysSuccess(resourceType, resourceId, data)))
        .catch(res => {
            const e = parseError(res)
            dispatch(getResourceKeysFailure(e))
            dispatch(error({
                title: 'Error!',
                message: e.message
            }))
            throw e
        })
}

export const addResourceKey = (resourceType: ResourceType, resourceId: ResourceId, key: Key) => (dispatch: Function) => {
    dispatch(addResourceKeyRequest())
    return axios.post(createLink(getApiUrl(resourceType, resourceId)), key)
        .then(({data}) => dispatch(addResourceKeySuccess(resourceType, resourceId, data)))
        .catch(res => {
            const e = parseError(res)
            dispatch(addResourceKeyFailure(e))
            dispatch(error({
                title: 'Error!',
                message: e.message
            }))
            throw e
        })
}

export const removeResourceKey = (resourceType: ResourceType, resourceId: ResourceId, keyId: $ElementType<Key, 'id'>) => (dispatch: Function) => {
    dispatch(removeResourceKeyRequest())
    return axios.delete(createLink(getApiUrl(resourceType, resourceId, keyId)))
        .then(() => dispatch(removeResourceKeySuccess(resourceType, resourceId, keyId)))
        .catch(res => {
            const e = parseError(res)
            dispatch(removeResourceKeyFailure(e))
            dispatch(error({
                title: 'Error!',
                message: e.message
            }))
            throw e
        })
}

const getApiUrl = (resourceType: ResourceType, resourceId: ResourceId, keyId?: $ElementType<Key, 'id'>) => {
    const urlPart = {
        STREAM: 'streams',
        USER: 'users'
    }[resourceType]
    if (!urlPart) {
        throw new Error(`Invalid resource type: ${resourceType}`)
    }
    return path.resolve('/api/v1', urlPart, resourceId, 'keys', keyId || '')
}

const getResourceKeysRequest = () => ({
    type: GET_RESOURCE_KEYS_REQUEST
})

const getResourceKeysSuccess = (resourceType: ResourceType, resourceId: ResourceId, keys: Array<Key>) => ({
    type: GET_RESOURCE_KEYS_SUCCESS,
    resourceType,
    resourceId,
    keys
})

const getResourceKeysFailure = (error: ErrorInUi) => ({
    type: GET_RESOURCE_KEYS_FAILURE,
    error
})

const addResourceKeyRequest = () => ({
    type: ADD_RESOURCE_KEY_REQUEST
})

const addResourceKeySuccess = (resourceType: ResourceType, resourceId: ResourceId, key: Key) => ({
    type: ADD_RESOURCE_KEY_SUCCESS,
    resourceType,
    resourceId,
    key
})

const addResourceKeyFailure = (error: ErrorInUi) => ({
    type: ADD_RESOURCE_KEY_FAILURE,
    error
})

const removeResourceKeyRequest = () => ({
    type: REMOVE_RESOURCE_KEY_REQUEST
})

const removeResourceKeySuccess = (resourceType: ResourceType, resourceId: ResourceId, keyId: $ElementType<Key, 'id'>) => ({
    type: REMOVE_RESOURCE_KEY_SUCCESS,
    resourceType,
    resourceId,
    keyId
})

const removeResourceKeyFailure = (error: ErrorInUi) => ({
    type: REMOVE_RESOURCE_KEY_FAILURE,
    error
})
