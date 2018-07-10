// @flow

import * as React from 'react'
import * as yup from 'yup'
import type {
    FieldErrorSetter,
    FlagSetter,
    ErrorHandler,
    FormFields,
} from '../types'

type PanelProps = {
    title: string,
}

type Props = PanelProps & {
    children: React.Node,
    validationSchema?: ?yup.Schema,
    onValidationError?: FieldErrorSetter,
    step: number,
    totalSteps: number,
    setIsProcessing?: FlagSetter,
    onSubmit: () => Promise<any>,
    onSuccess?: () => void,
    onFailure?: ErrorHandler,
    next?: () => void,
    form?: FormFields,
    current?: boolean,
}

class AuthStep extends React.Component<Props> {
    static defaultProps = {
        step: 0,
        totalSteps: 0,
        onSubmit: (): Promise<any> => Promise.resolve(),
    }

    setProcessing = (value: boolean) => {
        const { setIsProcessing } = this.props

        if (setIsProcessing) {
            setIsProcessing(value)
        }
    }

    validate = (): Promise<any> => {
        const { form, validationSchema } = this.props
        return (validationSchema || yup.object()).validate(form || {})
    }

    onSubmit = (e: SyntheticEvent<EventTarget>) => {
        e.preventDefault()
        const { onValidationError, step, totalSteps, onSubmit, onSuccess, onFailure, next } = this.props

        this.setProcessing(true)

        this.validate()
            .then(() => {
                return onSubmit()
                    .then(() => {
                        this.setProcessing(false)
                        if (onSuccess) {
                            onSuccess()
                        }
                        if ((step < totalSteps - 1) && next) {
                            next()
                        }
                    }, (error) => {
                        this.setProcessing(false)
                        if (onFailure) {
                            onFailure(error)
                        }
                    })
            }, (error: yup.ValidationError) => {
                this.setProcessing(false)
                if (!onValidationError) {
                    throw error
                }
                onValidationError(error.path, error.message)
            })
    }

    render = () => (
        <form onSubmit={this.onSubmit}>
            {this.props.children}
        </form>
    )
}

export default AuthStep
