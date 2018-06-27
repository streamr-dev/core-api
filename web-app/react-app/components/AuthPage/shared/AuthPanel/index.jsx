// @flow

import * as React from 'react'

import AuthPanelNav from '../AuthPanelNav'
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

    titles = () => React.Children.map(this.props.children, (child) => child.props.title || 'Title')

    render = () => {
        const { children, onBack, currentStep, onProceed } = this.props
        const { height } = this.state

        return (
            <form className={styles.authPanel} onSubmit={onProceed}>
                <div className={styles.navs}>
                    {React.Children.map(children, (child, index) => (
                        <AuthPanelNav
                            active={index === currentStep}
                            signin={child.props.showSignin}
                            signup={child.props.showSignup}
                            onUseEth={child.props.showEth ? (() => {}) : null}
                            onGoBack={child.props.showBack ? onBack : null}
                        />
                    ))}
                </div>
                <div className={styles.panel}>
                    <div className={styles.header}>
                        {this.titles()[currentStep]}
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
