// @flow

import type {User} from '../user-types'
import type {ErrorInUi} from '../common-types'

import {
    GET_CURRENT_USER_REQUEST,
    GET_CURRENT_USER_SUCCESS,
    GET_CURRENT_USER_FAILURE,
    SAVE_CURRENT_USER_REQUEST,
    SAVE_CURRENT_USER_SUCCESS,
    SAVE_CURRENT_USER_FAILURE,
    UPDATE_CURRENT_USER
} from '../../actions/user'

export type UserAction = {
    type: typeof GET_CURRENT_USER_REQUEST
        | typeof SAVE_CURRENT_USER_REQUEST
} | {
    type: typeof UPDATE_CURRENT_USER
        | typeof GET_CURRENT_USER_SUCCESS
        | typeof SAVE_CURRENT_USER_SUCCESS,
    user: User
} | {
    type: typeof GET_CURRENT_USER_FAILURE
        | typeof SAVE_CURRENT_USER_FAILURE
        | typeof SAVE_CURRENT_USER_FAILURE,
    error: ErrorInUi
}
