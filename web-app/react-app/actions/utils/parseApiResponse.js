// @flow

import type { ErrorFromApi, ErrorInUi } from '../../flowtype/common-types'

type Args = Error & {
    response?: {
        data: ErrorFromApi,
        status: number
    }
}

export const parseError = (args: Args): ErrorInUi => {
    const message = args.response && args.response.data.message || args.message
    const code = args.response && args.response.data.code || undefined
    const status = args.response && args.response.status
    return {
        message: message || 'Something went wrong',
        code: code,
        statusCode: status,
    }
}
