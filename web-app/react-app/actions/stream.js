// @flow

declare var Streamr: any

import axios from 'axios'
import {parseError} from './utils/parseApiResponse'
import createLink from '../helpers/createLink'

import {success, error} from 'react-notification-system-redux'

import type {ErrorInUi} from '../flowtype/common-types'
import type {Stream, CSVImporterSchema} from '../flowtype/stream-types'
import type {Permission} from '../flowtype/permission-types'

type StreamId = $ElementType<Stream, 'id'>
type StreamFields = $ElementType<$ElementType<Stream, 'config'>, 'fields'>
type PermissionOperation = Array<$ElementType<Permission, 'operation'>>

export const CREATE_STREAM_REQUEST = 'CREATE_STREAM_REQUEST'
export const CREATE_STREAM_SUCCESS = 'CREATE_STREAM_SUCCESS'
export const CREATE_STREAM_FAILURE = 'CREATE_STREAM_FAILURE'

export const GET_STREAM_REQUEST = 'GET_STREAM_REQUEST'
export const GET_STREAM_SUCCESS = 'GET_STREAM_SUCCESS'
export const GET_STREAM_FAILURE = 'GET_STREAM_FAILURE'

export const UPDATE_STREAM_REQUEST = 'UPDATE_STREAM_REQUEST'
export const UPDATE_STREAM_SUCCESS = 'UPDATE_STREAM_SUCCESS'
export const UPDATE_STREAM_FAILURE = 'UPDATE_STREAM_FAILURE'

export const DELETE_STREAM_REQUEST = 'DELETE_STREAM_REQUEST'
export const DELETE_STREAM_SUCCESS = 'DELETE_STREAM_SUCCESS'
export const DELETE_STREAM_FAILURE = 'DELETE_STREAM_FAILURE'

export const GET_MY_STREAM_PERMISSIONS_REQUEST = 'GET_MY_STREAM_PERMISSIONS_REQUEST'
export const GET_MY_STREAM_PERMISSIONS_SUCCESS = 'GET_MY_STREAM_PERMISSIONS_SUCCESS'
export const GET_MY_STREAM_PERMISSIONS_FAILURE = 'GET_MY_STREAM_PERMISSIONS_FAILURE'

export const SAVE_STREAM_FIELDS_REQUEST = 'SAVE_STREAM_FIELDS_REQUEST'
export const SAVE_STREAM_FIELDS_SUCCESS = 'SAVE_STREAM_FIELDS_SUCCESS'
export const SAVE_STREAM_FIELDS_FAILURE = 'SAVE_STREAM_FIELDS_FAILURE'

export const UPLOAD_CSV_FILE_REQUEST = 'UPLOAD_CSV_FILE_REQUEST'
export const UPLOAD_CSV_FILE_SUCCESS = 'UPLOAD_CSV_FILE_SUCCESS'
export const UPLOAD_CSV_FILE_UNKNOWN_SCHEMA = 'UPLOAD_CSV_FILE_UNKNOWN_SCHEMA'
export const UPLOAD_CSV_FILE_FAILURE = 'UPLOAD_CSV_FILE_FAILURE'

export const CONFIRM_CSV_FILE_UPLOAD_REQUEST = 'CONFIRM_CSV_FILE_UPLOAD_REQUEST'
export const CONFIRM_CSV_FILE_UPLOAD_SUCCESS = 'CONFIRM_CSV_FILE_UPLOAD_SUCCESS'
export const CONFIRM_CSV_FILE_UPLOAD_FAILURE = 'CONFIRM_CSV_FILE_UPLOAD_FAILURE'

export const CANCEL_CSV_FILE_UPLOAD = 'CANCEL_CSV_FILE_UPLOAD'
export const OPEN_STREAM = 'OPEN_STREAM'

const apiUrl = 'api/v1/streams'

export const createStream = (data: { name: string, description: string }) => (dispatch: Function): Promise<Stream> => {
    dispatch(createStreamRequest())
    return new Promise((resolve, reject) => {
        axios.post(createLink(apiUrl), data)
            .then(({data}: { data: Stream }) => {
                dispatch(createStreamSuccess(data))
                dispatch(success({
                    title: 'Success!',
                    message: `Stream ${data.name} created successfully!`
                }))
                resolve(data)
            })
            .catch(res => {
                const e = parseError(res)
                dispatch(createStreamFailure(e))
                dispatch(error({
                    title: 'Error!',
                    message: e.message
                }))
                reject(e)
            })
    })
}

