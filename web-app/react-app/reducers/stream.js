// @flow

import {
    GET_STREAM_REQUEST,
    GET_STREAM_SUCCESS,
    GET_STREAM_FAILURE,
    GET_MY_STREAM_PERMISSIONS_REQUEST,
    GET_MY_STREAM_PERMISSIONS_SUCCESS,
    GET_MY_STREAM_PERMISSIONS_FAILURE,
    OPEN_STREAM,
} from '../actions/stream.js'

import type {StreamState} from '../flowtype/states/stream-state'
import type {StreamAction} from '../flowtype/actions/stream-actions'

const initialState = {
    byId: {},
    openStream: {
        id: null
    },
    fetching: false,
    error: null
}

export default function(state: StreamState = initialState, action: StreamAction): StreamState {
    switch (action.type) {
        case GET_STREAM_REQUEST:
        case GET_MY_STREAM_PERMISSIONS_REQUEST:
            return {
                ...state,
                fetching: true
            }

        case GET_STREAM_SUCCESS:
            return {
                ...state,
                byId: {
                    ...state.byId,
                    [action.stream.id]: action.stream
                },
                fetching: false,
                error: null
            }

        case GET_MY_STREAM_PERMISSIONS_SUCCESS:
            return {
                ...state,
                byId: {
                    ...state.byId,
                    [action.id]: {
                        ...state.byId[action.id],
                        ownPermissions: action.permissions || []
                    }
                },
                error: null,
                fetching: false
            }

        case GET_STREAM_FAILURE:
        case GET_MY_STREAM_PERMISSIONS_FAILURE:
            return {
                ...state,
                fetching: false,
                error: action.error
            }

        case OPEN_STREAM:
            return {
                ...state,
                openStream: {
                    ...state.openStream,
                    id: action.id
                }
            }

        default:
            return state
    }
}
