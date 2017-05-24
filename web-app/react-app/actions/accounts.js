// @flow

import axios from 'axios'
import parseError from './utils/parseError'

export const GET_AND_REPLACE_ACCOUNTS_REQUEST = 'GET_ALL_ACCOUNTS_REQUEST'
export const GET_AND_REPLACE_ACCOUNTS_SUCCESS = 'GET_ALL_ACCOUNTS_SUCCESS'
export const GET_AND_REPLACE_ACCOUNTS_FAILURE = 'GET_ALL_ACCOUNTS_FAILURE'

export const GET_ACCOUNTS_BY_TYPE_REQUEST = 'GET_ACCOUNTS_BY_TYPE_REQUEST'
export const GET_ACCOUNTS_BY_TYPE_SUCCESS = 'GET_ACCOUNTS_BY_TYPE_SUCCESS'
export const GET_ACCOUNTS_BY_TYPE_FAILURE = 'GET_ACCOUNTS_BY_TYPE_FAILURE'

export const CREATE_ACCOUNT_REQUEST = 'CREATE_ACCOUNT_REQUEST'
export const CREATE_ACCOUNT_SUCCESS = 'CREATE_ACCOUNT_SUCCESS'
export const CREATE_ACCOUNT_FAILURE = 'CREATE_ACCOUNT_FAILURE'

export const DELETE_ACCOUNT_REQUEST = 'DELETE_ACCOUNT_REQUEST'
export const DELETE_ACCOUNT_SUCCESS = 'DELETE_ACCOUNT_SUCCESS'
export const DELETE_ACCOUNT_FAILURE = 'DELETE_ACCOUNT_FAILURE'

const apiUrl = 'api/v1/accounts'

declare var Streamr: {
    createLink: Function
}

type Account = {
    name: string,
    type: string,
    json: {}
}

export const getAndReplaceAccounts = () => (dispatch: Function) => {
    dispatch(getAndReplaceAccountsRequest())
    return axios.get(Streamr.createLink({
        uri: apiUrl
    }))
        .then(({data}) => dispatch(getAndReplaceAccountsSuccess(data)))
        .catch(res => {
            const e = parseError(res)
            dispatch(getAndReplaceAccountsFailure(e))
            throw e
        })
}

export const getAccountsByType = (accountType: string) => (dispatch: Function) => {
    dispatch(getAccountsByTypeRequest(type))
    return axios.get(Streamr.createLink({
        uri: apiUrl
    }), {
        accountType
    })
        .then(({data}) => dispatch(getAccountsByTypeSuccess(accountType, data)))
        .catch(res => {
            const e = parseError(res)
            dispatch(getAccountsByTypeFailure(accountType, e))
            throw e
        })
}

export const createAccount = (account: Account) => (dispatch: Function) => {
    dispatch(createAccountRequest())
    return axios.post(Streamr.createLink({
        uri: apiUrl
    }), account)
        .then(({data}) => dispatch(createAccountSuccess(data)))
        .catch(res => {
            const e = parseError(res)
            dispatch(createAccountFailure(e))
            throw e
        })
}

export const deleteAccount = (id: string) => (dispatch: Function) => {
    dispatch(deleteAccountRequest(id))
    return axios.delete(Streamr.createLink({
        uri: `${apiUrl}/${id}`
    }))
        .then(() => dispatch(deleteAccountSuccess(id)))
        .catch(res => {
            const e = parseError(res)
            dispatch(deleteAccountFailure(e))
            throw e
        })
}

const getAndReplaceAccountsRequest = () => ({
    actionType: GET_AND_REPLACE_ACCOUNTS_REQUEST,
})

const getAndReplaceAccountsSuccess = (accounts: Array<Account>) => ({
    actionType: GET_AND_REPLACE_ACCOUNTS_SUCCESS,
    accounts
})

const getAndReplaceAccountsFailure = (error: string) => ({
    actionType: GET_AND_REPLACE_ACCOUNTS_FAILURE,
    error
})

const getAccountsByTypeRequest = (accountType: string) => ({
    actionType: GET_ACCOUNTS_BY_TYPE_REQUEST,
    accountType
})

const getAccountsByTypeSuccess = (accountType: string, accounts: Array<Account>) => ({
    actionType: GET_ACCOUNTS_BY_TYPE_SUCCESS,
    accounts,
    accountType
})

const getAccountsByTypeFailure = (accountType :string, error: string) => ({
    actionType: GET_ACCOUNTS_BY_TYPE_FAILURE,
    error,
    accountType
})

const createAccountRequest = () => ({
    actionType: CREATE_ACCOUNT_REQUEST,
})

const createAccountSuccess = (account: Account) => ({
    actionType: CREATE_ACCOUNT_SUCCESS,
    account
})

const createAccountFailure = (error: string) => ({
    actionType: CREATE_ACCOUNT_FAILURE,
    error
})

const deleteAccountRequest = (id: string) => ({
    actionType: DELETE_ACCOUNT_REQUEST,
    id
})

const deleteAccountSuccess = (id: string) => ({
    actionType: DELETE_ACCOUNT_SUCCESS,
    id
})

const deleteAccountFailure = (error: string) => ({
    actionType: DELETE_ACCOUNT_FAILURE,
    error
})
