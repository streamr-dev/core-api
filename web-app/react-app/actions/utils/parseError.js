// @flow

type Err = {
    error: string,
    code?: string
}

export default (res: {
    response?: {
        data: Err
    }
}) : Err => (res.response || {}).data || {
    error: 'Something went wrong'
}