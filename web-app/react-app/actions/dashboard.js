// @flow

import axios from 'axios'
import {parseError} from './utils/parseApiResponse'
import createLink from '../helpers/createLink'
import _ from 'lodash'

import {success, error} from 'react-notification-system-redux'

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

export const LOCK_DASHBOARD_EDITING = 'LOCK_DASHBOARD_EDITING'
export const UNLOCK_DASHBOARD_EDITING = 'UNLOCK_DASHBOARD_EDITING'

export const CHANGE_DASHBOARD_ID = 'CHANGE_DASHBOARD_ID'

const apiUrl = 'api/v1/dashboards'

import type { ErrorInUi } from '../flowtype/common-types'
import type { Dashboard, DashboardItem, Layout, LayoutItem} from '../flowtype/dashboard-types'

const dashboardConfig = require('../components/DashboardPage/dashboardConfig')

declare var Streamr: {
    user: string
}

export const getAndReplaceDashboards = () => (dispatch: Function) => {
    dispatch(getAndReplaceDashboardsRequest())
    return axios.get(createLink(apiUrl))
        .then(({data}) => {
            dispatch(getAndReplaceDashboardsSuccess(data))
        })
        .catch(res => {
            const e = parseError(res)
            dispatch(getAndReplaceDashboardsFailure(e))
            dispatch(error({
                title: e.message
            }))
            throw e
        })
}

export const getDashboard = (id: $ElementType<Dashboard, 'id'>) => (dispatch: Function) => {
    dispatch(getDashboardRequest(id))
    return axios.get(createLink(`${apiUrl}/${id}`))
        .then(({data}) => dispatch(getDashboardSuccess({
            ...data,
            layout: data.layout
        })))
        .catch(res => {
            const e = parseError(res)
            dispatch(getDashboardFailure(e))
            dispatch(error({
                title: e.message
            }))
            throw e
        })
}

export const updateAndSaveCurrentDashboard = () => (dispatch: Function, getState: Function) => {
    const state = getState().dashboard
    const dashboard = state.dashboardsById[state.openDashboard.id]
    dispatch(updateAndSaveDashboard(dashboard))
}

export const updateAndSaveDashboard = (dashboard: Dashboard) => (dispatch: Function) => {
    dispatch(updateAndSaveDashboardRequest())
    const createNew = dashboard.new

    return axios({
        method: createNew ? 'POST' : 'PUT',
        url: createLink(createNew ? apiUrl : `${apiUrl}/${dashboard.id}`),
        data: {
            ...dashboard,
            layout: JSON.stringify(dashboard.layout)
        }
    })
        .then(({data}) => {
            dispatch(success({
                title: 'Dashboard saved successfully!'
            }))
            dashboard.id !== data.id && dispatch(changeDashboardId(dashboard.id, data.id))
            dispatch(updateAndSaveDashboardSuccess({
                ...data,
                ownPermissions: [...(dashboard.ownPermissions || []), ...(createNew ? ['read', 'write', 'share'] : [])]
            }))
        })
        .catch(res => {
            const e = parseError(res)

            dispatch(error({
                title: e.message
            }))
            dispatch(updateAndSaveDashboardFailure(e))

            throw e
        })
}

export const deleteDashboard = (id: $ElementType<Dashboard, 'id'>) => (dispatch: Function) => {
    dispatch(deleteDashboardRequest(id))
    return axios.delete(createLink(`${apiUrl}/${id}`))
        .then(() => dispatch(deleteDashboardSuccess(id)))
        .catch(res => {
            const e = parseError(res)
            dispatch(deleteDashboardFailure(e))
            dispatch(error({
                title: e.message
            }))
            throw e
        })
}

export const getMyDashboardPermissions = (id: $ElementType<Dashboard, 'id'>) => (dispatch: Function) => {
    dispatch(getMyDashboardPermissionsRequest(id))
    return axios.get(createLink(`${apiUrl}/${id}/permissions/me`))
        .then(res => dispatch(getMyDashboardPermissionsSuccess(id, res.data.filter(item => item.user === Streamr.user).map(item => item.operation))))
        .catch(res => {
            const e = parseError(res)
            dispatch(getMyDashboardPermissionsFailure(id, e))
            dispatch(error({
                title: 'Error!',
                message: e.message
            }))
            throw e
        })
}

export const updateDashboardChanges = (id: $ElementType<Dashboard, 'id'>, changes: {}) => (dispatch: Function, getState: Function) => {
    const state = getState()
    const dashboard = state.dashboard.dashboardsById[id]
    dispatch(updateDashboard({
        ...dashboard,
        ...changes
    }))
}

export const updateDashboard = (dashboard: Dashboard) => ({
    type: UPDATE_DASHBOARD,
    dashboard
})

export const addDashboardItem = (dashboard: Dashboard, item: DashboardItem) => updateDashboard({
    ...dashboard,
    items: [
        ...dashboard.items,
        item
    ]
})

