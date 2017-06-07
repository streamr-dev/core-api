// @flow

import axios from 'axios'
import parseError from './utils/parseError'

import {showSuccess, showError} from './notification'

export const CREATE_DASHBOARD = 'CREATE_DASHBOARD'
export const OPEN_DASHBOARD = 'OPEN_DASHBOARD'
export const UPDATE_DASHBOARD = 'UPDATE_DASHBOARD'

export const UPDATE_AND_SAVE_DASHBOARD_REQUEST = 'UPDATE_AND_SAVE_DASHBOARD_REQUEST'
export const UPDATE_AND_SAVE_DASHBOARD_SUCCESS = 'UPDATE_AND_SAVE_DASHBOARD_SUCCESS'
export const UPDATE_AND_SAVE_DASHBOARD_FAILURE = 'UPDATE_AND_SAVE_DASHBOARD_FAILURE'

export const GET_AND_REPLACE_DASHBOARDS_REQUEST = 'GET_AND_REPLACE_DASHBOARDS_REQUEST'
export const GET_AND_REPLACE_DASHBOARDS_SUCCESS = 'GET_AND_REPLACE_DASHBOARDS_SUCCESS'
export const GET_AND_REPLACE_DASHBOARDS_FAILURE = 'GET_AND_REPLACE_DASHBOARDS_FAILURE'

export const GET_DASHBOARD_REQUEST = 'GET_DASHBOARD_REQUEST'
export const GET_DASHBOARD_SUCCESS = 'GET_DASHBOARD_SUCCESS'
export const GET_DASHBOARD_FAILURE = 'GET_DASHBOARD_FAILURE'

export const DELETE_DASHBOARD_REQUEST = 'DELETE_DASHBOARD_REQUEST'
export const DELETE_DASHBOARD_SUCCESS = 'DELETE_DASHBOARD_SUCCESS'
export const DELETE_DASHBOARD_FAILURE = 'DELETE_DASHBOARD_FAILURE'

export const GET_MY_DASHBOARD_PERMISSIONS_REQUEST = 'GET_MY_DASHBOARD_PERMISSIONS_REQUEST'
export const GET_MY_DASHBOARD_PERMISSIONS_SUCCESS = 'GET_MY_DASHBOARD_PERMISSIONS_SUCCESS'
export const GET_MY_DASHBOARD_PERMISSIONS_FAILURE = 'GET_MY_DASHBOARD_PERMISSIONS_FAILURE'

const apiUrl = 'api/v1/dashboards'

declare var Streamr: any

import type { ApiError } from '../flowtype/common-types'
import type { Dashboard, DashboardItem } from '../flowtype/dashboard-types'

export const getAndReplaceDashboards = () => (dispatch: Function) => {
    dispatch(getAndReplaceDashboardsRequest())
    return axios.get(Streamr.createLink({
        uri: apiUrl
    }))
        .then(({data}) => {
            dispatch(getAndReplaceDashboardsSuccess(data))
        })
        .catch(res => {
            const e = parseError(res)
            dispatch(getAndReplaceDashboardsFailure(e))
            throw e
        })
}

export const getDashboard = (id: Dashboard.id) => (dispatch: Function) => {
    dispatch(getDashboardRequest(id))
    return axios.get(Streamr.createLink({
        uri: `${apiUrl}/${id}`
    }))
        .then(({data}) => dispatch(getDashboardSuccess({
            ...data,
            layout: (typeof data.layout === 'string') ? JSON.parse(data.layout) : data.layout
        })))
        .catch(res => {
            const e = parseError(res)
            dispatch(getDashboardFailure(e))
            throw e
        })
}

export const updateAndSaveDashboard = (dashboard: Dashboard, createNew?: boolean) => (dispatch: Function) => {
    dispatch(updateAndSaveDashboardRequest())
    return axios({
        method: createNew ? 'POST' : 'PUT',
        url: Streamr.createLink({
            uri: createNew ? apiUrl : `${apiUrl}/${dashboard.id}`
        }),
        data: {
            ...dashboard,
            layout: JSON.stringify(dashboard.layout)
        }
    })
        .then(({data}) => {
            dispatch(showSuccess({
                title: 'Dashboard saved succesfully!'
            }))
            
            return dispatch(updateAndSaveDashboardSuccess({
                ...data,
                layout: (typeof data.layout === 'string') ? JSON.parse(data.layout) : data.layout
            }))
        })
        .catch(res => {
            const e = parseError(res)
            dispatch(updateAndSaveDashboardFailure(e))
            
            dispatch(showError({
                title: 'Something went wrong!',
                message: e.error
            }))
            
            throw e
        })
}

