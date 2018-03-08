// @flow

import type { ErrorFromApi, ErrorInUi } from '../../flowtype/common-types'

type Args = Error | {
    response: {
        data: ErrorFromApi,
        status: number
    }
}

export const parseError = (args: Args): ErrorInUi => {
    if (args instanceof Error) {
        return {
            message: args.message
        }
    } else {
        const {data: {message, code}, status} = args.response
        return {
            message: message || 'Something went wrong',
            code: code,
            statusCode: status
        }
    }
}
