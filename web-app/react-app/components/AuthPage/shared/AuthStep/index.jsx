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
    onProcessing?: FlagSetter,
    onSubmit: () => Promise<any>,
    onSuccess?: () => void,
    onFailure?: ErrorHandler,
    next?: () => void,
    form?: FormFields,
}

class AuthStep extends React.Component<Props> {
    static defaultProps = {
        step: 0,
        totalSteps: 0,
        onSubmit: (): Promise<any> => Promise.resolve(),
    }

    setProcessing = (value: boolean) => {
        const { onProcessing } = this.props

        if (onProcessing) {
            onProcessing(value)
        }
    }

    validate = (): Promise<any> => new Promise((resolve, reject) => {
        setTimeout(() => {
            const { form } = this.props
            const schema = this.props.validationSchema || yup.object()

            schema
                .validate(form || {})
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

    render = () => (
        <form onSubmit={this.onSubmit}>
            {this.props.children}
        </form>
    )
}

export default AuthStep
