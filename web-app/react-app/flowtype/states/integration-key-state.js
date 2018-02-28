// @flow

import type {IntegrationKey} from '../integration-key-types.js'
import type {ErrorInUi} from '../common-types'

export type IntegrationKeyState = {
    listsByService: {
        [string]: Array<IntegrationKey>
    },
    error: ?ErrorInUi,
    fetching: boolean
}