export const getStream = (id: StreamId) => (dispatch: Function) => {
    dispatch(getStreamRequest())
    return axios.get(createLink(`${apiUrl}/${id}`))
        .then(({data}: { data: Stream }) => dispatch(getStreamSuccess(data)))
        .catch(res => {
            const e = parseError(res)
            dispatch(getStreamFailure(e))
            dispatch(error({
                title: 'Error!',
                message: e.message
            }))
            throw e
        })
}

export const updateStream = (stream: Stream) => (dispatch: Function) => {
    dispatch(updateStreamRequest())
    return axios.put(createLink(`${apiUrl}/${stream.id}`), stream)
        .then(({data}) => {
            dispatch(updateStreamSuccess(data))
            dispatch(success({
                title: 'Success!',
                message: 'Stream saved successfully'
            }))
        })
        .catch(res => {
            const e = parseError(res)
            dispatch(updateStreamFailure(e))
            dispatch(error({
                title: 'Error!',
                message: e.message
            }))
            throw e
        })
}

export const deleteStream = (stream: Stream) => (dispatch: Function): Promise<void> => {
    dispatch(deleteStreamRequest())
    return axios.delete(createLink(`${apiUrl}/${stream.id}`))
        .then(() => {
            dispatch(deleteStreamSuccess(stream.id))
            dispatch(success({
                title: 'Success!',
                message: 'Stream deleted successfully'
            }))
        })
        .catch(res => {
            const e = parseError(res)
            dispatch(deleteStreamFailure(e))
            dispatch(error({
                title: 'Error!',
                message: e.message
            }))
            throw e
        })
}

export const getMyStreamPermissions = (id: StreamId) => (dispatch: Function) => {
    dispatch(getMyStreamPermissionsRequest())
    return axios.get(createLink(`${apiUrl}/${id}/permissions/me`))
        .then(res => dispatch(getMyStreamPermissionsSuccess(id, res.data.filter(item => item.user === Streamr.user).map(item => item.operation))))
        .catch(res => {
            const e = parseError(res)
            dispatch(getMyStreamPermissionsFailure(e))
            dispatch(error({
                title: 'Error!',
                message: e.message
            }))
            throw e
        })
}

export const saveFields = (id: StreamId, fields: StreamFields) => (dispatch: Function) => {
    dispatch(saveFieldsRequest())
    return axios.post(createLink(`${apiUrl}/${id}/fields`), fields)
        .then(({data}) => {
            dispatch(saveFieldsSuccess(id, data))
            dispatch(success({
                title: 'Success!',
                message: 'Fields saved successfully'
            }))
        })
        .catch(res => {
            const e = parseError(res)
            dispatch(saveFieldsFailure(e))
            dispatch(error({
                title: 'Error!',
                message: e.message
            }))
            throw e
        })
}

