// @flow

import {
    GET_AND_REPLACE_DASHBOARDS_REQUEST,
    GET_AND_REPLACE_DASHBOARDS_SUCCESS,
    GET_AND_REPLACE_DASHBOARDS_FAILURE,
    GET_DASHBOARD_REQUEST,
    GET_DASHBOARD_SUCCESS,
    GET_DASHBOARD_FAILURE,
    UPDATE_AND_SAVE_DASHBOARD_REQUEST,
    UPDATE_AND_SAVE_DASHBOARD_SUCCESS,
    UPDATE_AND_SAVE_DASHBOARD_FAILURE,
    CREATE_DASHBOARD_REQUEST,
    CREATE_DASHBOARD_SUCCESS,
    CREATE_DASHBOARD_FAILURE,
    DELETE_DASHBOARD_REQUEST,
    DELETE_DASHBOARD_SUCCESS,
    DELETE_DASHBOARD_FAILURE,
    GET_MY_DASHBOARD_PERMISSIONS_REQUEST,
    GET_MY_DASHBOARD_PERMISSIONS_SUCCESS,
    GET_MY_DASHBOARD_PERMISSIONS_FAILURE
} from '../actions/dashboard.js'

declare var _: any

import type {
    DashboardReducerState as State,
    DashboardReducerAction as Action
} from '../types/dashboard-types'

const initialState = {
    dashboardsById: {},
    error: null,
    fetching: false
}

const dashboard = function(state: State = initialState, action: Action) : State {
    switch (action.type) {
        case GET_AND_REPLACE_DASHBOARDS_REQUEST:
        case GET_DASHBOARD_REQUEST:
        case CREATE_DASHBOARD_REQUEST:
        case UPDATE_AND_SAVE_DASHBOARD_REQUEST:
        case DELETE_DASHBOARD_REQUEST:
        case GET_MY_DASHBOARD_PERMISSIONS_REQUEST:
            return {
                ...state,
                fetching: true
            }
        case GET_AND_REPLACE_DASHBOARDS_SUCCESS:
            return {
                ...state,
                dashboardsById: _.groupBy(action.dashboards, dashboard => dashboard.id),
                fetching: false,
                error: null
            }
        case GET_DASHBOARD_SUCCESS:
        case CREATE_DASHBOARD_SUCCESS:
        case UPDATE_AND_SAVE_DASHBOARD_SUCCESS: {
            if (!action.dashboard || !action.dashboard.id) {
                return state
            }
            return {
                ...state,
                dashboardsById: {
                    ...state.dashboardsById,
                    [action.dashboard.id]: {
                        ...state.dashboardsById[action.dashboard.id],
                        ...action.dashboard
                    }
                },
                error: null,
                fetching: false
            }
        }
        case DELETE_DASHBOARD_SUCCESS: {
            if (!action.id) {
                return state
            }
            return {
                ...state,
                dashboardsById: {
                    ...state.dashboardsById,
                    [action.id]: undefined
                },
                error: null,
                fetching: false
            }
        }
        case GET_MY_DASHBOARD_PERMISSIONS_SUCCESS: {
            if (!action.id) {
                return state
            }
            return {
                ...state,
                dashboardsById: {
                    ...state.dashboardsById,
                    [action.id]: {
                        ...state.dashboardsById[action.id],
                        permissions: action.permissions || []
                    }
                },
                error: null,
                fetching: false
            }
        }
        
        case GET_AND_REPLACE_DASHBOARDS_FAILURE:
        case GET_DASHBOARD_FAILURE:
        case CREATE_DASHBOARD_FAILURE:
        case UPDATE_AND_SAVE_DASHBOARD_FAILURE:
        case DELETE_DASHBOARD_FAILURE:
        case GET_MY_DASHBOARD_PERMISSIONS_FAILURE:
            return {
                ...state,
                fetching: false,
                error: action.error
            }
        default:
            return state
    }
}

export default dashboard