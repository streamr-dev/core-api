// @flow

import {
    GET_ALL_ACCOUNTS_REQUEST,
    GET_ALL_ACCOUNTS_SUCCESS,
    GET_ALL_ACCOUNTS_FAILURE,
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

const initialState = {
    list: [],
    error: null,
    fetching: false
}

const user = (state: {
    list?: Array<Account>,
    error?: ?string,
    fetching?: boolean
} = initialState, action: {
    type: string,
    account?: Account,
    accounts?: Array<Account>,
    error?: string,
    id: string
}) => {
    switch (action.type) {
        case GET_ALL_ACCOUNTS_REQUEST:
        case CREATE_ACCOUNT_REQUEST:
        case DELETE_ACCOUNT_REQUEST:
            return {
                ...state,
                fetching: true
            }
        case GET_ALL_ACCOUNTS_SUCCESS:
            return {
                ...state,
                list: action.accounts,
                fetching: false,
                error: null
            }
        case CREATE_ACCOUNT_SUCCESS:
            return {
                ...state,
                list: [...state.list || [], action.account], //  || [] is because of flowtype which cannot understand default values
                error: null,
                fetching: false
            }
        case DELETE_ACCOUNT_SUCCESS:
            return {
                ...state,
                list: _.reject(state.list, account => account.id === action.id),
                error: null,
                fetching: false
            }
        case CREATE_ACCOUNT_FAILURE:
        case GET_ALL_ACCOUNTS_FAILURE:
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