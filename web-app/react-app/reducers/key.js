// @flow

import {
    GET_RESOURCE_KEYS_REQUEST,
    GET_RESOURCE_KEYS_SUCCESS,
    GET_RESOURCE_KEYS_FAILURE,
    ADD_RESOURCE_KEY_REQUEST,
    ADD_RESOURCE_KEY_SUCCESS,
    ADD_RESOURCE_KEY_FAILURE,
    REMOVE_RESOURCE_KEY_REQUEST,
    REMOVE_RESOURCE_KEY_SUCCESS,
    REMOVE_RESOURCE_KEY_FAILURE,
} from '../actions/key.js'

import type {KeyState} from '../flowtype/states/key-state'
import type {KeyAction} from '../flowtype/actions/key-actions'

const initialState = {
    byTypeAndId: {},
    error: null,
    fetching: false,
}

export default function(state: KeyState = initialState, action: KeyAction): KeyState {
    switch (action.type) {
        case GET_RESOURCE_KEYS_REQUEST:
        case ADD_RESOURCE_KEY_REQUEST:
        case REMOVE_RESOURCE_KEY_REQUEST:
            return {
                ...state,
                fetching: true,
            }

        case GET_RESOURCE_KEYS_SUCCESS:
            return {
                ...state,
                byTypeAndId: {
                    ...state.byTypeAndId,
                    [action.resourceType]: {
                        ...(state.byTypeAndId[action.resourceType] || {}),
                        [action.resourceId]: action.keys,
                    },
                },
                fetching: false,
                error: null,
            }

        case ADD_RESOURCE_KEY_SUCCESS:
            return {
                ...state,
                byTypeAndId: {
                    ...state.byTypeAndId,
                    [action.resourceType]: {
                        ...(state.byTypeAndId[action.resourceType] || {}),
                        [action.resourceId]: [
                            ...((state.byTypeAndId[action.resourceType] || {})[action.resourceId] || []),
                            action.key,
                        ],
                    },
                },
                fetching: false,
                error: null,
            }

        case REMOVE_RESOURCE_KEY_SUCCESS:
            if (!action.keyId) {
                throw new Error('No keyId provided!')
            }
            return {
                ...state,
                byTypeAndId: {
                    ...state.byTypeAndId,
                    [action.resourceType]: {
                        ...(state.byTypeAndId[action.resourceType] || {}),
                        [action.resourceId]: (state.byTypeAndId[action.resourceType][action.resourceId] || []).filter(key => !action.keyId || key.id !== action.keyId),
                    },
                },
                fetching: false,
                error: null,
            }

        case GET_RESOURCE_KEYS_FAILURE:
        case ADD_RESOURCE_KEY_FAILURE:
        case REMOVE_RESOURCE_KEY_FAILURE:
            return {
                ...state,
                fetching: false,
                error: action.error,
            }

        default:
            return state
    }
}
