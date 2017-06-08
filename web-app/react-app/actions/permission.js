// @flow

import axios from 'axios'
import path from 'path'
import parseError from './utils/parseError'

export const GET_RESOURCE_PERMISSIONS_REQUEST = 'GET_RESOURCE_PERMISSIONS_REQUEST'
export const GET_RESOURCE_PERMISSIONS_SUCCESS = 'GET_RESOURCE_PERMISSIONS_SUCCESS'
export const GET_RESOURCE_PERMISSIONS_FAILURE = 'GET_RESOURCE_PERMISSIONS_FAILURE'

export const ADD_RESOURCE_PERMISSION = 'ADD_RESOURCE_PERMISSION'
export const REMOVE_RESOURCE_PERMISSION = 'REMOVE_RESOURCE_PERMISSION'

export const UPDATE_AND_SAVE_RESOURCE_PERMISSION_REQUEST = 'UPDATE_AND_SAVE_RESOURCE_PERMISSIONS_REQUEST'
export const UPDATE_AND_SAVE_RESOURCE_PERMISSION_SUCCESS = 'UPDATE_AND_SAVE_RESOURCE_PERMISSIONS_SUCCESS'
export const UPDATE_AND_SAVE_RESOURCE_PERMISSION_FAILURE = 'UPDATE_AND_SAVE_RESOURCE_PERMISSIONS_FAILURE'

export const SET_MODULE_CHECKED = 'SET_MODULE_CHECKED'

const getApiUrl = (resourceType, resourceId) => {
    const urlPartsByResourceType = {
        DASHBOARD: 'dashboards',
        CANVAS: 'canvases',
        STREAM: 'streams'
    }
    return path.resolve('api/v1', urlPartsByResourceType[resourceType], resourceId)
}

declare var Streamr: {
    createLink: Function
}

import type { ApiError } from '../flowtype/common-types'
import type { Permission } from '../flowtype/permission-types'

export const getResourcePermissions = (resourceType: Permission.resourceType, resourceId: Permission.resourceId) => (dispatch: Function) => {
    dispatch(getResourcePermissionsRequest())
    axios.get(Streamr.createLink({
        uri: `${getApiUrl(resourceType, resourceId)}/permissions`
    }))
        .then(({data}) => dispatch(getResourcePermissionsSuccess(data)))
        .catch(res => {
            const e = parseError(res)
            dispatch(getResourcePermissionsFailure(e))
            throw e
        })
}

export const addResourcePermission = (resourceId: Permission.resourceId, permission: Permission) => ({
    type: ADD_RESOURCE_PERMISSION,
    resourceId,
    permission
})

export const removeResourcePermission = (resourceId: Permission.resourceId, permissionId: Permission.id) => ({
    type: REMOVE_RESOURCE_PERMISSION,
    resourceId,
    permissionId
})

export const updateAndSaveResourcePermissions = (resourceType: Permission.resourceType, resourceId: Permission.resourceId, permissions: Array<Permission>) => (dispatch: Function) => {
    return new Promise((resolve, reject) => {
        Promise.all(permissions.map(permission => {
            dispatch(getResourcePermissionsRequest(resourceType, resourceId, permission))
            return axios.post(Streamr.createLink({
                uri: `${getApiUrl(resourceType, resourceId)}/permissions`
            }), permission)
                .then(
                    value => ({
                        state: 'fullfilled',
                        value
                    }),
                    value => ({
                        state: 'rejected',
                        value
                    })
                )
        }))
            .then(results => {
                const fullfilled = results.map(r => r.state === 'fulfilled')
                const rejected = results.map(r => r.state === 'rejected')
                //fullfilled.forEach(p => dispatch(updateAndSaveResourcePermissionSuccess(p.value)))
                //rejected.forEach(p => dispatch(updateAndSaveResourcePermissionFailure(p.value)))
            })
    })
}

const getResourcePermissionsRequest = (resourceType: Permission.resourceType, resourceId: Permission.resourceId) => ({
    type: GET_RESOURCE_PERMISSIONS_REQUEST,
    resourceType,
    resourceId
})

const getResourcePermissionsSuccess = (permissions: {
    [Permission.id]: Permission
}) => ({
    type: GET_RESOURCE_PERMISSIONS_SUCCESS,
    permissions
})

const getResourcePermissionsFailure = (error: ApiError) => ({
    type: GET_RESOURCE_PERMISSIONS_FAILURE,
    error
})

const updateAndSaveResourcePermissionRequest = (permissions: {
    [Permission.id]: Permission
}) => ({
    type: GET_RESOURCE_PERMISSIONS_REQUEST,
    permissions
})

const updateAndSaveResourcePermissionSuccess = (permission: Permission) => ({
    type: UPDATE_AND_SAVE_RESOURCE_PERMISSION_SUCCESS,
    permission
})

const updateAndSaveResourcePermissionFailure = (error: ApiError) => ({
    type: UPDATE_AND_SAVE_RESOURCE_PERMISSION_FAILURE,
    error
})