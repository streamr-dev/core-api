// @flow

import _ from 'lodash'

import {
    GET_STREAM_REQUEST,
    GET_STREAM_SUCCESS,
    GET_STREAM_FAILURE,
    CREATE_STREAM_REQUEST,
    CREATE_STREAM_SUCCESS,
    CREATE_STREAM_FAILURE,
    UPDATE_STREAM_REQUEST,
    UPDATE_STREAM_SUCCESS,
    UPDATE_STREAM_FAILURE,
    DELETE_STREAM_REQUEST,
    DELETE_STREAM_SUCCESS,
    DELETE_STREAM_FAILURE,
    SAVE_STREAM_FIELDS_REQUEST,
    SAVE_STREAM_FIELDS_SUCCESS,
    SAVE_STREAM_FIELDS_FAILURE,
    GET_MY_STREAM_PERMISSIONS_REQUEST,
    GET_MY_STREAM_PERMISSIONS_SUCCESS,
    GET_MY_STREAM_PERMISSIONS_FAILURE,
    UPLOAD_CSV_FILE_REQUEST,
    UPLOAD_CSV_FILE_SUCCESS,
    UPLOAD_CSV_FILE_FAILURE,
    UPLOAD_CSV_FILE_UNKNOWN_SCHEMA,
    CONFIRM_CSV_FILE_UPLOAD_REQUEST,
    CONFIRM_CSV_FILE_UPLOAD_SUCCESS,
    CONFIRM_CSV_FILE_UPLOAD_FAILURE,
    OPEN_STREAM,
    CANCEL_CSV_FILE_UPLOAD
} from '../actions/stream.js'

import type {StreamState} from '../flowtype/states/stream-state'
import type {StreamAction} from '../flowtype/actions/stream-actions'

const initialState = {
    byId: {},
    openStream: {
        id: null
    },
    savingStreamFields: false,
    fetching: false,
    error: null,
    csvUpload: null
}

export default function(state: StreamState = initialState, action: StreamAction): StreamState {
    switch (action.type) {
        case GET_STREAM_REQUEST:
        case CREATE_STREAM_REQUEST:
        case UPDATE_STREAM_REQUEST:
        case GET_MY_STREAM_PERMISSIONS_REQUEST:
        case DELETE_STREAM_REQUEST:
            return {
                ...state,
                fetching: true
            }

        case SAVE_STREAM_FIELDS_REQUEST:
            return {
                ...state,
                savingStreamFields: true
            }

        case UPLOAD_CSV_FILE_REQUEST:
            return {
                ...state,
                csvUpload: {
                    id: action.id,
                    fetching: true
                }
            }

        case CONFIRM_CSV_FILE_UPLOAD_REQUEST:
            return {
                ...state,
                csvUpload: {
                    ...(state.csvUpload || {}),
                    fetching: true
                }
            }

        case UPLOAD_CSV_FILE_SUCCESS:
        case CONFIRM_CSV_FILE_UPLOAD_SUCCESS:
            return {
                ...state,
                csvUpload: null
            }

        case GET_STREAM_SUCCESS:
        case CREATE_STREAM_SUCCESS:
            return {
                ...state,
                byId: {
                    ...state.byId,
                    [action.stream.id]: action.stream
                },
                fetching: false,
                error: null
            }

        case UPDATE_STREAM_SUCCESS:
            return {
                ...state,
                byId: {
                    ...state.byId,
                    [action.stream.id]: {
                        ...(action.stream.id && state.byId[action.stream.id] || {}),
                        ...action.stream
                    }
                },
                fetching: false,
                error: null
            }

        case DELETE_STREAM_SUCCESS:
            return {
                ...state,
                byId: _.omit(state.byId, action.id),
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

        case UPLOAD_CSV_FILE_UNKNOWN_SCHEMA:
            return {
                ...state,
                csvUpload: {
                    fetching: false,
                    id: action.streamId,
                    fileUrl: action.fileUrl,
                    schema: action.schema
                }
            }

        case UPLOAD_CSV_FILE_FAILURE:
            return {
                ...state,
                error: action.error,
                csvUpload: null
            }

        case CONFIRM_CSV_FILE_UPLOAD_FAILURE:
            return {
                ...state,
                error: action.error,
                csvUpload: {
                    ...(state.csvUpload || {}),
                    fetching: false
                }
            }

        case GET_STREAM_FAILURE:
        case CREATE_STREAM_FAILURE:
        case UPDATE_STREAM_FAILURE:
        case GET_MY_STREAM_PERMISSIONS_FAILURE:
        case DELETE_STREAM_FAILURE:
            return {
                ...state,
                fetching: false,
                error: action.error
            }

        case SAVE_STREAM_FIELDS_FAILURE:
            return {
                ...state,
                savingStreamFields: false,
                error: action.error
            }

        case SAVE_STREAM_FIELDS_SUCCESS: {
            const stream = state.byId[action.id] || {}
            const config = stream.config || {}
            return {
                ...state,
                byId: {
                    ...state.byId,
                    [action.id]: {
                        ...stream,
                        config: {
                            ...config,
                            fields: action.fields
                        }
                    }
                },
                fetching: false,
                error: null
            }
        }

        case OPEN_STREAM:
            return {
                ...state,
                openStream: {
                    ...state.openStream,
                    id: action.id
                }
            }

        case CANCEL_CSV_FILE_UPLOAD:
            return {
                ...state,
                csvUpload: null
            }

        default:
            return state
    }
}
