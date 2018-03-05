// @flow

import axios from 'axios'
import {parseError} from './utils/parseApiResponse'
import createLink from '../helpers/createLink'

export const GET_RUNNING_CANVASES_REQUEST = 'GET_RUNNING_CANVASES_REQUEST'
export const GET_RUNNING_CANVASES_SUCCESS = 'GET_RUNNING_CANVASES_SUCCESS'
export const GET_RUNNING_CANVASES_FAILURE = 'GET_RUNNING_CANVASES_FAILURE'

const apiUrl = 'api/v1/canvases'

import type { ErrorInUi } from '../flowtype/common-types'
import type { Canvas } from '../flowtype/canvas-types'

export const getRunningCanvases = () => (dispatch: Function) => {
    dispatch(getCanvasesRequest())
    return axios.get(createLink(apiUrl), {
        params: {
            state: 'RUNNING',
            adhoc: false,
            sort: 'dateCreated',
            order: 'desc'
        }
    })
        .then(({data}) => {
            dispatch(getCanvasesSuccess(data))
        })
        .catch(res => {
            const e = parseError(res)
            dispatch(getCanvasesFailure(e))
            throw e
        })
}

const getCanvasesRequest = () => ({
    type: GET_RUNNING_CANVASES_REQUEST
})

const getCanvasesSuccess = (canvases: Array<Canvas>) => ({
    type: GET_RUNNING_CANVASES_SUCCESS,
    canvases
})

const getCanvasesFailure = (error: ErrorInUi) => ({
    type: GET_RUNNING_CANVASES_FAILURE,
    error
})
