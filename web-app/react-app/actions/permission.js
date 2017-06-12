// @flow

import axios from 'axios'
import path from 'path'
import settle from 'promise-settle'
import parseError from './utils/parseError'

import {showError} from './notification'

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

declare var Streamr: {
    createLink: Function
}

import type { ApiError } from '../flowtype/common-types'
import type { Permission } from '../flowtype/permission-types'

export const getResourcePermissions = (resourceType: Permission.resourceType, resourceId: Permission.resourceId) => (dispatch: Function) => {
    dispatch(getResourcePermissionsRequest())
    const uri = `${getApiUrl(resourceType, resourceId)}/permissions`
    return axios.get(Streamr.createLink({
        uri
    }))
        .then(({data}) => dispatch(getResourcePermissionsSuccess(resourceType, resourceId, data)))
        .catch(res => {
            const e = parseError(res)
            dispatch(getResourcePermissionsFailure(e))
            throw e
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

export const saveUpdatedResourcePermissions = (resourceType: Permission.resourceType, resourceId: Permission.resourceId, permissions: Array<Permission>) => (dispatch: Function) => {
    const addedPermissions = permissions.filter(p => p.new)
    const addPermissions = new Promise(resolve => {
        settle(addedPermissions.map(permission => {
            dispatch(saveAddedResourcePermissionRequest(resourceType, resourceId, permission))
            return axios.post(Streamr.createLink({
                uri: `${getApiUrl(resourceType, resourceId)}/permissions`
            }), permission)
        }))
            .then(results => {
                const fullfilled = results.filter(r => r.isFulfilled()).map(r => r.value())
                const rejected = results.filter(r => !r.isFulfilled()).map(r => r.reason())
            
                fullfilled.forEach(({data}) => dispatch(saveAddedResourcePermissionSuccess(resourceType, resourceId, data)))
                rejected.forEach((res, i) => dispatch(saveAddedResourcePermissionFailure(resourceType, resourceId, {
                    ...addedPermissions[i],
                    error: parseError(res)
                })))
            
                resolve([fullfilled, rejected])
            })
    })

    const removedPermissions = permissions.filter(p => p.removed)
    const removePermissions = new Promise(resolve => {
        settle(removedPermissions.map(permission => {
            dispatch(saveRemovedResourcePermissionRequest(resourceType, resourceId, permission))
            return axios.delete(Streamr.createLink({
                uri: `${getApiUrl(resourceType, resourceId)}/permissions/${permission.id}`
            }), permission)
        }))
            .then(results => {
                const fullfilled = results.filter(r => r.isFulfilled()).map(r => r.value())
                const rejected = results.filter(r => !r.isFulfilled()).map(r => r.reason())
        
                fullfilled.forEach((firstParam, i) => dispatch(saveRemovedResourcePermissionSuccess(resourceType, resourceId, removedPermissions[i])))
                rejected.forEach((res, i) => dispatch(saveRemovedResourcePermissionFailure(resourceType, resourceId, {
                    ...removedPermissions[i],
                    error: parseError(res)
                })))
        
                resolve([fullfilled, rejected])
            })
    })
    
    return Promise.all([addPermissions, removePermissions])
        .then(([[, addedFailed], [, removedFailed]]) => {
            let message
            if (addedFailed.length) {
                message = 'Something went wrong while adding some of the permission(s)'
            } else if (removedFailed.length) {
                message = 'Something went wrong while revoking some of the permission(s)'
            }
            if (message) {
                const e = new Error(message)
                dispatch(showError({
                    title: 'Error!',
                    message
                }))
                throw e
            }
        })
}

const getResourcePermissionsRequest = () => ({
    type: GET_RESOURCE_PERMISSIONS_REQUEST
})

const getResourcePermissionsSuccess = (resourceType: Permission.resourceType, resourceId: Permission.resourceId, permissions: {
    [Permission.id]: Permission
}) => ({
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