/* globals Streamr */

import axios from 'axios'
import path from 'path'

export const GET_ALL_ACCOUNTS_REQUEST = 'GET_ALL_ACCOUNTS_REQUEST'
export const GET_ALL_ACCOUNTS_SUCCESS = 'GET_ALL_ACCOUNTS_SUCCESS'
export const GET_ALL_ACCOUNTS_FAILURE = 'GET_ALL_ACCOUNTS_FAILURE'

export const CREATE_ACCOUNT_REQUEST = 'CREATE_ACCOUNT_REQUEST'
export const CREATE_ACCOUNT_SUCCESS = 'CREATE_ACCOUNT_SUCCESS'
export const CREATE_ACCOUNT_FAILURE = 'CREATE_ACCOUNT_FAILURE'

export const DELETE_ACCOUNT_REQUEST = 'DELETE_ACCOUNT_REQUEST'
export const DELETE_ACCOUNT_SUCCESS = 'DELETE_ACCOUNT_SUCCESS'
export const DELETE_ACCOUNT_FAILURE = 'DELETE_ACCOUNT_FAILURE'

const parseError = (res) => (res.data && res.data.error) || (res.response && res.response.data && res.response.data.error) || (res.message)
const apiUrl = 'api/v1/accounts'

export const getAllAccounts = () => dispatch => {
    dispatch(getAllAccountsRequest())
    axios.get(Streamr.createLink({
        uri: apiUrl
    }))
        .then(({data}) => dispatch(getAllAccountsSuccess(data)))
        .catch(res => dispatch(getAllAccountsFailure(parseError(res))))
}

export const createAccount = account => dispatch => {
    dispatch(createAccountRequest())
    axios.post(Streamr.createLink({
        uri: apiUrl
    }), account)
        .then(({data}) => dispatch(createAccountSuccess(data)))
        .catch(res => dispatch(createAccountFailure(parseError(res))))
}

export const deleteAccount = id => dispatch => {
    dispatch(deleteAccountRequest(id))
    axios.delete(Streamr.createLink({
        uri: `${apiUrl}/${id}`
    }))
        .then(() => dispatch(deleteAccountSuccess(id)))
        .catch(res => dispatch(deleteAccountFailure(parseError(res))))
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
