// @flow

import * as React from 'react'
import * as yup from 'yup'

type PanelProps = {
    title: string,
}

type Props = PanelProps & {
    children: React.Node,
    validationSchema?: ?yup.Schema,
    onValidationError?: (string, string) => void,
    step: number,
    totalSteps: number,
    onProcessing?: (boolean) => void,
    onSubmit: () => Promise<any>,
    onSuccess?: () => void,
    onFailure?: (Error) => void,
    next?: () => void,
}

class AuthStep extends React.Component<Props> {
    static defaultProps = {
        step: 0,
        totalSteps: 0,
        onSubmit: (): Promise<any> => Promise.resolve(),
    }

    form: ?HTMLFormElement = null

    setForm = (form: ?HTMLFormElement) => {
        this.form = form
    }

    formData = () => {
        const form = this.form

        if (form) {
            return [...form.querySelectorAll('input[name]')].reduce((memo, element: any) => {
                const input: HTMLInputElement = element
                return {
                    ...memo,
                    [input.name]: (input.type === 'checkbox' ? input.checked : input.value),
                }
            }, {})
        }

        return {}
    }

    setProcessing = (value: boolean) => {
        const { onProcessing } = this.props

        if (onProcessing) {
            onProcessing(value)
        }
    }

    validate = (): Promise<any> => new Promise((resolve, reject) => {
        setTimeout(() => {
            (this.props.validationSchema || yup.object())
                .validate(this.formData())
                .then(resolve, reject)
        }, 500)
    })

    onSubmit = (e: SyntheticEvent<EventTarget>) => {
        e.preventDefault()
        const { onValidationError, step, totalSteps, onSubmit, onSuccess, onFailure, next } = this.props

        this.setProcessing(true)

        this.validate()
            .then(() => {
                this.setProcessing(false)
                return onSubmit()
                    .then(() => {
                        if (onSuccess) {
                            onSuccess()
                        }
                        if ((step < totalSteps - 1) && next) {
                            next()
                        }
                    }, onFailure)
            }, (error: yup.ValidationError) => {
                this.setProcessing(false)
                if (!onValidationError) {
                    throw error
                }
                onValidationError(error.path, error.message)
            })
    }

    render = () => {
        const { children } = this.props

        return (
            <form onSubmit={this.onSubmit} ref={this.setForm}>
                {children}
            </form>
        )
    }
}

export default AuthStep
