// @flow

import {
    GET_AND_REPLACE_ACCOUNTS_REQUEST,
    GET_AND_REPLACE_ACCOUNTS_SUCCESS,
    GET_AND_REPLACE_ACCOUNTS_FAILURE,
    GET_ACCOUNTS_BY_TYPE_REQUEST,
    GET_ACCOUNTS_BY_TYPE_SUCCESS,
    GET_ACCOUNTS_BY_TYPE_FAILURE,
    CREATE_ACCOUNT_REQUEST,
    CREATE_ACCOUNT_SUCCESS,
    CREATE_ACCOUNT_FAILURE,
    DELETE_ACCOUNT_REQUEST,
    DELETE_ACCOUNT_SUCCESS,
    DELETE_ACCOUNT_FAILURE
} from '../actions/accounts.js'

declare var _: any

type Account = {
    name: string,
    type: string,
    json: {}
}

type State = {
    listsByType?: {
        [string]: Array<Account>
    },
    error?: ?string,
    fetching?: boolean
}

type Action = {
    actionType: string,
    accountType?: string,
    account?: Account,
    accounts?: Array<Account>,
    error?: string,
    id: string
}

const initialState = {
    listsByType: {},
    error: null,
    fetching: false
}

const user = function(state: State = initialState, action: Action) : State {
    
    switch (action.actionType) {
        case GET_AND_REPLACE_ACCOUNTS_REQUEST:
        case GET_ACCOUNTS_BY_TYPE_REQUEST:
        case CREATE_ACCOUNT_REQUEST:
        case DELETE_ACCOUNT_REQUEST:
            return {
                ...state,
                fetching: true
            }
        case GET_AND_REPLACE_ACCOUNTS_SUCCESS:
            return {
                ...state,
                listsByType: _.groupBy(action.accounts, account => account.type),
                fetching: false,
                error: null
            }
        case GET_ACCOUNTS_BY_TYPE_SUCCESS:
            if (!action.accountType) {
                throw new Error(`${GET_ACCOUNTS_BY_TYPE_SUCCESS} requires action.actionType`)
            }
            return {
                ...state,
                listsByType: {
                    ...state.listsByType,
                    [action.accountType]: action.accounts
                },
                error: null,
                fetching: false
            }
        case CREATE_ACCOUNT_SUCCESS:
            if (!action.account || !action.account.type) {
                throw new Error(`${GET_ACCOUNTS_BY_TYPE_SUCCESS} requires action.account and action.account.type`)
            }
            // These are just to make sure flow is happy
            const listsByType = state.listsByType || {}
            const existing = listsByType[action.account.type] || []
            
            return {
                ...state,
                listsByType: {
                    ...state.listsByType,
                    [action.account.type]: [
                        ...existing,
                        action.account
                    ]
                },
                error: null,
                fetching: false
            }
        case DELETE_ACCOUNT_SUCCESS:
            return {
                ...state,
                listsByType: _.mapObject(state.listsByType, list => _.reject(list, account => account.id === action.id)),
                error: null,
                fetching: false
            }
        case GET_AND_REPLACE_ACCOUNTS_FAILURE:
        case GET_ACCOUNTS_BY_TYPE_FAILURE:
        case CREATE_ACCOUNT_FAILURE:
        case DELETE_ACCOUNT_FAILURE:
            return {
                ...state,
                fetching: false,
                error: action.error
            }
        default:
            return state
    }
}

export default user