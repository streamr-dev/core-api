// @flow

declare var Streamr: {
    createLink: Function
}

export default (uri: string) => Streamr.createLink({
    uri
})
