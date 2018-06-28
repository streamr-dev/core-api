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

type State = {
    height: string | number,
}

class AuthPanel extends React.Component<Props, State> {
    static styles = styles

    state = {
        height: 'auto',
    }

    setHeight = (height: number) => {
        this.setState({
            height,
        })
    }

    render = () => {
        const { children, onBack, currentStep, onProceed } = this.props
        const { height } = this.state

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
                        <div
                            className={styles.inner}
                            style={{
                                height,
                            }}
                        >
                            {React.Children.map(children, (child, index) => React.cloneElement(child, {
                                active: index === currentStep,
                                onHeightChange: this.setHeight,
                            }))}
                        </div>
                    </div>
                </div>
            </form>
        )
    }
}

export default AuthPanel
