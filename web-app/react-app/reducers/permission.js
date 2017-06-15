// @flow

import {
    GET_RESOURCE_PERMISSIONS_REQUEST,
    GET_RESOURCE_PERMISSIONS_SUCCESS,
    GET_RESOURCE_PERMISSIONS_FAILURE,
    ADD_RESOURCE_PERMISSION,
    REMOVE_RESOURCE_PERMISSION,
    SAVE_ADDED_RESOURCE_PERMISSION_REQUEST,
    SAVE_ADDED_RESOURCE_PERMISSION_SUCCESS,
    SAVE_ADDED_RESOURCE_PERMISSION_FAILURE,
    SAVE_REMOVED_RESOURCE_PERMISSION_REQUEST,
    SAVE_REMOVED_RESOURCE_PERMISSION_SUCCESS,
    SAVE_REMOVED_RESOURCE_PERMISSION_FAILURE
} from '../actions/permission.js'

import type {State, Action} from '../flowtype/permission-types'

const initialState = {
    byTypeAndId: {},
    error: null,
    fetching: false
}

const modifyPermission = (state, action, attributes) => {
    const permissions = state.byTypeAndId[action.resourceType][action.resourceId]
    if (!action.permission) {
        return permissions
    }
    return [...permissions].map(permission => {
        if ((permission.id != null && permission.id === action.permission.id) || (permission.anonymous === true && action.permission.anonymous === true) || (permission.user === action.permission.user && permission.operation === action.permission.operation)) {
            return {
                ...permission,
                ...attributes
            }
        } else {
            return permission
        }
    })
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
                        ...(state.byTypeAndId[action.resourceType] || {}),
                        [action.resourceId]: action.permissions.map(permission => ({
                            ...permission,
                            new: false,
                            fetching: false,
                            removed: false,
                            error: null
                        }))
                    }
                },
                fetching: false,
                error: null
            }
            
        case GET_RESOURCE_PERMISSIONS_FAILURE:
            return {
                ...state,
                fetching: false,
                error: action.error
            }
    
        case ADD_RESOURCE_PERMISSION: {
            const byResourceType = state.byTypeAndId[action.resourceType] || {}
            const byResourceId = (byResourceType[action.resourceId] || []).filter(permission => {
                return (permission.id != null && permission.id !== action.permission.id) || (permission.anonymous && !action.permission.anonymous) || (action.permission.anonymous && !permission.anonymous) || (permission.user !== action.permission.user) || (permission.operation !== action.permission.operation)
            })
            return {
                ...state,
                byTypeAndId: {
                    ...state.byTypeAndId,
                    [action.resourceType]: {
                        ...state.byTypeAndId[action.resourceType],
                        [action.resourceId]: [...byResourceId, {
                            ...action.permission,
                            new: true,
                            fetching: false,
                            removed: false,
                            error: null
                        }]
                    }
                },
                fetching: false,
                error: null
            }
            
        }
        
        case REMOVE_RESOURCE_PERMISSION:
            return {
                ...state,
                byTypeAndId: {
                    ...state.byTypeAndId,
                    [action.resourceType]: {
                        ...state.byTypeAndId[action.resourceType],
                        [action.resourceId]: modifyPermission(state, action, {
                            removed: true,
                            fetching: false,
                            error: null
                        })
                    }
                },
                fetching: false,
                error: null
            }
            
        case SAVE_REMOVED_RESOURCE_PERMISSION_REQUEST:
            return {
                ...state,
                byTypeAndId: {
                    ...state.byTypeAndId,
                    [action.resourceType]: {
                        ...state.byTypeAndId[action.resourceType],
                        [action.resourceId]: modifyPermission(state, action, {
                            fetching: true
                        })
                    }
                },
                fetching: false,
                error: null
            }
    
        case SAVE_REMOVED_RESOURCE_PERMISSION_SUCCESS: {
            return {
                ...state,
                byTypeAndId: {
                    ...state.byTypeAndId,
                    [action.resourceType]: {
                        [action.resourceId]: modifyPermission(state, action, {
                            fetching: false,
                            ...action.permission
                        })
                    }
                },
                fetching: false,
                error: null
            }
        }
    
        case SAVE_REMOVED_RESOURCE_PERMISSION_FAILURE:
            return {
                ...state,
                byTypeAndId: {
                    ...state.byTypeAndId,
                    [action.resourceType]: {
                        ...state.byTypeAndId[action.resourceType],
                        [action.resourceId]: modifyPermission(state, action, {
                            removed: false,
                            new: false,
                            fetching: false,
                            error: action.permission.error
                        })
                    }
                },
                fetching: false,
                error: null
            }
        
        case SAVE_ADDED_RESOURCE_PERMISSION_REQUEST:
            return {
                ...state,
                byTypeAndId: {
                    ...state.byTypeAndId,
                    [action.resourceType]: {
                        ...state.byTypeAndId[action.resourceType],
                        [action.resourceId]: modifyPermission(state, action, {
                            fetching: true
                        })
                    }
                },
                fetching: false,
                error: null
            }
    
        case SAVE_ADDED_RESOURCE_PERMISSION_SUCCESS:
            return {
                ...state,
                byTypeAndId: {
                    ...state.byTypeAndId,
                    [action.resourceType]: {
                        ...state.byTypeAndId[action.resourceType],
                        [action.resourceId]: modifyPermission(state, action, {
                            fetching: false,
                            new: false
                        })
                    }
                },
                fetching: false,
                error: null
            }
    
        case SAVE_ADDED_RESOURCE_PERMISSION_FAILURE:
            return {
                ...state,
                byTypeAndId: {
                    ...state.byTypeAndId,
                    [action.resourceType]: {
                        ...state.byTypeAndId[action.resourceType],
                        [action.resourceId]: modifyPermission(state, action, {
                            fetching: false,
                            error: action.permission.error
                        })
                    }
                },
                fetching: false,
                error: null
            }
            
        default:
            return state
    }
}
