// @flow

import axios from 'axios'
import parseError from './utils/parseError'

export const CREATE_DASHBOARD_REQUEST = 'CREATE_DASHBOARD_KEY_REQUEST'
export const CREATE_DASHBOARD_SUCCESS = 'CREATE_DASHBOARD_KEY_SUCCESS'
export const CREATE_DASHBOARD_FAILURE = 'CREATE_DASHBOARD_KEY_FAILURE'

export const UPDATE_DASHBOARD_REQUEST = 'UPDATE_DASHBOARD_REQUEST'
export const UPDATE_DASHBOARD_SUCCESS = 'UPDATE_DASHBOARD_SUCCESS'
export const UPDATE_DASHBOARD_FAILURE = 'UPDATE_DASHBOARD_FAILURE'

export const GET_AND_REPLACE_DASHBOARDS_REQUEST = 'GET_AND_REPLACE_DASHBOARDS_REQUEST'
export const GET_AND_REPLACE_DASHBOARDS_SUCCESS = 'GET_AND_REPLACE_DASHBOARDS_SUCCESS'
export const GET_AND_REPLACE_DASHBOARDS_FAILURE = 'GET_AND_REPLACE_DASHBOARDS_FAILURE'

export const GET_DASHBOARD_REQUEST = 'GET_DASHBOARD_REQUEST'
export const GET_DASHBOARD_SUCCESS = 'GET_DASHBOARD_SUCCESS'
export const GET_DASHBOARD_FAILURE = 'GET_DASHBOARD_FAILURE'

export const DELETE_DASHBOARD_REQUEST = 'DELETE_DASHBOARD_REQUEST'
export const DELETE_DASHBOARD_SUCCESS = 'DELETE_DASHBOARD_SUCCESS'
export const DELETE_DASHBOARD_FAILURE = 'DELETE_DASHBOARD_FAILURE'

const apiUrl = 'api/v1/dashboards'

declare var Streamr: {
    createLink: Function
}

type Dashboard = {
    id: number,
    name: string,
    items: Array<{}>
}

type Err = {
    error: string,
    code?: string
}

export const getAndReplaceDashboards = () => (dispatch: Function) => {
    dispatch(getAndReplaceDashboardsRequest())
    return axios.get(Streamr.createLink({
        uri: apiUrl
    }))
        .then(({data}) => dispatch(getAndReplaceDashboardsSuccess(data)))
        .catch(res => {
            const e = parseError(res)
            dispatch(getAndReplaceDashboardsFailure(e))
            throw e
        })
}

export const getDashboard = (id: number) => (dispatch: Function) => {
    dispatch(getDashboardRequest(id))
    return axios.get(Streamr.createLink({
        uri: `${apiUrl}/${id}`
    }))
        .then(({data}) => dispatch(getDashboardSuccess(data)))
        .catch(res => {
            const e = parseError(res)
            dispatch(getDashboardFailure(e))
            throw e
        })
}

export const createDashboard = (dashboard: Dashboard) => (dispatch: Function) => {
    dispatch(createDashboardRequest())
    return axios.post(Streamr.createLink({
        uri: apiUrl
    }), dashboard)
        .then(({data}) => dispatch(createDashboardSuccess(data)))
        .catch(res => {
            const e = parseError(res)
            dispatch(createDashboardFailure(e))
            throw e
        })
}

export const updateDashboard = (dashboard: Dashboard) => (dispatch: Function) => {
    dispatch(updateDashboardRequest())
    return axios.put(Streamr.createLink({
        uri: `${apiUrl}/${dashboard.id}`
    }), dashboard)
        .then(({data}) => dispatch(updateDashboardSuccess(data)))
        .catch(res => {
            const e = parseError(res)
            dispatch(updateDashboardFailure(e))
            throw e
        })
}

export const deleteDashboard = (id: number) => (dispatch: Function) => {
    dispatch(deleteDashboardRequest(id))
    return axios.delete(Streamr.createLink({
        uri: `${apiUrl}/${id}`
    }))
        .then(() => dispatch(deleteDashboardSuccess(id)))
        .catch(res => {
            const e = parseError(res)
            dispatch(deleteDashboardFailure(e))
            throw e
        })
}

const getAndReplaceDashboardsRequest = () => ({
    type: GET_AND_REPLACE_DASHBOARDS_REQUEST,
})

const getDashboardRequest = (id: number) => ({
    type: GET_DASHBOARD_REQUEST,
    id
})

const createDashboardRequest = () => ({
    type: CREATE_DASHBOARD_REQUEST,
})

const updateDashboardRequest = () => ({
    type: UPDATE_DASHBOARD_REQUEST,
})

const deleteDashboardRequest = (id: number) => ({
    type: DELETE_DASHBOARD_REQUEST,
    id
})

const getAndReplaceDashboardsSuccess = (dashboards: Array<Dashboard>) => ({
    type: GET_AND_REPLACE_DASHBOARDS_SUCCESS,
    dashboards
})

const getDashboardSuccess = (dashboard: Dashboard) => ({
    type: GET_DASHBOARD_SUCCESS,
    dashboard
})

const createDashboardSuccess = (dashboard: Dashboard) => ({
    type: CREATE_DASHBOARD_SUCCESS,
    dashboard
})

const updateDashboardSuccess = (dashboard: Dashboard) => ({
    type: UPDATE_DASHBOARD_SUCCESS,
    dashboard
})

const deleteDashboardSuccess = (id: number) => ({
    type: DELETE_DASHBOARD_SUCCESS,
    id
})

const getAndReplaceDashboardsFailure = (error: Err) => ({
    type: GET_AND_REPLACE_DASHBOARDS_FAILURE,
    error
})

const getDashboardFailure = (error: Err) => ({
    type: GET_DASHBOARD_FAILURE,
    error
})

const createDashboardFailure = (error: Err) => ({
    type: CREATE_DASHBOARD_FAILURE,
    error
})

const updateDashboardFailure = (error: Err) => ({
    type: UPDATE_DASHBOARD_FAILURE,
    error
})

const deleteDashboardFailure = (error: Err) => ({
    type: DELETE_DASHBOARD_FAILURE,
    error
})
