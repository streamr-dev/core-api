// @flow

import {
    GET_RESOURCE_PERMISSIONS_REQUEST,
    GET_RESOURCE_PERMISSIONS_SUCCESS,
    GET_RESOURCE_PERMISSIONS_FAILURE,
    ADD_RESOURCE_PERMISSION,
    REMOVE_RESOURCE_PERMISSION,
    UPDATE_AND_SAVE_RESOURCE_PERMISSION_REQUEST,
    UPDATE_AND_SAVE_RESOURCE_PERMISSION_SUCCESS,
    UPDATE_AND_SAVE_RESOURCE_PERMISSION_FAILURE
} from '../actions/permission.js'

import type {State, Action} from '../flowtype/permission-types'

const initialState = {
    byTypeAndId: {},
    statesByTypeAndId: {},
    error: null,
    fetching: false
}

export default function(state: State = initialState, action: Action) : State {
    switch (action.type) {
        case GET_RESOURCE_PERMISSIONS_REQUEST:
            return {
                ...state,
                fetching: true
            }
        case GET_RESOURCE_PERMISSIONS_SUCCESS:
            return {
                ...state,
                byTypeAndId: {
                    ...state.byTypeAndId,
                    [action.resourceType]: {
                        ...state.byTypeAndId[action.resourceType],
                        [action.resourceId]: {
                            permissions: action.permissions,
                            saving: false
                        }
                    }
                },
                fetching: false,
                error: null
            }
        case GET_RESOURCE_PERMISSIONS_FAILURE:
            return {
                ...state,
                error: action.error
            }
        case UPDATE_AND_SAVE_RESOURCE_PERMISSION_REQUEST:
            return {
                ...state,
                byTypeAndId: {
                    ...state.byTypeAndId,
                    [action.resourceType]: {
                        ...state.byTypeAndId[action.resourceType],
                        [action.resourceId]: {
                            ...state.byTypeAndId[action.resourceType][action.resourceId],
                            saving: true,
                            error: null
                        }
                    }
                },
                fetching: false,
                error: null
            }
            
        case UPDATE_AND_SAVE_RESOURCE_PERMISSION_FAILURE:
            return {
                ...state,
                byTypeAndId: {
                    ...state.byTypeAndId,
                    [action.resourceType]: {
                        ...state.byTypeAndId[action.resourceType],
                        [action.resourceId]: {
                            ...state.byTypeAndId[action.resourceType][action.resourceId],
                            saving: false,
                            error: action.error
                        }
                    }
                },
                fetching: false,
                error: null
            }
        default:
            return state
    }
}
