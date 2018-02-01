// @flow

import {
    GET_AND_REPLACE_INTEGRATION_KEYS_REQUEST,
    GET_AND_REPLACE_INTEGRATION_KEYS_SUCCESS,
    GET_AND_REPLACE_INTEGRATION_KEYS_FAILURE,
    GET_INTEGRATION_KEYS_BY_SERVICE_REQUEST,
    GET_INTEGRATION_KEYS_BY_SERVICE_SUCCESS,
    GET_INTEGRATION_KEYS_BY_SERVICE_FAILURE,
    CREATE_INTEGRATION_KEY_REQUEST,
    CREATE_INTEGRATION_KEY_SUCCESS,
    CREATE_INTEGRATION_KEY_FAILURE,
    DELETE_INTEGRATION_KEY_REQUEST,
    DELETE_INTEGRATION_KEY_SUCCESS,
    DELETE_INTEGRATION_KEY_FAILURE
} from '../actions/integrationKey.js'

import _ from 'lodash'

import type {IntegrationKeyState} from '../flowtype/states/integration-key-state'
import type {IntegrationKeyAction} from '../flowtype/actions/integration-key-actions'

const initialState = {
    listsByService: {},
    error: null,
    fetching: false
}

export default function(state: IntegrationKeyState = initialState, action: IntegrationKeyAction): IntegrationKeyState {
    
    switch (action.type) {
        case GET_AND_REPLACE_INTEGRATION_KEYS_REQUEST:
        case GET_INTEGRATION_KEYS_BY_SERVICE_REQUEST:
        case CREATE_INTEGRATION_KEY_REQUEST:
        case DELETE_INTEGRATION_KEY_REQUEST:
            return {
                ...state,
                fetching: true
            }
        case GET_AND_REPLACE_INTEGRATION_KEYS_SUCCESS:
            return {
                ...state,
                listsByService: _.groupBy(action.integrationKeys, integrationKey => integrationKey.service),
                fetching: false,
                error: null
            }
        case GET_INTEGRATION_KEYS_BY_SERVICE_SUCCESS: {
            if (!action.service) {
                throw new Error(`${GET_INTEGRATION_KEYS_BY_SERVICE_SUCCESS} requires action.service`)
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
        case CREATE_INTEGRATION_KEY_SUCCESS:
            return {
                ...state,
                listsByService: {
                    ...(state.listsByService || {}),
                    [action.integrationKey.service]: [
                        ...(state.listsByService[action.integrationKey.service] || []),
                        action.integrationKey
                    ]
                },
                error: null,
                fetching: false
            }
        
        case DELETE_INTEGRATION_KEY_SUCCESS: {
            // This is some hacking for keeping Flow happy
            if (!action.id) {
                return state
            }
            const newListsByService = {}
            Object.keys(state.listsByService).forEach((key) => {
                const keys = state.listsByService[key]
                newListsByService[key] = keys.filter(integrationKey => action.id && action.id !== integrationKey.id)
            })
            return {
                ...state,
                listsByService: newListsByService,
                error: null,
                fetching: false
            }
        }
        
        case GET_AND_REPLACE_INTEGRATION_KEYS_FAILURE:
        case GET_INTEGRATION_KEYS_BY_SERVICE_FAILURE:
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
