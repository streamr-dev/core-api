// @flow

export const preventDefault = (callback: Function, ...args: Array<any>) => (e: SyntheticEvent<EventTarget>) => {
    e.preventDefault()
    callback.apply(null, args)
}

export const onInputChange = (callback: (string, any) => void) => (e: SyntheticInputEvent<EventTarget>) => {
    const value = e.target.type === 'checkbox' ? e.target.checked : e.target.value
    callback(e.target.name, value)
}