export const deleteDashboard = (id: Dashboard.id) => (dispatch: Function) => {
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

export const getMyDashboardPermissions = (id: Dashboard.id) => (dispatch: Function) => {
    dispatch(getMyDashboardPermissionsRequest(id))
    return axios.delete(Streamr.createLink({
        uri: `${apiUrl}/${id}/permissions/me`
    }))
        .then(res => {
            dispatch(getMyDashboardPermissionsSuccess(id, res.data.map(item => item.operation)))
        })
        .catch(res => {
            const e = parseError(res)
            dispatch(getMyDashboardPermissionsFailure(e))
            throw e
        })
}

export const removeDashboardItem = (dashboard: Dashboard, item: DashboardItem) => updateDashboard({
    ...dashboard,
    items: dashboard.items.filter(it => it.canvas !== item.canvas || it.module !== item.module)
})

export const addDashboardItem = (dashboard: Dashboard, item: DashboardItem) => updateDashboard({
    ...dashboard,
    items: [
        ...dashboard.items,
        item
    ]
})

export const updateDashboardItem = (dashboard: Dashboard, item: DashboardItem) => updateDashboard({
    ...dashboard,
    items: [
        dashboard.items.filter(it => it.canvas !== item.canvas || it.module !== item.module),
        item
    ]
})

export const newDashboard = (id: Dashboard.id) => createDashboard({
    id,
    name: 'Untitled',
    items: []
})

export const createDashboard = (dashboard: Dashboard) => ({
    type: CREATE_DASHBOARD,
    dashboard
})

export const openDashboard = (id: Dashboard.id) => ({
    type: OPEN_DASHBOARD,
    id
})

export const updateDashboard = (dashboard: Dashboard) => ({
    type: UPDATE_DASHBOARD,
    dashboard
})

const getAndReplaceDashboardsRequest = () => ({
    type: GET_AND_REPLACE_DASHBOARDS_REQUEST,
})

const getDashboardRequest = (id: Dashboard.id) => ({
    type: GET_DASHBOARD_REQUEST,
    id
})

const updateAndSaveDashboardRequest = () => ({
    type: UPDATE_AND_SAVE_DASHBOARD_REQUEST,
})

const deleteDashboardRequest = (id: Dashboard.id) => ({
    type: DELETE_DASHBOARD_REQUEST,
    id
})

const getMyDashboardPermissionsRequest = (id: Dashboard.id) => ({
    type: GET_MY_DASHBOARD_PERMISSIONS_REQUEST,
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

const updateAndSaveDashboardSuccess = (dashboard: Dashboard) => ({
    type: UPDATE_AND_SAVE_DASHBOARD_SUCCESS,
    dashboard
})

const deleteDashboardSuccess = (id: Dashboard.id) => ({
    type: DELETE_DASHBOARD_SUCCESS,
    id
})

const getMyDashboardPermissionsSuccess = (id: Dashboard.id, permissions: Array<string>) => ({
    type: GET_MY_DASHBOARD_PERMISSIONS_SUCCESS,
    id,
    permissions
})

const getAndReplaceDashboardsFailure = (error: ApiError) => ({
    type: GET_AND_REPLACE_DASHBOARDS_FAILURE,
    error
})

const getDashboardFailure = (error: ApiError) => ({
    type: GET_DASHBOARD_FAILURE,
    error
})

const updateAndSaveDashboardFailure = (error: ApiError) => ({
    type: UPDATE_AND_SAVE_DASHBOARD_FAILURE,
    error
})

const deleteDashboardFailure = (error: ApiError) => ({
    type: DELETE_DASHBOARD_FAILURE,
    error
})

const getMyDashboardPermissionsFailure = (error: ApiError) => ({
    type: GET_MY_DASHBOARD_PERMISSIONS_FAILURE,
    error
})
