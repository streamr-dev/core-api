// @flow

import type {Key, ResourceType, ResourceId} from '../key-types'
import type {ApiError} from '../common-types'

import {
    GET_RESOURCE_KEYS_REQUEST,
    GET_RESOURCE_KEYS_SUCCESS,
    GET_RESOURCE_KEYS_FAILURE,
    ADD_RESOURCE_KEY_REQUEST,
    ADD_RESOURCE_KEY_SUCCESS,
    ADD_RESOURCE_KEY_FAILURE,
    REMOVE_RESOURCE_KEY_REQUEST,
    REMOVE_RESOURCE_KEY_SUCCESS,
    REMOVE_RESOURCE_KEY_FAILURE
} from '../../actions/key'

export type KeyAction = {
    type: typeof GET_RESOURCE_KEYS_REQUEST
        | typeof ADD_RESOURCE_KEY_REQUEST
        | typeof REMOVE_RESOURCE_KEY_REQUEST
} | {
    type: typeof GET_RESOURCE_KEYS_SUCCESS,
    resourceType: ResourceType,
    resourceId: ResourceId,
    keys: Array<Key>
} | {
    type: typeof ADD_RESOURCE_KEY_SUCCESS,
    resourceType: ResourceType,
    resourceId: ResourceId,
    key: Key
} | {
    type: typeof REMOVE_RESOURCE_KEY_SUCCESS,
    resourceType: ResourceType,
    resourceId: ResourceId,
    keyId: $ElementType<Key, 'id'>
} | {
    type: typeof GET_RESOURCE_KEYS_FAILURE
        | typeof ADD_RESOURCE_KEY_FAILURE
        | typeof REMOVE_RESOURCE_KEY_FAILURE,
    error: ApiError
}
