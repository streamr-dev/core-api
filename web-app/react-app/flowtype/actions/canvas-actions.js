// @flow

import type {Canvas} from '../canvas-types'
import type {ApiError} from '../common-types'
import * as actions from '../../actions/canvas'

export type CanvasAction = {
    type: typeof actions.GET_RUNNING_CANVASES_REQUEST
} | {
    type: typeof actions.GET_RUNNING_CANVASES_SUCCESS,
    canvases: Array<Canvas>
} | {
    type: typeof actions.GET_RUNNING_CANVASES_FAILURE,
    error: ApiError
}
