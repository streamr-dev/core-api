// @flow

export const preventDefault = (callback: Function, ...args: Array<any>) => (e: SyntheticInputEvent<EventTarget>) => {
    e.preventDefault()
    callback.apply(null, args)
}

export const onInputChange = (callback: (string, any) => void) => (e: SyntheticInputEvent<EventTarget>) => {
    const value = e.target.type === 'checkbox' ? e.target.checked : e.target.value
    callback(e.target.name, value)
}

export const onEnterKeyDown = (callback: Function, ...args: Array<any>) => (e: SyntheticKeyboardEvent<EventTarget>) => {
    const keyCode = e.keyCode || e.which
    if (keyCode === 13) { // 13 for Enter
        callback.apply(null, args)
    }
}
