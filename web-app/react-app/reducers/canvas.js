// @flow

import {
    GET_RUNNING_CANVASES_REQUEST,
    GET_RUNNING_CANVASES_SUCCESS,
    GET_RUNNING_CANVASES_FAILURE
} from '../actions/canvas.js'

import type {CanvasState} from '../flowtype/states/canvas-state'
import type {CanvasAction} from '../flowtype/actions/canvas-actions'

const initialState = {
    list: [],
    error: null,
    fetching: false
}

export default function(state: CanvasState = initialState, action: CanvasAction): CanvasState {
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
