// @flow

import axios from 'axios'
import parseError from './utils/parseError'

import {showSuccess} from './notification'

import type {ApiError} from '../flowtype/common-types'
import type {User} from '../flowtype/user-types'

export const GET_CURRENT_USER_REQUEST = 'GET_CURRENT_USER_REQUEST'
export const GET_CURRENT_USER_SUCCESS = 'GET_CURRENT_USER_SUCCESS'
export const GET_CURRENT_USER_FAILURE = 'GET_CURRENT_USER_FAILURE'

export const SAVE_CURRENT_USER_REQUEST = 'SAVE_CURRENT_USER_REQUEST'
export const SAVE_CURRENT_USER_SUCCESS = 'SAVE_CURRENT_USER_SUCCESS'
export const SAVE_CURRENT_USER_FAILURE = 'SAVE_CURRENT_USER_FAILURE'

export const UPDATE_CURRENT_USER = 'UPDATE_CURRENT_USER'

const apiUrl = 'api/v1/users'

declare var Streamr: {
    createLink: Function
}

export const getCurrentUser = () => (dispatch: Function) => {
    dispatch(getCurrentUserRequest())
    return axios.get(Streamr.createLink({
        uri: `${apiUrl}/me`
    }))
        .then(({data}) => {
            dispatch(getCurrentUserSuccess(data))
            dispatch(showSuccess({
                title: 'Success!',
                message: 'Profile saved'
            }))
        })
        .catch(res => dispatch(getCurrentUserFailure(parseError(res))))
}

export const saveCurrentUser = (user: User) => (dispatch: Function) => {
    dispatch(saveCurrentUserRequest())
    return axios.post(Streamr.createLink({
        uri: 'profile/update'
    }), user)
        .then(({data}) => dispatch(saveCurrentUserSuccess(data)))
        .catch(res => dispatch(saveCurrentUserFailure(parseError(res))))
}

const updateCurrentUser = (user: User) => ({
    type: UPDATE_CURRENT_USER,
    user
})

export const updateCurrentUserName = (name: string) => updateCurrentUser({
    name
})

export const updateCurrentUserTimezone = (timezone: string) => updateCurrentUser({
    timezone
})

const getCurrentUserRequest = () => ({
    type: GET_CURRENT_USER_REQUEST,
})

const getCurrentUserSuccess = (user: User) => ({
    type: GET_CURRENT_USER_SUCCESS,
    user
})

const getCurrentUserFailure = (error: ApiError) => ({
    type: GET_CURRENT_USER_FAILURE,
    error
})

const saveCurrentUserRequest = () => ({
    type: SAVE_CURRENT_USER_REQUEST,
})

const saveCurrentUserSuccess = (user: User) => ({
    type: SAVE_CURRENT_USER_SUCCESS,
    user
})

const saveCurrentUserFailure = (error: ApiError) => ({
    type: SAVE_CURRENT_USER_FAILURE,
    error
})