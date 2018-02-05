// @flow

import type {ApiError} from '../../flowtype/common-types'

export default (res: {
    response?: {
        data: ApiError
    },
    message?: string
}): ApiError => (res.response || {}).data || res.message || {
    error: 'Something went wrong'
}