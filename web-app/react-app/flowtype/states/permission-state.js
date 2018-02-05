// @flow

import type {Permission, ResourceType, ResourceId} from '../permission-types.js'
import type {ErrorInUi} from '../common-types.js'

export type PermissionState = {
    byTypeAndId: {
        [ResourceType]: {
            [ResourceId]: Array<Permission>
        }
    },
    error: ?ErrorInUi,
    fetching: boolean
}
