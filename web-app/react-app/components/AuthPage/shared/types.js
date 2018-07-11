// @flow

export type FormFields = {
    [string]: any,
}

export type Errors = {
    [string]: string,
}

export type FieldSetter = (string, any, ?() => void) => void

export type FieldErrorSetter = (string, string) => void

export type FlagSetter = (boolean) => void

export type ErrorHandler = (Error) => void

export type AuthFlowProps = {
    errors: Errors,
    form: FormFields,
    isProcessing: boolean,
    next: () => void,
    prev: () => void,
    setFieldError: FieldErrorSetter,
    setFormField: FieldSetter,
    setIsProcessing: () => void,
    step: number,
    onComplete: () => void,
}
