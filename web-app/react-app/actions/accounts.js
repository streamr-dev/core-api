// @flow

import axios from 'axios'
import parseError from './utils/parseError'

export const GET_ALL_ACCOUNTS_REQUEST = 'GET_ALL_ACCOUNTS_REQUEST'
export const GET_ALL_ACCOUNTS_SUCCESS = 'GET_ALL_ACCOUNTS_SUCCESS'
export const GET_ALL_ACCOUNTS_FAILURE = 'GET_ALL_ACCOUNTS_FAILURE'

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

export const getAllAccounts = () => (dispatch: Function) => {
    dispatch(getAllAccountsRequest())
    return axios.get(Streamr.createLink({
        uri: apiUrl
    }))
        .then(({data}) => dispatch(getAllAccountsSuccess(data)))
        .catch(res => {
            const e = parseError(res)
            dispatch(getAllAccountsFailure(e))
            throw e
        })
}

export const createAccount = (account: {
    name: string,
    type: string,
    json: {}
}) => (dispatch: Function) => {
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

const getAllAccountsRequest = () => ({
    type: GET_ALL_ACCOUNTS_REQUEST,
})

const getAllAccountsSuccess = accounts => ({
    type: GET_ALL_ACCOUNTS_SUCCESS,
    accounts
})

const getAllAccountsFailure = error => ({
    type: GET_ALL_ACCOUNTS_FAILURE,
    error
})

const createAccountRequest = () => ({
    type: CREATE_ACCOUNT_REQUEST,
})

const createAccountSuccess = account => ({
    type: CREATE_ACCOUNT_SUCCESS,
    account
})

const createAccountFailure = error => ({
    type: CREATE_ACCOUNT_FAILURE,
    error
})

const deleteAccountRequest = id => ({
    type: DELETE_ACCOUNT_REQUEST,
    id
})

const deleteAccountSuccess = id => ({
    type: DELETE_ACCOUNT_SUCCESS,
    id
})

const deleteAccountFailure = error => ({
    type: DELETE_ACCOUNT_FAILURE,
    error
})
