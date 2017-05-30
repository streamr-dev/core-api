// @flow

import {
    GET_RUNNING_CANVASES_REQUEST,
    GET_RUNNING_CANVASES_SUCCESS,
    GET_RUNNING_CANVASES_FAILURE,
    SET_MODULE_CHECKED
} from '../actions/canvas.js'

import type {
    CanvasReducerState as State,
    CanvasReducerAction as Action
} from '../types/canvas-types'

const initialState = {
    list: [],
    error: null,
    fetching: false
}

declare var _ : any

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
        case SET_MODULE_CHECKED: {
            let canvas = _.find(state.list, canvas => canvas.id === action.canvasId)
            let module = _.find(canvas.modules, module => module.id === action.moduleId)
            return {
                ...state,
                list: [
                    ...(_.without(state.list, canvas)),
                    {
                        ...canvas,
                        modules: [
                            ...(_.without(canvas.modules, module)),
                            {
                                ...module,
                                checked: action.state
                            }
                        ]
                    }
                ],
                fetching: false,
                error: action.error
            }
            
        }
        
        
        default:
            return state
    }
}
