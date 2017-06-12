
import type {ApiError} from './common-types'
import type {User} from './user-types'

type resourceType = 'DASHBOARD' | 'CANVAS' | 'STREAM'
type resourceId = string

export type Operation = 'read' | 'write' | 'share'

export type Permission = {
    operation: Operation,
    user: User.email,
    fetching?: boolean,
    new?: boolean,
    removed?: boolean,
    error?: ApiError
}

export type State = {
    byTypeAndId: {
        [resourceType]: {
            [resourceId]: Array<Permission>
        }
    },
    error: ?ApiError,
    fetching: boolean
}