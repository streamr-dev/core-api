// @flow

import * as React from 'react'
import * as yup from 'yup'

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
    processing: boolean,
    step: Step,
}

export type AuthFlowProps = {
    attach: (?AuthPanel) => void,
    errors: {
        [string]: string,
    },
    form: FormFields,
    next: (Array<yup.Schema>) => void,
    prev: () => void,
    processing: boolean,
    setFormField: (string, any) => void,
    step: Step,
}

const getDisplayName = (WrappedComponent: React.ComponentType<any>) => WrappedComponent.displayName || WrappedComponent.name || 'Component'

const withAuthFlow = (WrappedComponent: React.ComponentType<any>, step: Step, initialFormFields: FormFields) => {
    class WithAuthFlow extends React.Component<{}, State> {
        static displayName = `WithAuthFlow(${getDisplayName(WrappedComponent)})`

        panel: ?AuthPanel

        state = {
            step,
            processing: false,
            form: initialFormFields,
            errors: {},
        }

        setFormField = (field: string, value: any) => {
            const { form, errors: prevErrors } = this.state
            const errors = {
                ...prevErrors,
            }

            delete errors[field]

            this.setState({
                form: {
                    ...form,
                    [field]: value,
                },
                errors,
            })
        }

        validate = (schema: ?yup.Schema): Promise<any> => (schema || yup.object()).validate(this.state.form)

        next = (schemas: Array<yup.Schema>) => {
            const { step, errors } = this.state

            this.setState({
                processing: true,
            }, () => {
                this.validate(schemas[step])
                    .then(() => {
                        this.setState({
                            processing: false,
                            step: Math.min(this.numSteps(), step + 1),
                        })
                    }, (error: yup.ValidationError) => {
                        this.setState({
                            processing: false,
                            errors: {
                                ...errors,
                                [error.path]: error.message,
                            },
                        })
                    })
            })
        }

        prev = () => {
            this.setState({
                step: Math.max(0, this.state.step - 1),
            })
        }

        attach = (panel: ?AuthPanel) => {
            this.panel = panel
        }

        numSteps = () => (this.panel ? React.Children.count(this.panel.props.children) : 0)

        render = () => {
            const { step, processing, errors, form } = this.state

            return (
                <WrappedComponent
                    {...this.props}
                    attach={this.attach}
                    next={this.next}
                    prev={this.prev}
                    setFormField={this.setFormField}
                    step={step}
                    processing={processing}
                    errors={errors}
                    form={form}
                />
            )
        }
    }

    return WithAuthFlow
}

export default withAuthFlow
