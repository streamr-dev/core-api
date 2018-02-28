// @flow

import type {Key, ResourceId, ResourceType} from '../key-types'
import type {ErrorInUi} from '../common-types'

export type KeyState = {
    byTypeAndId: {
        [ResourceType]: {
            [ResourceId]: Array<Key>
        }
    },
    error?: ?ErrorInUi,
    fetching: boolean
}
