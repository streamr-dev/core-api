// @flow

import type {User} from '../user-types.js'
import type {ApiError} from '../common-types'

export type UserState = {
    currentUser: ?User,
    error: ?ApiError,
    fetching: boolean,
    saved: boolean
}
