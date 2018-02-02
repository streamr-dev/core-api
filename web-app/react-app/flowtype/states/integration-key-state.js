// @flow

import type {IntegrationKey} from '../integration-key-types.js'
import type {ApiError} from '../common-types'

export type IntegrationKeyState = {
    listsByService: {
        [string]: Array<IntegrationKey>
    },
    error: ?ApiError,
    fetching: boolean
}
