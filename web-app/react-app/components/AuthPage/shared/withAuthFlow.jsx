// @flow

import * as React from 'react'

import RedirectAuthenticated from './RedirectAuthenticated'
import type {
    FormFields,
    Errors,
} from './types'
import { getDisplayName } from './utils'

type State = {
    form: FormFields,
    errors: Errors,
    isProcessing: boolean,
    step: number,
    complete: boolean,
}

const withAuthFlow = (WrappedComponent: React.ComponentType<any>, step: number, initialFormFields: FormFields) => {
    class WithAuthFlow extends React.Component<{}, State> {
        static displayName = `WithAuthFlow(${getDisplayName(WrappedComponent)})`

        setFieldError: Function
        setFormField: Function
        setIsProcessing: Function
        setStep: Function
        prev: Function
        next: Function
        markAsComplete: Function

        state = {
            step,
            isProcessing: false,
            form: initialFormFields,
            errors: {},
            complete: false,
        }

        constructor(props: {}) {
            super(props)

            this.setFieldError = this.setFieldError.bind(this)
            this.setFormField = this.setFormField.bind(this)
            this.setIsProcessing = this.setIsProcessing.bind(this)
            this.setStep = this.setStep.bind(this)
            this.prev = this.prev.bind(this)
            this.next = this.next.bind(this)
            this.markAsComplete = this.markAsComplete.bind(this)
        }

        setFieldError(field: string, message: string, callback?: () => void): void {
            const errors = {
                ...this.state.errors,
                [field]: message,
            }
            if (!message) {
                delete errors[field]
            }
            this.setState({
                errors,
            }, callback)
        }

        setFormField(field: string, value: any, callback?: () => void): void {
            this.setFieldError(field, '', () => {
                this.setState({
                    form: {
                        ...this.state.form,
                        [field]: value,
                    },
                }, callback)
            })
        }

        setIsProcessing(isProcessing: boolean, callback?: () => void): void {
            this.setState({
                isProcessing,
            }, callback)
        }

        setStep(step: number, callback?: () => void): void {
            this.setState({
                step,
            }, callback)
        }

        prev(callback?: () => void): void {
            this.setStep(Math.max(0, this.state.step - 1), callback)
        }

        next(callback?: () => void): void {
            this.setStep(this.state.step + 1, callback)
        }

        markAsComplete(callback?: () => void): void {
            this.setState({
                complete: true,
            }, callback)
        }

        render() {
            const { step, isProcessing, errors, form, complete } = this.state

            return (
                <React.Fragment>
                    <WrappedComponent
                        {...this.props}
                        next={this.next}
                        prev={this.prev}
                        setFormField={this.setFormField}
                        setFieldError={this.setFieldError}
                        setIsProcessing={this.setIsProcessing}
                        step={step}
                        isProcessing={isProcessing}
                        errors={errors}
                        form={form}
                        redirect={this.markAsComplete}
                    />
                    <RedirectAuthenticated blindly={complete} />
                </React.Fragment>
            )
        }
    }

    return WithAuthFlow
}

export default withAuthFlow
