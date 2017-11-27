// @flow

export type Err = {
    error: string,
    code?: string
}

export default (res: {
    response?: {
        data: Err
    },
    message?: string
}) : Err => {
    if (res.response) {
        return res.response.data
    } else {
        return {
            error: res.message || 'Something went wrong'
        }
    }
}