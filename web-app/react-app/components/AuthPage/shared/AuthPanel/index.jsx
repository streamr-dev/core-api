// @flow

import * as React from 'react'

import AuthPanelNav from '../AuthPanelNav'
import Switch from '../Switch'
import styles from './authPanel.pcss'

type Props = {
    children: React.Node,
    onBack?: () => void,
    currentStep: number,
    onProceed?: (SyntheticEvent<EventTarget>) => void,
}

class AuthPanel extends React.Component<Props> {
    static styles = styles

    render = () => {
        const { children, onBack, currentStep, onProceed } = this.props

        return (
            <form className={styles.authPanel} onSubmit={onProceed}>
                <Switch current={currentStep}>
                    {React.Children.map(children, (child) => (
                        <AuthPanelNav
                            signin={child.props.showSignin}
                            signup={child.props.showSignup}
                            onUseEth={child.props.showEth ? (() => {}) : null}
                            onGoBack={child.props.showBack ? onBack : null}
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
                            {children}
                        </Switch>
                    </div>
                </div>
            </form>
        )
    }
}

export default AuthPanel
