
declare var Streamr: {
    createLink: Function
}

export default (uri) => Streamr.createLink({
    uri
})