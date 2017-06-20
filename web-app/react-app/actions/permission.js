// @flow

import axios from 'axios'
import path from 'path'
import settle from 'promise-settle'
import parseError from './utils/parseError'
import createLink from '../createLink'

import {showError, showSuccess} from './notification'

export const GET_RESOURCE_PERMISSIONS_REQUEST = 'GET_RESOURCE_PERMISSIONS_REQUEST'
export const GET_RESOURCE_PERMISSIONS_SUCCESS = 'GET_RESOURCE_PERMISSIONS_SUCCESS'
export const GET_RESOURCE_PERMISSIONS_FAILURE = 'GET_RESOURCE_PERMISSIONS_FAILURE'

export const ADD_RESOURCE_PERMISSION = 'ADD_RESOURCE_PERMISSION'
export const REMOVE_RESOURCE_PERMISSION = 'REMOVE_RESOURCE_PERMISSION'

export const SAVE_ADDED_RESOURCE_PERMISSION_REQUEST = 'SAVE_ADDED_RESOURCE_PERMISSIONS_REQUEST'
export const SAVE_ADDED_RESOURCE_PERMISSION_SUCCESS = 'SAVE_ADDED_RESOURCE_PERMISSIONS_SUCCESS'
export const SAVE_ADDED_RESOURCE_PERMISSION_FAILURE = 'SAVE_ADDED_RESOURCE_PERMISSIONS_FAILURE'

export const SAVE_REMOVED_RESOURCE_PERMISSION_REQUEST = 'SAVE_REMOVED_RESOURCE_PERMISSIONS_REQUEST'
export const SAVE_REMOVED_RESOURCE_PERMISSION_SUCCESS = 'SAVE_REMOVED_RESOURCE_PERMISSIONS_SUCCESS'
export const SAVE_REMOVED_RESOURCE_PERMISSION_FAILURE = 'SAVE_REMOVED_RESOURCE_PERMISSIONS_FAILURE'

const getApiUrl = (resourceType, resourceId) => {
    const urlPartsByResourceType = {
        DASHBOARD: 'dashboards',
        CANVAS: 'canvases',
        STREAM: 'streams'
    }
    return path.resolve('/api/v1', urlPartsByResourceType[resourceType], resourceId)
}

import type { ApiError } from '../flowtype/common-types'
import type { Permission } from '../flowtype/permission-types'
import type { User } from '../flowtype/user-types'

export const getResourcePermissions = (resourceType: Permission.resourceType, resourceId: Permission.resourceId) => (dispatch: Function) => {
    dispatch(getResourcePermissionsRequest())
    return axios.get(createLink(`${getApiUrl(resourceType, resourceId)}/permissions`))
        .then(({data}) => dispatch(getResourcePermissionsSuccess(resourceType, resourceId, data)))
        .catch(res => {
            const e = parseError(res)
            dispatch(getResourcePermissionsFailure(e))
            throw e
        })
}

export const setResourceHighestOperationForUser = (resourceType: Permission.resourceType, resourceId: Permission.resourceId, user: User.email, operation: Permission.operation) => (dispatch: Function, getState: Function) => {
    const state = getState()
    const currentPermissions = (state.permission.byTypeAndId[resourceType] && state.permission.byTypeAndId[resourceType][resourceId] || []).filter(p => p.user === user)
    const operationsInOrder = ['read', 'write', 'share']
    const index = operationsInOrder.indexOf(operation)
    const addOperations = operationsInOrder.slice(0, index + 1)
    const removeOperations = operationsInOrder.slice(index + 1, operationsInOrder.length)
    addOperations.forEach(o => {
        if (!currentPermissions.find(item => item.operation === o)) {
            dispatch(addResourcePermission(resourceType, resourceId, {
                user,
                operation: o
            }))
        }
    })
    removeOperations.forEach(o => {
        const permission = currentPermissions.find(item => {
            return item.operation === o
        })
        if (permission) {
            dispatch(removeResourcePermission(resourceType, resourceId, permission))
        }
    })
}

export const addResourcePermission = (resourceType: Permission.resourceType, resourceId: Permission.resourceId, permission: Permission) => ({
    type: ADD_RESOURCE_PERMISSION,
    resourceType,
    resourceId,
    permission
})

export const removeResourcePermission = (resourceType: Permission.resourceType, resourceId: Permission.resourceId, permission: Permission) => ({
    type: REMOVE_RESOURCE_PERMISSION,
    resourceType,
    resourceId,
    permission
})

export const removeAllResourcePermissionsByUser = (resourceType: Permission.resourceType, resourceId: Permission.resourceId, user: User.email) => (dispatch: Function) => {
    ['read', 'write', 'share'].forEach(operation => {
        dispatch(removeResourcePermission(resourceType, resourceId, {
            user,
            operation
        }))
    })
    
}

