// @flow

import * as React from 'react'
import * as yup from 'yup'
import debounce from 'lodash/debounce'
import cx from 'classnames'

import styles from './authStep.pcss'

import type {
    FieldErrorSetter,
    FlagSetter,
    ErrorHandler,
    FormFields,
} from '../types'

export {
    styles,
}

type PanelProps = {
    title: string,
}

type Props = PanelProps & {
    className?: string,
    children: React.Node,
    validationSchema?: ?yup.Schema,
    onValidationError?: FieldErrorSetter,
    step: number,
    totalSteps: number,
    setIsProcessing?: FlagSetter,
    isProcessing?: boolean,
    onSubmit: () => Promise<any>,
    onSuccess?: () => void,
    onFailure?: ErrorHandler,
    next?: () => void,
    form?: FormFields,
    current?: boolean,
    autoSubmitOnChange?: Array<string>,
}

class AuthStep extends React.Component<Props> {
    static defaultProps = {
        step: 0,
        totalSteps: 0,
        onSubmit: (): Promise<any> => Promise.resolve(),
    }

    form: ?HTMLFormElement = null

    setForm = (ref: ?HTMLFormElement) => {
        this.form = ref
    }

    onFieldChange = (event: any) => {
        const e: SyntheticInputEvent<EventTarget> = event
        const { autoSubmitOnChange } = this.props

        if ((autoSubmitOnChange || []).indexOf(e.target.name) !== -1) {
            this.debouncedScheduleSubmit()
        }
    }

    componentDidMount() {
        const form = this.form

        if (form) {
            form.addEventListener('change', this.onFieldChange)
        }
    }

    componentWillUnmount() {
        const form = this.form

        if (form) {
            form.removeEventListener('change', this.onFieldChange)
        }
    }

    componentDidUpdate({ isProcessing: prevIsProcessing }: Props) {
        const { isProcessing } = this.props

        if (isProcessing && (isProcessing !== prevIsProcessing)) {
            this.submit()
        }
    }

    setProcessing = (value: boolean, callback?: () => void) => {
        const { setIsProcessing } = this.props

        if (setIsProcessing) {
            setIsProcessing(value, callback)
        } else if (callback) {
            callback()
        }
    }

    validate = (): Promise<any> => {
        const { form, validationSchema } = this.props
        return (validationSchema || yup.object()).validate(form || {})
    }

    scheduleSubmit = () => {
        this.setProcessing(true)
    }

    debouncedScheduleSubmit = debounce(this.scheduleSubmit, 500)

    submit(): Promise<any> {
        const { onValidationError, step, totalSteps, onSubmit, onSuccess, onFailure, next } = this.props

        return this.validate()
            .then(() => {
                return onSubmit()
                    .then(() => new Promise((resolve) => {
                        this.setProcessing(false, () => {
                            if (onSuccess) {
                                onSuccess()
                            }
                            if ((step < totalSteps - 1) && next) {
                                next()
                            }
                            resolve()
                        })
                    }), (error) => {
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

    onSubmit = (e: SyntheticEvent<EventTarget>) => {
        e.preventDefault()
        this.debouncedScheduleSubmit.cancel()
        this.scheduleSubmit()
    }

    render() {
        const { children, className } = this.props

        return (
            <form
                onSubmit={this.onSubmit}
                ref={this.setForm}
                className={cx(styles.root, className)}
            >
                {children}
            </form>
        )
    }
}

export default AuthStep
