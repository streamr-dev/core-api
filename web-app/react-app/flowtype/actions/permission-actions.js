// @flow

import type {Permission, ResourceType, ResourceId} from '../permission-types'
import type {ErrorInUi} from '../common-types'

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
} from '../../actions/permission'

export type PermissionAction = {
    type: typeof GET_RESOURCE_PERMISSIONS_REQUEST
} | {
    type: typeof ADD_RESOURCE_PERMISSION
        | typeof REMOVE_RESOURCE_PERMISSION
        | typeof SAVE_ADDED_RESOURCE_PERMISSION_REQUEST
        | typeof SAVE_ADDED_RESOURCE_PERMISSION_SUCCESS
        | typeof SAVE_ADDED_RESOURCE_PERMISSION_FAILURE
        | typeof SAVE_REMOVED_RESOURCE_PERMISSION_REQUEST
        | typeof SAVE_REMOVED_RESOURCE_PERMISSION_SUCCESS
        | typeof SAVE_REMOVED_RESOURCE_PERMISSION_FAILURE,
    resourceType: ResourceType,
    resourceId: ResourceId,
    permission: Permission
} |{
    type: typeof GET_RESOURCE_PERMISSIONS_SUCCESS,
    resourceType: ResourceType,
    resourceId: ResourceId,
    permissions: Array<Permission>
} | {
    type: typeof GET_RESOURCE_PERMISSIONS_FAILURE,
    error: ErrorInUi
}
