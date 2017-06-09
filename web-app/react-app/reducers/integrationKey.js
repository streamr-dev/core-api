// @flow

import {
    GET_AND_REPLACE_INTEGRATION_KEYS_REQUEST,
    GET_AND_REPLACE_INTEGRATION_KEYS_SUCCESS,
    GET_AND_REPLACE_INTEGRATION_KEYS_FAILURE,
    GET_INTEGRATION_KEYS_BY_TYPE_REQUEST,
    GET_INTEGRATION_KEYS_BY_TYPE_SUCCESS,
    GET_INTEGRATION_KEYS_BY_TYPE_FAILURE,
    CREATE_INTEGRATION_KEY_REQUEST,
    CREATE_INTEGRATION_KEY_SUCCESS,
    CREATE_INTEGRATION_KEY_FAILURE,
    DELETE_INTEGRATION_KEY_REQUEST,
    DELETE_INTEGRATION_KEY_SUCCESS,
    DELETE_INTEGRATION_KEY_FAILURE
} from '../actions/integrationKey.js'

declare var _: any

import type {State, Action} from '../flowtype/integration-key-types.js'

const initialState = {
    listsByService: {},
    error: null,
    fetching: false
}

const integrationKey = function(state: State = initialState, action: Action) : State {
    
    switch (action.type) {
        case GET_AND_REPLACE_INTEGRATION_KEYS_REQUEST:
        case GET_INTEGRATION_KEYS_BY_TYPE_REQUEST:
        case CREATE_INTEGRATION_KEY_REQUEST:
        case DELETE_INTEGRATION_KEY_REQUEST:
            return {
                ...state,
                fetching: true
            }
        case GET_AND_REPLACE_INTEGRATION_KEYS_SUCCESS:
            return {
                ...state,
                listsByService: _.groupBy(action.integrationKeys, integrationKey => integrationKey.type),
                fetching: false,
                error: null
            }
        case GET_INTEGRATION_KEYS_BY_TYPE_SUCCESS: {
            if (!action.service) {
                throw new Error(`${GET_INTEGRATION_KEYS_BY_TYPE_SUCCESS} requires action.service`)
            }
            return {
                ...state,
                listsByService: {
                    ...state.listsByService,
                    [action.service]: action.integrationKeys
                },
                error: null,
                fetching: false
            }
        }
        case CREATE_INTEGRATION_KEY_SUCCESS: {
            if (!action.integrationKey || !action.integrationKey.type) {
                throw new Error(`${GET_INTEGRATION_KEYS_BY_TYPE_SUCCESS} requires action.integrationKey and action.integrationKey.type`)
            }
            // These are just to make sure that flow is happy
            const listsByService = state.listsByService || {}
            const existing = listsByService[action.integrationKey.type] || []
            
            return {
                ...state,
                listsByService: {
                    ...listsByService,
                    [action.integrationKey.type]: [
                        ...existing,
                        action.integrationKey
                    ]
                },
                error: null,
                fetching: false
            }
        }
        case DELETE_INTEGRATION_KEY_SUCCESS: {
            return {
                ...state,
                listsByService: _.mapValues(state.listsByService, list => _.reject(list, integrationKey => integrationKey.id === action.id)),
                error: null,
                fetching: false
            }
        }
        case GET_AND_REPLACE_INTEGRATION_KEYS_FAILURE:
        case GET_INTEGRATION_KEYS_BY_TYPE_FAILURE:
        case CREATE_INTEGRATION_KEY_FAILURE:
        case DELETE_INTEGRATION_KEY_FAILURE:
            return {
                ...state,
                fetching: false,
                error: action.error
            }
        default:
            return state
    }
}

export default integrationKey