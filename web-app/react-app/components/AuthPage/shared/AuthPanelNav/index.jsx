// @flow

import * as React from 'react'
import { Link } from 'react-router-dom'
import styles from './authPanelNav.pcss'

type Props = {
    onGoBack: () => void,
    onUseEth: () => void,
    signup?: boolean,
    signin?: boolean,
}

class AuthPanelNav extends React.Component<Props> {
    onClick = (callback?: () => void) => (e: SyntheticInputEvent<EventTarget>) => {
        const onClick = callback || (() => {})
        e.preventDefault()
        onClick()
    }

    render = () => {
        const { onGoBack, onUseEth, signin, signup } = this.props

        return (
            <div className={styles.root}>
                {onGoBack ? (
                    <React.Fragment>
                        <a href="#" onClick={this.onClick(onGoBack)}>
                            Back
                        </a>
                        <span />
                    </React.Fragment>
                ) : (
                    <React.Fragment>
                        {onUseEth ? (
                            <a href="#" onClick={this.onClick(onUseEth)}>
                                Sign in with Ethereum
                            </a>
                        ) : (
                            <span>&nbsp;</span>
                        )}
                        <span>
                            {signup && (
                                <Link to="/register/signup">
                                    Sign up
                                </Link>
                            )}
                            {signin && (
                                <Link to="/login/auth">
                                    Sign in
                                </Link>
                            )}
                        </span>
                    </React.Fragment>
                )}
            </div>
        )
    }
}

export default AuthPanelNav
