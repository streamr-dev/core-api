// @flow

export const preventDefault = (callback: Function) => (e: SyntheticInputEvent<EventTarget>) => {
    e.preventDefault()
    callback()
}

export const onInputChange = (callback: (string, any) => void) => (e: SyntheticInputEvent<EventTarget>) => {
    const value = e.target.type === 'checkbox' ? e.target.checked : e.target.value
    callback(e.target.name, value)
}
