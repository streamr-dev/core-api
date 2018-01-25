
import type {ErrorInUi} from './common-types'
import type {User} from './user-types'

type resourceType = 'DASHBOARD' | 'CANVAS' | 'STREAM'
type resourceId = string

type Operation = 'read' | 'write' | 'share'

export type Permission = {
    operation: Operation,
    user: User.email,
    anonymous?: boolean,
    fetching?: boolean,
    new?: boolean,
    removed?: boolean,
    error?: ErrorInUi
}

export type State = {
    byTypeAndId: {
        [resourceType]: {
            [resourceId]: Array<Permission>
        }
    },
    error: ?ErrorInUi,
    fetching: boolean
}