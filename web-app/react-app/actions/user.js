// @flow

import axios from 'axios'
import parseError from './utils/parseError'

import type {User} from '../types/user-types'

export const GET_CURRENT_USER_REQUEST = 'GET_CURRENT_USER_REQUEST'
export const GET_CURRENT_USER_SUCCESS = 'GET_CURRENT_USER_SUCCESS'
export const GET_CURRENT_USER_FAILURE = 'GET_CURRENT_USER_FAILURE'

export const UPDATE_CURRENT_USER_NAME = 'UPDATE_CURRENT_USER_NAME'
export const UPDATE_CURRENT_USER_TIMEZONE = 'UPDATE_CURRENT_USER_TIMEZONE'

const apiUrl = 'api/v1/users'

declare var Streamr: {
    createLink: Function
}

import type {Err} from './utils/parseError'

export const getCurrentUser = () => (dispatch: Function) => {
    dispatch(getCurrentUserRequest())
    return axios.get(Streamr.createLink({
        uri: `${apiUrl}/me`
    }))
        .then(({data}) => dispatch(getCurrentUserSuccess(data)))
        .catch(res => {
            const e = parseError(res)
            dispatch(getCurrentUserFailure(e))
            throw e
        })
}

export const updateCurrentUserName = (name: string) => ({
    type: UPDATE_CURRENT_USER_NAME,
    name
})

export const updateCurrentUserTimezone = (timezone: string) => ({
    type: UPDATE_CURRENT_USER_TIMEZONE,
    timezone
})

const getCurrentUserRequest = () => ({
    type: GET_CURRENT_USER_REQUEST,
})

const getCurrentUserSuccess = (user: User) => ({
    type: GET_CURRENT_USER_SUCCESS,
    user
})

const getCurrentUserFailure = (error: Err) => ({
    type: GET_CURRENT_USER_FAILURE,
    error
})