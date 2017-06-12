// @flow

export type Err = {
    error: string,
    code?: string
}

export default (res: {
    response?: {
        data: Err
    }
}) : Err => {
    if (res.response) {
        return res.response.data
    } else {
        return {
            error: 'Something went wrong'
        }
    }
}