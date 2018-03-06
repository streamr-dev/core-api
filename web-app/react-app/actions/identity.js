// @flow

import createLink from '../helpers/createLink'
import {error, success} from 'react-notification-system-redux'
import axios from 'axios/index'
import {parseError} from './utils/parseApiResponse'
import type {ErrorInUi} from '../flowtype/common-types'
import ownWeb3 from '../utils/web3Instance'
import type {IntegrationKey} from '../flowtype/integration-key-types'

const apiUrl = 'api/v1/integration_keys'

export const CREATE_IDENTITY_REQUEST = 'CREATE_IDENTITY_REQUEST'
export const CREATE_IDENTITY_SUCCESS = 'CREATE_IDENTITY_SUCCESS'
export const CREATE_IDENTITY_FAILURE = 'CREATE_IDENTITY_FAILURE'

export const createIdentity = (integrationKey: IntegrationKey) => (dispatch: Function) => {
    dispatch(createIdentityRequest(integrationKey))
    if (!ownWeb3) {
        dispatch(createIdentityFailure({
            message: 'MetaMask browser extension is not installed'
        }))
        dispatch(error({
            title: 'Create identity failed',
            message: 'MetaMask browser extension is not installed',
        }))
        return
    }
    if (ownWeb3.eth.defaultAccount == null) {
        dispatch(createIdentityFailure({
            message: 'MetaMask browser extension is locked'
        }))
        dispatch(error({
            title: 'Create identity failed',
            message: 'MetaMask browser extension is locked',
        }))
        return
    }
    let challenge
    axios.post(createLink('api/v1/login/challenge')).then(({data}) => {
        challenge = data
        return ownWeb3.eth.personal.sign(data.challenge, ownWeb3.eth.defaultAccount)
    })
        .then((signature) => {
            const input = {
                ...integrationKey,
                signature: signature,
                address: ownWeb3.eth.defaultAccount,
                challenge: {
                    ...challenge
                },
            }
            return axios.post(createLink(apiUrl), input)
                .then((response) => {
                    dispatch(createIdentitySuccess(response.data))
                    dispatch(success({
                        title: 'Success!',
                        message: 'New identity created',
                    }))
                })

        })
        .catch((response) => {
            const err = parseError(response)
            dispatch(createIdentityFailure(err))
            dispatch(error({
                title: 'Create identity failed',
                message: err.message,
            }))
        })
}

const createIdentityRequest = (integrationKey: IntegrationKey) => ({
    type: CREATE_IDENTITY_REQUEST,
    integrationKey,
})

const createIdentitySuccess = (integrationKey: IntegrationKey) => ({
    type: CREATE_IDENTITY_SUCCESS,
    integrationKey,
})

const createIdentityFailure = (error: ErrorInUi) => ({
    type: CREATE_IDENTITY_FAILURE,
    error,
})
