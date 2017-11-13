// @flow

import {
    GET_CURRENT_USER_REQUEST,
    GET_CURRENT_USER_SUCCESS,
    GET_CURRENT_USER_FAILURE,
    UPDATE_CURRENT_USER_NAME,
    UPDATE_CURRENT_USER_TIMEZONE
} from '../actions/user.js'

import type {State, Action} from '../flowtype/user-types.js'

const initialState = {
    currentUser: {},
    error: null,
    fetching: false
}

export default function(state: State = initialState, action: Action) : State {
    switch (action.type) {
        case GET_CURRENT_USER_REQUEST:
            return {
                ...state,
                fetching: true
            }
        case GET_CURRENT_USER_SUCCESS:
            return {
                ...state,
                currentUser: action.user,
                fetching: false,
                error: null
            }
        case GET_CURRENT_USER_FAILURE:
            return {
                ...state,
                fetching: false,
                error: action.error
            }
        case UPDATE_CURRENT_USER_NAME:
            return {
                ...state,
                currentUser: {
                    ...state.currentUser,
                    name: action.name
                }
            }
        case UPDATE_CURRENT_USER_TIMEZONE:
            return {
                ...state,
                currentUser: {
                    ...state.currentUser,
                    timezone: action.timezone
                }
            }
        default:
            return state
    }
}