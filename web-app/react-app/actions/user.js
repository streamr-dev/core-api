// @flow

import axios from 'axios'
import parseError from './utils/parseError'
import createLink from '../helpers/createLink'

import {showSuccess, showError} from './notification'

import type {ApiError} from '../flowtype/common-types'
import type {User} from '../flowtype/user-types'

export const GET_CURRENT_USER_REQUEST = 'GET_CURRENT_USER_REQUEST'
export const GET_CURRENT_USER_SUCCESS = 'GET_CURRENT_USER_SUCCESS'
export const GET_CURRENT_USER_FAILURE = 'GET_CURRENT_USER_FAILURE'

export const SAVE_CURRENT_USER_REQUEST = 'SAVE_CURRENT_USER_REQUEST'
export const SAVE_CURRENT_USER_SUCCESS = 'SAVE_CURRENT_USER_SUCCESS'
export const SAVE_CURRENT_USER_FAILURE = 'SAVE_CURRENT_USER_FAILURE'

export const UPDATE_CURRENT_USER = 'UPDATE_CURRENT_USER'

const apiUrl = '/api/v1/users'

export const getCurrentUser = () => (dispatch: Function) => {
    dispatch(getCurrentUserRequest())
    return axios.get(createLink(`${apiUrl}/me`))
        .then(({data}) => dispatch(getCurrentUserSuccess(data)))
        .catch(res => {
            const error = parseError(res)
            dispatch(getCurrentUserFailure(error))
            dispatch(showError(error))
        })
}

export const saveCurrentUser = (user: User) => (dispatch: Function) => {
    dispatch(saveCurrentUserRequest())
    const form = new FormData()
    Object.keys(user).forEach((key: string) => {
        form.append(key, user[key])
    })
    return axios.post(createLink('profile/update'), form, {
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded'
        }
    })
        .then(({data}) => {
            dispatch(saveCurrentUserSuccess(data))
            dispatch(showSuccess({
                title: 'Success!',
                message: 'Profile saved'
            }))
        })
        .catch(res => {
            const error = parseError(res)
            dispatch(saveCurrentUserFailure(error))
            dispatch(showError(error))
        })
}

export const updateCurrentUserName = (name: string) => (dispatch: Function, getState: Function) => {
    const state = getState()
    const user = state.user.currentUser
    dispatch(updateCurrentUser({
        ...user,
        name
    }))
}

export const updateCurrentUserTimezone = (timezone: string) => (dispatch: Function, getState: Function) => {
    const state = getState()
    const user = state.user.currentUser
    dispatch(updateCurrentUser({
        ...user,
        timezone
    }))
}

const updateCurrentUser = (user: User) => ({
    type: UPDATE_CURRENT_USER,
    user
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