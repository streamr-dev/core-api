// @flow

import * as React from 'react'
import * as yup from 'yup'
import axios from 'axios'

import AuthPanel from './AuthPanel'

type Step = number

type FormFields = {
    [string]: any,
}

type State = {
    form: FormFields,
    errors: {
        [string]: string,
    },
    isProcessing: boolean,
    step: Step,
}

export type AuthFlowProps = {
    errors: {
        [string]: string,
    },
    form: FormFields,
    next: () => void,
    prev: () => void,
    isProcessing: boolean,
    setIsProcessing: () => void,
    setFormField: (string, any) => void,
    setFieldError: (string, string) => void,
    step: Step,
}

const getDisplayName = (WrappedComponent: React.ComponentType<any>) => (
    WrappedComponent.displayName || WrappedComponent.name || 'Component'
)

const withAuthFlow = (WrappedComponent: React.ComponentType<any>, step: Step, initialFormFields: FormFields) => {
    class WithAuthFlow extends React.Component<{}, State> {
        static displayName = `WithAuthFlow(${getDisplayName(WrappedComponent)})`

        panel: ?AuthPanel

        state = {
            step,
            isProcessing: false,
            form: initialFormFields,
            errors: {},
        }

        setFieldError = (field: string, message: string) => {
            const errors = {
                ...this.state.errors,
                [field]: message,
            }
            if (!message) {
                delete errors[field]
            }
            this.setState({
                errors,
            })
        }

        setFormField = (field: string, value: any) => {
            this.setFieldError(field, '')
            this.setState({
                form: {
                    ...this.state.form,
                    [field]: value,
                },
            })
        }

        setIsProcessing = (isProcessing: boolean) => {
            this.setState({
                isProcessing,
            })
        }

        setStep = (step: number) => {
            this.setState({
                step,
            })
        }

        prev = () => (
            this.setStep(Math.max(0, this.state.step - 1))
        )

        next = () => (
            this.setStep(this.state.step + 1)
        )

        render = () => {
            const { step, isProcessing, errors, form } = this.state

            return (
                <WrappedComponent
                    next={this.next}
                    prev={this.prev}
                    setFormField={this.setFormField}
                    setFieldError={this.setFieldError}
                    setIsProcessing={this.setIsProcessing}
                    step={step}
                    isProcessing={isProcessing}
                    errors={errors}
                    form={form}
                />
            )
        }
    }

    return WithAuthFlow
}

export default withAuthFlow