export const saveUpdatedResourcePermissions = (resourceType: Permission.resourceType, resourceId: Permission.resourceId) => (dispatch: Function, getState: Function) => {
    const state = getState()
    const permissions = state.permission.byTypeAndId[resourceType] && state.permission.byTypeAndId[resourceType][resourceId] || []

    const addedPermissions = permissions.filter(p => p.new)
    const addPermissions = new Promise(resolve => {
        settle(addedPermissions.map(permission => {
            dispatch(saveAddedResourcePermissionRequest(resourceType, resourceId, permission))
            return axios.post(createLink(`${getApiUrl(resourceType, resourceId)}/permissions`), permission)
        }))
            .then(results => {
                results.forEach((res, i) => {
                    if (!res.isFulfilled()) {
                        const reason = res.reason()
                        dispatch(saveAddedResourcePermissionFailure(resourceType, resourceId, {
                            ...addedPermissions[i],
                            error: parseError(reason)
                        }))
                    } else {
                        dispatch(saveAddedResourcePermissionSuccess(resourceType, resourceId, addedPermissions[i]))
                    }
                })
                resolve(results)
            })
    })

    const removedPermissions = permissions.filter(p => p.removed)
    const removePermissions = new Promise(resolve => {
        settle(removedPermissions.map(permission => {
            dispatch(saveRemovedResourcePermissionRequest(resourceType, resourceId, permission))
            return axios.delete(createLink(`${getApiUrl(resourceType, resourceId)}/permissions/${permission.id}`), permission)
        }))
            .then(results => {
                results.forEach((res, i) => {
                    if (!res.isFulfilled()) {
                        const reason = res.reason()
                        dispatch(saveRemovedResourcePermissionFailure(resourceType, resourceId, {
                            ...removedPermissions[i],
                            error: parseError(reason)
                        }))
                    } else {
                        dispatch(saveRemovedResourcePermissionSuccess(resourceType, resourceId, removedPermissions[i]))
                    }
                })
                resolve(results)
            })
    })
    
    return new Promise((resolve, reject) => {
        Promise.all([addPermissions, removePermissions])
            .then(([added, removed]) => {
                let message
                if (added.filter(p => !p.isFulfilled()).length) {
                    message = 'Something went wrong while adding some of the permission(s)'
                } else if (removed.filter(p => !p.isFulfilled()).length) {
                    message = 'Something went wrong while revoking some of the permission(s)'
                }
                if (message) {
                    const e = new Error(message)
                    dispatch(showError({
                        title: 'Error!',
                        message
                    }))
                    reject(e)
                } else {
                    resolve()
                    dispatch(showSuccess({
                        title: 'Permissions saved successfully!'
                    }))
                }
            })
    })
}

const getResourcePermissionsRequest = () => ({
    type: GET_RESOURCE_PERMISSIONS_REQUEST
})

const getResourcePermissionsSuccess = (resourceType: Permission.resourceType, resourceId: Permission.resourceId, permissions: Array<Permission>) => ({
    type: GET_RESOURCE_PERMISSIONS_SUCCESS,
    resourceType,
    resourceId,
    permissions
})

const getResourcePermissionsFailure = (error: ApiError) => ({
    type: GET_RESOURCE_PERMISSIONS_FAILURE,
    error
})

const saveAddedResourcePermissionRequest = (resourceType: Permission.resourceType, resourceId: Permission.resourceId, permission: Permission) => ({
    type: SAVE_ADDED_RESOURCE_PERMISSION_REQUEST,
    resourceType,
    resourceId,
    permission
})

const saveAddedResourcePermissionSuccess = (resourceType: Permission.resourceType, resourceId: Permission.resourceId, permission: Permission) => ({
    type: SAVE_ADDED_RESOURCE_PERMISSION_SUCCESS,
    resourceType,
    resourceId,
    permission
})

const saveAddedResourcePermissionFailure = (resourceType: Permission.resourceType, resourceId: Permission.resourceId, permission: Permission) => ({
    type: SAVE_ADDED_RESOURCE_PERMISSION_FAILURE,
    resourceType,
    resourceId,
    permission
})

const saveRemovedResourcePermissionRequest = (resourceType: Permission.resourceType, resourceId: Permission.resourceId, permission: Permission) => ({
    type: SAVE_REMOVED_RESOURCE_PERMISSION_REQUEST,
    resourceType,
    resourceId,
    permission
})

const saveRemovedResourcePermissionSuccess = (resourceType: Permission.resourceType, resourceId: Permission.resourceId, permission: Permission) => ({
    type: SAVE_REMOVED_RESOURCE_PERMISSION_SUCCESS,
    resourceType,
    resourceId,
    permission
})

const saveRemovedResourcePermissionFailure = (resourceType: Permission.resourceType, resourceId: Permission.resourceId, permission: Permission) => ({
    type: SAVE_REMOVED_RESOURCE_PERMISSION_FAILURE,
    resourceType,
    resourceId,
    permission
})