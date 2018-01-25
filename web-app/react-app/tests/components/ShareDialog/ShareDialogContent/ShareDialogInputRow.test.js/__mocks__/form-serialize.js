
export default (form, opt) => {
    if (!opt || !opt.hash) {
        throw new Error('form-serialixe should always be called with hash: true')
    }
    return form
}