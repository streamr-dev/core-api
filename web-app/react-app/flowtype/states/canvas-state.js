// @flow

import type {Canvas} from '../canvas-types.js'
import type {ApiError} from '../common-types'

export type CanvasState = {
    list: Array<Canvas>,
    error: ?ApiError,
    fetching?: boolean
}
