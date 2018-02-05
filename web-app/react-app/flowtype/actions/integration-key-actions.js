// @flow

import type {IntegrationKey} from '../integration-key-types'
import type {ErrorInUi} from '../common-types'

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
} from '../../actions/integrationKey'

export type IntegrationKeyAction = {
    type: typeof GET_AND_REPLACE_INTEGRATION_KEYS_REQUEST
        | typeof CREATE_INTEGRATION_KEY_REQUEST
} | {
    type: typeof GET_INTEGRATION_KEYS_BY_SERVICE_REQUEST,
    service: $ElementType<IntegrationKey, 'service'>
} | {
    type: typeof DELETE_INTEGRATION_KEY_SUCCESS
        | typeof DELETE_INTEGRATION_KEY_REQUEST,
    id: $ElementType<IntegrationKey, 'id'>
} | {
    type: typeof GET_AND_REPLACE_INTEGRATION_KEYS_SUCCESS,
    integrationKeys: Array<IntegrationKey>
} | {
    type: typeof GET_INTEGRATION_KEYS_BY_SERVICE_SUCCESS,
    integrationKeys: Array<IntegrationKey>,
    service: $ElementType<IntegrationKey, 'service'>
} | {
    type: typeof CREATE_INTEGRATION_KEY_SUCCESS,
    integrationKey: IntegrationKey
} | {
    type: typeof GET_AND_REPLACE_INTEGRATION_KEYS_FAILURE
        | typeof CREATE_INTEGRATION_KEY_FAILURE
        | typeof DELETE_INTEGRATION_KEY_FAILURE,
    error: ErrorInUi
} | {
    type: typeof GET_INTEGRATION_KEYS_BY_SERVICE_FAILURE,
    service: $ElementType<IntegrationKey, 'service'>,
    error: ErrorInUi
}
