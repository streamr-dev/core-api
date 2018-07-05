// @flow

import * as React from 'react'
import { Schema } from 'yup'

import AuthPanelNav from '../AuthPanelNav'
import Switch from '../Switch'
import styles from './authPanel.pcss'
import type {
    FormFields,
    FlagSetter,
    FieldErrorSetter,
} from '../types'

export {
    styles,
}

type Props = {
    form: FormFields,
    children: React.Node,
    currentStep: number,
    onPrev: () => void,
    onNext: () => void,
    setIsProcessing: FlagSetter,
    validationSchemas: Array<Schema>,
    onValidationError: FieldErrorSetter,
}

class AuthPanel extends React.Component<Props> {
    render = () => {
        const { children, onPrev, currentStep, validationSchemas, onValidationError, setIsProcessing, onNext: next, form } = this.props
        const totalSteps = React.Children.count(children)

        return (
            <div className={styles.authPanel}>
                <Switch current={currentStep}>
                    {React.Children.map(children, (child) => (
                        <AuthPanelNav
                            signin={child.props.showSignin}
                            signup={child.props.showSignup}
                            onUseEth={child.props.showEth ? (() => {}) : null}
                            onGoBack={child.props.showBack ? onPrev : null}
                        />
                    ))}
                </Switch>
                <div className={styles.panel}>
                    <div className={styles.header}>
                        <Switch current={currentStep}>
                            {React.Children.map(children, (child) => (
                                <span>{child.props.title || 'Title'}</span>
                            ))}
                        </Switch>
                    </div>
                    <div className={styles.body}>
                        <Switch current={currentStep}>
                            {React.Children.map(children, (child, index) => React.cloneElement(child, {
                                validationSchema: validationSchemas[index],
                                step: index,
                                totalSteps,
                                onValidationError,
                                setIsProcessing,
                                next,
                                form,
                            }))}
                        </Switch>
                    </div>
                </div>
            </div>
        )
    }
}

export default AuthPanel