export const updateDashboardLayout = (dashboardId: $ElementType<Dashboard, 'id'>, layout: Layout) => (dispatch: Function, getState: Function) => {
    const state = getState().dashboard
    const dashboard = state.dashboardsById[state.openDashboard.id]
    const normalizeLayoutItem = (item: LayoutItem) => ({
        i: item.i || 0,
        h: item.h || 0,
        isDraggable: item.isDraggable,
        isResizable: item.isResizable,
        maxH: item.maxH,
        maxW: item.maxW,
        minH: item.minH || 0,
        minW: item.minW || 0,
        moved: item.moved || false,
        static: item.static || false,
        w: item.w || 0,
        x: item.x || 0,
        y: item.y || 0
    })
    const normalizeItemList = (itemList: ?Array<LayoutItem>) => itemList ? _.chain(itemList)
        .sortBy('i')
        .map(normalizeLayoutItem)
        .value() : []
    const normalizeLayout = (layout: Layout) => dashboardConfig.layout.sizes.reduce((obj, size) => {
        obj[size] = (layout && layout[size]) ? normalizeItemList(layout[size]) : []
        return obj
    }, {})
    if (dashboard && !_.isEqual(normalizeLayout(layout), normalizeLayout(dashboard.layout))) {
        dispatch(updateDashboard({
            ...dashboard,
            layout
        }))
    }
}

export const updateDashboardItem = (dashboard: Dashboard, item: DashboardItem) => updateDashboard({
    ...dashboard,
    items: [
        ...(dashboard.items.filter(it => it.canvas !== item.canvas || it.module !== item.module)),
        item
    ]
})

export const removeDashboardItem = (dashboard: Dashboard, item: DashboardItem) => updateDashboard({
    ...dashboard,
    items: dashboard.items.filter(it => it.canvas !== item.canvas || it.module !== item.module)
})

export const createDashboard = (dashboard: Dashboard) => ({
    type: CREATE_DASHBOARD,
    dashboard
})

export const newDashboard = (id: $ElementType<Dashboard, 'id'>) => createDashboard({
    id,
    name: 'Untitled Dashboard',
    items: [],
    layout: {},
    editingLocked: false
})

export const openDashboard = (id: $ElementType<Dashboard, 'id'>) => ({
    type: OPEN_DASHBOARD,
    id
})

export const lockDashboardEditing = (id: $ElementType<Dashboard, 'id'>) => ({
    type: LOCK_DASHBOARD_EDITING,
    id
})

export const unlockDashboardEditing = (id: $ElementType<Dashboard, 'id'>) => ({
    type: UNLOCK_DASHBOARD_EDITING,
    id
})

const changeDashboardId = (oldId: $ElementType<Dashboard, 'id'>, newId: $ElementType<Dashboard, 'id'>) => ({
    type: CHANGE_DASHBOARD_ID,
    oldId,
    newId
})

const getAndReplaceDashboardsRequest = () => ({
    type: GET_AND_REPLACE_DASHBOARDS_REQUEST,
})

const getDashboardRequest = (id: $ElementType<Dashboard, 'id'>) => ({
    type: GET_DASHBOARD_REQUEST,
    id
})

const updateAndSaveDashboardRequest = () => ({
    type: UPDATE_AND_SAVE_DASHBOARD_REQUEST,
})

const deleteDashboardRequest = (id: $ElementType<Dashboard, 'id'>) => ({
    type: DELETE_DASHBOARD_REQUEST,
    id
})

const getMyDashboardPermissionsRequest = (id: $ElementType<Dashboard, 'id'>) => ({
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

const deleteDashboardSuccess = (id: $ElementType<Dashboard, 'id'>) => ({
    type: DELETE_DASHBOARD_SUCCESS,
    id
})

const getMyDashboardPermissionsSuccess = (id: $ElementType<Dashboard, 'id'>, permissions: Array<string>) => ({
    type: GET_MY_DASHBOARD_PERMISSIONS_SUCCESS,
    id,
    permissions
})

const getAndReplaceDashboardsFailure = (error: ErrorInUi) => ({
    type: GET_AND_REPLACE_DASHBOARDS_FAILURE,
    error
})

const getDashboardFailure = (error: ErrorInUi) => ({
    type: GET_DASHBOARD_FAILURE,
    error
})

const updateAndSaveDashboardFailure = (error: ErrorInUi) => ({
    type: UPDATE_AND_SAVE_DASHBOARD_FAILURE,
    error
})

const deleteDashboardFailure = (error: ErrorInUi) => ({
    type: DELETE_DASHBOARD_FAILURE,
    error
})

const getMyDashboardPermissionsFailure = (id: $ElementType<Dashboard, 'id'>, error: ErrorInUi) => ({
    type: GET_MY_DASHBOARD_PERMISSIONS_FAILURE,
    id,
    error
})
