// @flow

declare var Streamr: {
    createLink: Function
}

const createLink = (uri: string) => Streamr.createLink({
    uri
})

export default createLink