export const uploadCsvFile = (id: StreamId, file: File) => (dispatch: Function) => {
    const formData = new FormData()
    formData.append('file', file)
    dispatch(uploadCsvFileRequest())
    return axios.post(createLink(`${apiUrl}/${id}/uploadCsvFile`), formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
        .then(() => {
            dispatch(uploadCsvFileSuccess())
            dispatch(success({
                title: 'Success!',
                message: 'CSV file imported successfully'
            }))
        })
        .catch((res) => {
            const e = parseError(res)
            if (e.code === 'CSV_PARSE_UNKNOWN_SCHEMA') {
                dispatch(uploadCsvFileUnknownSchema(id, res.response.data.fileUrl, res.response.data.schema))
            } else {
                dispatch(uploadCsvFileFailure(e))
                dispatch(error({
                    title: 'Error!',
                    message: e.message
                }))
            }
            throw e
        })
}

export const confirmCsvFileUpload = (id: StreamId, fileUrl: string, dateFormat: string, timestampColumnIndex: number) => (dispatch: Function) => {
    dispatch(confirmCsvFileUploadRequest())
    return axios.post(createLink(`${apiUrl}/${id}/confirmCsvFileUpload`), {
        fileUrl,
        dateFormat,
        timestampColumnIndex
    })
        .then(() => {
            dispatch(confirmCsvFileUploadSuccess())
            dispatch(success({
                title: 'Success!',
                message: 'CSV file imported successfully'
            }))
        })
        .catch((res) => {
            const e = parseError(res)
            dispatch(confirmCsvFileUploadFailure(e))
            dispatch(error({
                title: 'Error!',
                message: e.message
            }))
            throw e
        })
}

export const openStream = (id: StreamId) => ({
    type: OPEN_STREAM,
    id
})

export const cancelCsvFileUpload = () => ({
    type: CANCEL_CSV_FILE_UPLOAD
})

const saveFieldsRequest = () => ({
    type: SAVE_STREAM_FIELDS_REQUEST
})

const saveFieldsSuccess = (id: StreamId, fields: StreamFields) => ({
    type: SAVE_STREAM_FIELDS_SUCCESS,
    id,
    fields
})

const saveFieldsFailure = (error: ErrorInUi) => ({
    type: SAVE_STREAM_FIELDS_FAILURE,
    error
})

const updateStreamRequest = () => ({
    type: UPDATE_STREAM_REQUEST
})

const updateStreamSuccess = (stream: Stream) => ({
    type: UPDATE_STREAM_SUCCESS,
    stream
})

const updateStreamFailure = (error: ErrorInUi) => ({
    type: UPDATE_STREAM_FAILURE,
    error
})

const getStreamRequest = () => ({
    type: GET_STREAM_REQUEST
})

const getStreamSuccess = (stream: Stream) => ({
    type: GET_STREAM_SUCCESS,
    stream
})

const getStreamFailure = (error: ErrorInUi) => ({
    type: GET_STREAM_FAILURE,
    error
})

const deleteStreamRequest = () => ({
    type: DELETE_STREAM_REQUEST
})

const deleteStreamSuccess = (id: StreamId) => ({
    type: DELETE_STREAM_SUCCESS,
    id
})

const deleteStreamFailure = (error: ErrorInUi) => ({
    type: DELETE_STREAM_FAILURE,
    error
})

const createStreamRequest = () => ({
    type: CREATE_STREAM_REQUEST
})

const createStreamSuccess = (stream: Stream) => ({
    type: CREATE_STREAM_SUCCESS,
    stream
})

const createStreamFailure = (error: ErrorInUi) => ({
    type: CREATE_STREAM_FAILURE,
    error
})

const getMyStreamPermissionsRequest = () => ({
    type: GET_MY_STREAM_PERMISSIONS_REQUEST
})

const getMyStreamPermissionsSuccess = (id: StreamId, permissions: PermissionOperation) => ({
    type: GET_MY_STREAM_PERMISSIONS_SUCCESS,
    id,
    permissions
})

const getMyStreamPermissionsFailure = (error: ErrorInUi) => ({
    type: GET_MY_STREAM_PERMISSIONS_FAILURE,
    error
})

const uploadCsvFileRequest = () => ({
    type: UPLOAD_CSV_FILE_REQUEST
})

const uploadCsvFileSuccess = () => ({
    type: UPLOAD_CSV_FILE_SUCCESS
})

const uploadCsvFileFailure = (error: ErrorInUi) => ({
    type: UPLOAD_CSV_FILE_FAILURE,
    error,
})

const uploadCsvFileUnknownSchema = (id: $ElementType<Stream, 'id'>, fileUrl: string, schema: CSVImporterSchema) => ({
    type: UPLOAD_CSV_FILE_UNKNOWN_SCHEMA,
    streamId: id,
    fileUrl,
    schema
})

const confirmCsvFileUploadRequest = () => ({
    type: CONFIRM_CSV_FILE_UPLOAD_REQUEST
})

const confirmCsvFileUploadSuccess = () => ({
    type: CONFIRM_CSV_FILE_UPLOAD_SUCCESS
})

const confirmCsvFileUploadFailure = (error: ErrorInUi) => ({
    type: CONFIRM_CSV_FILE_UPLOAD_FAILURE,
    error,
})
