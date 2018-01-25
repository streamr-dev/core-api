// @flow

import {
    GET_CURRENT_USER_REQUEST,
    GET_CURRENT_USER_SUCCESS,
    GET_CURRENT_USER_FAILURE,
    SAVE_CURRENT_USER_REQUEST,
    SAVE_CURRENT_USER_SUCCESS,
    SAVE_CURRENT_USER_FAILURE,
    UPDATE_CURRENT_USER
} from '../actions/user.js'

import type {State, Action} from '../flowtype/user-types.js'

const initialState = {
    currentUser: {},
    error: null,
    fetching: false,
    saved: true
}

export default function(state: State = initialState, action: Action) : State {
    switch (action.type) {
        case GET_CURRENT_USER_REQUEST:
        case SAVE_CURRENT_USER_REQUEST:
            return {
                ...state,
                fetching: true
            }
        case SAVE_CURRENT_USER_SUCCESS:
        case GET_CURRENT_USER_SUCCESS:
            return {
                ...state,
                currentUser: action.user,
                fetching: false,
                saved: true,
                error: null
            }
        case SAVE_CURRENT_USER_FAILURE:
        case GET_CURRENT_USER_FAILURE:
            return {
                ...state,
                fetching: false,
                error: action.error
            }
        case UPDATE_CURRENT_USER:
            return {
                ...state,
                saved: false,
                currentUser: {
                    ...state.currentUser,
                    ...action.user
                }
            }
        default:
            return state
    }
}