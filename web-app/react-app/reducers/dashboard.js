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
    DELETE_DASHBOARD_REQUEST,
    DELETE_DASHBOARD_SUCCESS,
    DELETE_DASHBOARD_FAILURE,
    GET_MY_DASHBOARD_PERMISSIONS_REQUEST,
    GET_MY_DASHBOARD_PERMISSIONS_SUCCESS,
    GET_MY_DASHBOARD_PERMISSIONS_FAILURE,
    OPEN_DASHBOARD,
    CREATE_DASHBOARD,
    UPDATE_DASHBOARD,
    LOCK_DASHBOARD_EDITING,
    UNLOCK_DASHBOARD_EDITING,
    CHANGE_DASHBOARD_ID,
} from '../actions/dashboard.js'

import _ from 'lodash'

import type {DashboardState} from '../flowtype/states/dashboard-state'
import type {Action as DashboardAction} from '../flowtype/actions/dashboard-actions'

const initialState = {
    dashboardsById: {},
    openDashboard: {
        id: null,
        isFullScreen: false,
    },
    error: null,
    fetching: false,
}

const dashboard = function(state: DashboardState = initialState, action: DashboardAction): DashboardState {
    switch (action.type) {
        case OPEN_DASHBOARD:
            return {
                ...state,
                openDashboard: {
                    ...state.openDashboard,
                    id: action.id,
                },
            }

        case GET_AND_REPLACE_DASHBOARDS_REQUEST:
        case GET_DASHBOARD_REQUEST:
        case UPDATE_AND_SAVE_DASHBOARD_REQUEST:
        case DELETE_DASHBOARD_REQUEST:
        case GET_MY_DASHBOARD_PERMISSIONS_REQUEST:
            return {
                ...state,
                fetching: true,
            }

        case GET_AND_REPLACE_DASHBOARDS_SUCCESS:
            return {
                ...state,
                dashboardsById: _.keyBy(action.dashboards, 'id'),
                fetching: false,
                error: null,
            }

        case CREATE_DASHBOARD: {
            return {
                ...state,
                dashboardsById: {
                    ...state.dashboardsById,
                    [action.dashboard && action.dashboard.id]: {
                        ...action.dashboard,
                        new: true,
                        saved: true,
                    },
                },
                error: null,
                fetching: false,
            }
        }

        case UPDATE_DASHBOARD: {
            return {
                ...state,
                dashboardsById: {
                    ...state.dashboardsById,
                    [action.dashboard && action.dashboard.id]: {
                        ...state.dashboardsById[action.dashboard.id],
                        ...action.dashboard,
                        saved: false,
                    },
                },
                error: null,
                fetching: false,
            }
        }

        case GET_DASHBOARD_SUCCESS:
        case UPDATE_AND_SAVE_DASHBOARD_SUCCESS: {
            return {
                ...state,
                dashboardsById: {
                    ...state.dashboardsById,
                    [action.dashboard && action.dashboard.id]: {
                        ...state.dashboardsById[action.dashboard && action.dashboard.id],
                        ...action.dashboard,
                        new: false,
                        saved: true,
                    },
                },
                error: null,
                fetching: false,
            }
        }

        case DELETE_DASHBOARD_SUCCESS: {
            const dbById = {
                ...state.dashboardsById,
            }
            action.id && delete dbById[action.id]
            return {
                ...state,
                dashboardsById: dbById,
                error: null,
                fetching: false,
            }
        }

        case GET_MY_DASHBOARD_PERMISSIONS_SUCCESS: {
            return {
                ...state,
                dashboardsById: {
                    ...state.dashboardsById,
                    [action.id]: {
                        ...state.dashboardsById[action.id],
                        ownPermissions: action.permissions || [],
                    },
                },
                error: null,
                fetching: false,
            }
        }

        case GET_AND_REPLACE_DASHBOARDS_FAILURE:
        case GET_DASHBOARD_FAILURE:
        case UPDATE_AND_SAVE_DASHBOARD_FAILURE:
        case DELETE_DASHBOARD_FAILURE:
        case GET_MY_DASHBOARD_PERMISSIONS_FAILURE:
            return {
                ...state,
                fetching: false,
                error: action.error,
            }

        case LOCK_DASHBOARD_EDITING:
            return {
                ...state,
                dashboardsById: {
                    ...state.dashboardsById,
                    [action.id]: {
                        ...(state.dashboardsById[action.id] || {}),
                        editingLocked: true,
                    },
                },
            }

        case UNLOCK_DASHBOARD_EDITING:
            return {
                ...state,
                dashboardsById: {
                    ...state.dashboardsById,
                    [action.id]: {
                        ...(state.dashboardsById[action.id] || {}),
                        editingLocked: false,
                    },
                },
            }

        case CHANGE_DASHBOARD_ID: {
            const newDashboardsById = {
                ...state.dashboardsById,
                [action.newId]: {
                    ...(state.dashboardsById[action.oldId] || {}),
                    id: action.newId,
                },
            }
            delete newDashboardsById[action.oldId]
            return {
                ...state,
                dashboardsById: newDashboardsById,
                openDashboard: {
                    ...state.openDashboard,
                    id: state.openDashboard.id === action.oldId ? action.newId : state.openDashboard.id,
                },
            }

        }

        default:
            return state
    }
}

export default dashboard
