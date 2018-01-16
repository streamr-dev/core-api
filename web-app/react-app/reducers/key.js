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
    REMOVE_RESOURCE_KEY_FAILURE
} from '../actions/key.js'

import type {State, Action, Key, ResourceType, ResourceId} from '../flowtype/key-types'

const initialState = {
    byTypeAndId: {},
    error: null,
    fetching: false
}

export default function(state: State = initialState, action: Action) : State {
    switch (action.type) {
        case GET_RESOURCE_KEYS_REQUEST:
        case ADD_RESOURCE_KEY_REQUEST:
        case REMOVE_RESOURCE_KEY_REQUEST:
            return {
                ...state,
                fetching: true
            }
            
        case GET_RESOURCE_KEYS_SUCCESS:
            return {
                ...state,
                byTypeAndId: {
                    ...state.byTypeAndId,
                    [action.resourceType]: {
                        ...(state.byTypeAndId[action.resourceType] || {}),
                        [action.resourceId]: action.keys
                    }
                },
                fetching: false,
                error: null
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
                            action.key
                        ]
                    }
                },
                fetching: false,
                error: null
            }
            
        case REMOVE_RESOURCE_KEY_SUCCESS:
            return {
                ...state,
                byTypeAndId: {
                    ...state.byTypeAndId,
                    [action.resourceType]: {
                        ...(state.byTypeAndId[action.resourceType] || {}),
                        [action.resourceId]: (state.byTypeAndId[action.resourceType][action.resourceId] || []).filter(key => key.id !== action.keyId)
                    }
                },
                fetching: false,
                error: null
            }
            
        case GET_RESOURCE_KEYS_FAILURE:
        case ADD_RESOURCE_KEY_FAILURE:
        case REMOVE_RESOURCE_KEY_FAILURE:
            return {
                ...state,
                fetching: false,
                error: action.error
            }
            
        default:
            return state
    }
}
