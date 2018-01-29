// @flow

import type { ErrorFromApi, ErrorInUi } from '../../flowtype/common-types'

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
