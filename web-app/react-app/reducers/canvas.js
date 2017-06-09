// @flow

import {
    GET_RUNNING_CANVASES_REQUEST,
    GET_RUNNING_CANVASES_SUCCESS,
    GET_RUNNING_CANVASES_FAILURE
} from '../actions/canvas.js'

import type {
    CanvasReducerState as State,
    CanvasReducerAction as Action
} from '../flowtype/canvas-types'

const initialState = {
    list: [],
    error: null,
    fetching: false
}

export default function(state: State = initialState, action: Action) : State {
    switch (action.type) {
        case GET_RUNNING_CANVASES_REQUEST:
            return {
                ...state,
                fetching: true
            }
        case GET_RUNNING_CANVASES_SUCCESS:
            return {
                ...state,
                list: action.canvases,
                fetching: false,
                error: null
            }
        case GET_RUNNING_CANVASES_FAILURE:
            return {
                ...state,
                fetching: false,
                error: action.error
            }
        default:
            return state
    }
}
