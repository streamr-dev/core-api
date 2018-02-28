// @flow

import type {Dashboard} from '../dashboard-types'
import type {ErrorInUi} from '../common-types'
import {
    UPDATE_AND_SAVE_DASHBOARD_REQUEST,
    UPDATE_AND_SAVE_DASHBOARD_SUCCESS,
    UPDATE_AND_SAVE_DASHBOARD_FAILURE,
    DELETE_DASHBOARD_REQUEST,
    DELETE_DASHBOARD_SUCCESS,
    DELETE_DASHBOARD_FAILURE,
    GET_AND_REPLACE_DASHBOARDS_REQUEST,
    GET_AND_REPLACE_DASHBOARDS_SUCCESS,
    GET_AND_REPLACE_DASHBOARDS_FAILURE,
    GET_DASHBOARD_REQUEST,
    GET_DASHBOARD_SUCCESS,
    GET_DASHBOARD_FAILURE,
    GET_MY_DASHBOARD_PERMISSIONS_REQUEST,
    GET_MY_DASHBOARD_PERMISSIONS_SUCCESS,
    GET_MY_DASHBOARD_PERMISSIONS_FAILURE,
    UPDATE_DASHBOARD,
    CREATE_DASHBOARD,
    LOCK_DASHBOARD_EDITING,
    UNLOCK_DASHBOARD_EDITING,
    OPEN_DASHBOARD,
    CHANGE_DASHBOARD_ID
} from '../../actions/dashboard'

export type Action = {
    type: typeof UPDATE_AND_SAVE_DASHBOARD_REQUEST
        | typeof DELETE_DASHBOARD_REQUEST
        | typeof GET_AND_REPLACE_DASHBOARDS_REQUEST
} | {
    type: typeof UPDATE_DASHBOARD
        | typeof CREATE_DASHBOARD
        | typeof GET_DASHBOARD_SUCCESS
        | typeof UPDATE_AND_SAVE_DASHBOARD_SUCCESS,
    dashboard: Dashboard
} | {
    type: typeof OPEN_DASHBOARD
        | typeof LOCK_DASHBOARD_EDITING
        | typeof UNLOCK_DASHBOARD_EDITING
        | typeof GET_DASHBOARD_REQUEST
        | typeof GET_AND_REPLACE_DASHBOARDS_REQUEST
        | typeof DELETE_DASHBOARD_REQUEST
        | typeof DELETE_DASHBOARD_SUCCESS
        | typeof GET_MY_DASHBOARD_PERMISSIONS_REQUEST,
    id: $ElementType<Dashboard, 'id'>
} | {
    type: typeof GET_AND_REPLACE_DASHBOARDS_SUCCESS,
    dashboards: Array<Dashboard>
} | {
    type: typeof CHANGE_DASHBOARD_ID,
    oldId: $ElementType<Dashboard, 'id'>,
    newId: $ElementType<Dashboard, 'id'>
} | {
    type: typeof GET_MY_DASHBOARD_PERMISSIONS_SUCCESS,
    id: $ElementType<Dashboard, 'id'>,
    permissions: Array<string>
} | {
    type: typeof GET_AND_REPLACE_DASHBOARDS_FAILURE
        | typeof GET_DASHBOARD_FAILURE
        | typeof DELETE_DASHBOARD_FAILURE
        | typeof UPDATE_AND_SAVE_DASHBOARD_FAILURE,
    error: ErrorInUi
} | {
    type: typeof GET_MY_DASHBOARD_PERMISSIONS_FAILURE,
    id: $ElementType<Dashboard, 'id'>,
    error: ErrorInUi
}
