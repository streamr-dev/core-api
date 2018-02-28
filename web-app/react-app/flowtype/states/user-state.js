// @flow

import type {User} from '../user-types.js'
import type {ErrorInUi} from '../common-types'

export type UserState = {
    currentUser: ?User,
    error: ?ErrorInUi,
    fetching: boolean,
    saved: boolean
}
