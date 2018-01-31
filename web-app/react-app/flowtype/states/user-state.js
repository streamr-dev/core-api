// @flow

import type {User} from '../user-types.js'

export type UserState = {
    currentUser: User,
    error?: ?string,
    fetching?: boolean,
    saved: boolean
}
