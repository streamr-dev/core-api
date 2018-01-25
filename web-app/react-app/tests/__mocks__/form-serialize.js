
export default (form, opt) => {
    if (!opt || !opt.hash) {
        throw new Error('hash: true required')
    }
    return form
}