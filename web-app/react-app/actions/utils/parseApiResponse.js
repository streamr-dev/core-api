// @flow

import type { ErrorFromApi, ErrorInUi } from '../../flowtype/common-types'

export class StreamUiError extends Error {
    message: string
    statusCode: number
    code: string
    constructor(message: string, code: string, statusCode: number) {
        super(message)
        this.message = message
        this.code = code
        this.statusCode = statusCode
    }
    toString() {
        return this.message
    }
}

export const parseError = ({response}: {
    response: {
        data: ErrorFromApi,
        status: number
    }
}) : ErrorInUi => ({
    message: response.data.message || 'Something went wrong',
    code: response.data.code,
    statusCode: response.status
})
