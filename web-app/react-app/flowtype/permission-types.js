// @flow

import type {ApiError} from './common-types'
import type {User} from './user-types'

export type ResourceType = 'DASHBOARD' | 'CANVAS' | 'STREAM'
export type ResourceId = string

export type Operation = 'read' | 'write' | 'share'

export type Permission = {
    id?: number,
    operation: Operation,
    user: ?$ElementType<User, 'email'>,
    resourceTitle?: string,
    anonymous?: boolean,
    fetching?: boolean,
    new?: boolean,
    removed?: boolean,
    error?: ApiError
}
