// @flow

import type {Key, ResourceId, ResourceType} from '../key-types'
import type {ApiError} from '../common-types'

export type KeyState = {
    byTypeAndId: {
        [ResourceType]: {
            [ResourceId]: Array<Key>
        }
    },
    error?: ?ApiError,
    fetching: boolean
}
