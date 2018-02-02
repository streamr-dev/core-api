// @flow

import type {Permission, ResourceType, ResourceId} from '../permission-types.js'
import type {ApiError} from '../common-types.js'

export type PermissionState = {
    byTypeAndId: {
        [ResourceType]: {
            [ResourceId]: Array<Permission>
        }
    },
    error: ?ApiError,
    fetching: boolean
}
