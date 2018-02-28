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
    SAVE_REMOVED_RESOURCE_PERMISSION_FAILURE,
} from '../actions/permission.js'

import type {Permission, ResourceType, ResourceId} from '../flowtype/permission-types'
import type {PermissionState} from '../flowtype/states/permission-state'
import type {PermissionAction} from '../flowtype/actions/permission-actions'

const initialState = {
    byTypeAndId: {},
    error: null,
    fetching: false,
}

const permEquals = (p1: ?Permission, p2: ?Permission): boolean => {
    return !!p1 && !!p2 && ((p1.id != null && p1.id === p2.id) ||
        (p1.anonymous === true && p2.anonymous === true) ||
        (p1.user !== undefined && p1.operation && p1.user === p2.user && p1.operation === p2.operation))
}

const modifyPermission = (byTypeAndId: $ElementType<PermissionState, 'byTypeAndId'>, resourceType: ResourceType, resourceId: ResourceId, permission: Permission, attributes: {}): Array<Permission> => {
    const permissions = byTypeAndId[resourceType] && byTypeAndId[resourceType][resourceId] || []
    return [...permissions].map(p2 => {
        if (permEquals(p2, permission)) {
            return {
                ...p2,
                ...attributes,
            }
        } else {
            return p2
        }
    })
}

export default function(state: PermissionState = initialState, action: PermissionAction): PermissionState {
    switch (action.type) {
        case GET_RESOURCE_PERMISSIONS_REQUEST:
            return {
                ...state,
                fetching: true,
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
                            error: null,
                        })),
                    },
                },
                fetching: false,
                error: null,
            }

        case GET_RESOURCE_PERMISSIONS_FAILURE:
            return {
                ...state,
                fetching: false,
                error: action.error,
            }

        case ADD_RESOURCE_PERMISSION: {
            const perm = action.permission || null
            const byResourceType = state.byTypeAndId[action.resourceType] || {}
            const byResourceId = action.permission && byResourceType[action.resourceId] && byResourceType[action.resourceId].filter(permission => !perm || !permEquals(permission, perm))
            return perm ? {
                ...state,
                byTypeAndId: {
                    ...state.byTypeAndId,
                    [action.resourceType]: {
                        ...state.byTypeAndId[action.resourceType],
                        [action.resourceId]: [...(byResourceId || []), {
                            ...action.permission,
                            new: true,
                            fetching: false,
                            removed: false,
                            error: null,
                        }],
                    },
                },
                fetching: false,
                error: null,
            } : state
        }

        case REMOVE_RESOURCE_PERMISSION:
            return {
                ...state,
                byTypeAndId: {
                    ...state.byTypeAndId,
                    [action.resourceType]: {
                        ...state.byTypeAndId[action.resourceType],
                        [action.resourceId]: modifyPermission(state.byTypeAndId, action.resourceType, action.resourceId, action.permission, {
                            removed: true,
                            fetching: false,
                            error: null,
                        }),
                    },
                },
                fetching: false,
                error: null,
            }

        case SAVE_REMOVED_RESOURCE_PERMISSION_REQUEST:
            return {
                ...state,
                byTypeAndId: {
                    ...state.byTypeAndId,
                    [action.resourceType]: {
                        ...state.byTypeAndId[action.resourceType],
                        [action.resourceId]: modifyPermission(state.byTypeAndId, action.resourceType, action.resourceId, action.permission, {
                            fetching: true,
                        }),
                    },
                },
                fetching: false,
                error: null,
            }

        case SAVE_REMOVED_RESOURCE_PERMISSION_SUCCESS: {
            const perm = action.permission || null
            return {
                ...state,
                byTypeAndId: {
                    ...state.byTypeAndId,
                    [action.resourceType]: {
                        [action.resourceId]: state.byTypeAndId[action.resourceType][action.resourceId].filter(permission => !perm || !permEquals(permission, perm)),
                    },
                },
                fetching: false,
                error: null,
            }
        }

        case SAVE_REMOVED_RESOURCE_PERMISSION_FAILURE:
            return {
                ...state,
                byTypeAndId: {
                    ...state.byTypeAndId,
                    [action.resourceType]: {
                        ...state.byTypeAndId[action.resourceType],
                        [action.resourceId]: modifyPermission(state.byTypeAndId, action.resourceType, action.resourceId, action.permission, {
                            removed: false,
                            new: false,
                            fetching: false,
                            error: action.permission.error,
                        }),
                    },
                },
                fetching: false,
                error: null,
            }

        case SAVE_ADDED_RESOURCE_PERMISSION_REQUEST:
            return {
                ...state,
                byTypeAndId: {
                    ...state.byTypeAndId,
                    [action.resourceType]: {
                        ...state.byTypeAndId[action.resourceType],
                        [action.resourceId]: modifyPermission(state.byTypeAndId, action.resourceType, action.resourceId, action.permission, {
                            fetching: true,
                        }),
                    },
                },
                fetching: false,
                error: null,
            }

        case SAVE_ADDED_RESOURCE_PERMISSION_SUCCESS:
            return {
                ...state,
                byTypeAndId: {
                    ...state.byTypeAndId,
                    [action.resourceType]: {
                        ...state.byTypeAndId[action.resourceType],
                        [action.resourceId]: modifyPermission(state.byTypeAndId, action.resourceType, action.resourceId, action.permission, {
                            fetching: false,
                            new: false,
                        }),
                    },
                },
                fetching: false,
                error: null,
            }

        case SAVE_ADDED_RESOURCE_PERMISSION_FAILURE:
            return {
                ...state,
                byTypeAndId: {
                    ...state.byTypeAndId,
                    [action.resourceType]: {
                        ...state.byTypeAndId[action.resourceType],
                        [action.resourceId]: modifyPermission(state.byTypeAndId, action.resourceType, action.resourceId, action.permission, {
                            fetching: false,
                            error: action.permission.error,
                        }),
                    },
                },
                fetching: false,
                error: null,
            }

        default:
            return state
    }
}
