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

import type {State, Action, Permission} from '../flowtype/permission-types'

const initialState = {
    byTypeAndId: {},
    error: null,
    fetching: false
}

const permEquals = (p1: Permission, p2: Permission) : boolean => {
    return (p1.id != null && p1.id === p2.id) ||
        (p1.anonymous === true && p2.anonymous === true) ||
        (p1.user && p1.operation && p1.user === p2.user && p1.operation === p2.operation)
}

const modifyPermission = (state, action, attributes) : Array<Permission> => {
    const permissions = state.byTypeAndId[action.resourceType][action.resourceId]
    if (!action.permission) {
        return permissions
    }
    return [...permissions].map(permission => {
        if (permEquals(permission, action.permission)) {
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
            const byResourceId = (byResourceType[action.resourceId] || []).filter(permission => !permEquals(permission, action.permission))
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
                        [action.resourceId]: state.byTypeAndId[action.resourceType][action.resourceId].filter(permission => !permEquals(permission, action.permission))
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
