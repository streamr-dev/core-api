// @flow

export default (res: Function) => (res.data && res.data.error) || (res.response && res.response.data && (res.response.data.error || res.response.data.message)) || (res.message)