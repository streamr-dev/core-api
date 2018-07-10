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

    timeout: TimeoutID

    root: ?HTMLFormElement = null

    setProcessing = (value: boolean) => {
        const { setIsProcessing } = this.props

        if (setIsProcessing) {
            setIsProcessing(value)
        }
    }

    setRoot = (ref: ?HTMLFormElement) => {
        this.root = ref
    }

    validate = (): Promise<any> => {
        const { form, validationSchema } = this.props
        return (validationSchema || yup.object()).validate(form || {})
    }

    focus = () => {
        clearTimeout(this.timeout)

        this.timeout = setTimeout(() => {
            if (this.root) {
                const input = this.root.querySelector('input:not([style])')

                if (input) {
                    input.focus()
                }
            }
        }, 100)
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

    componentDidMount = () => {
        if (this.props.current) {
            this.focus()
        }
    }

    componentDidUpdate = ({ current: prevCurrent }: Props) => {
        const { current } = this.props

        if (current && prevCurrent !== current) {
            this.focus()
        }
    }

    render = () => (
        <form onSubmit={this.onSubmit} ref={this.setRoot}>
            {this.props.children}
        </form>
    )
}

export default AuthStep
