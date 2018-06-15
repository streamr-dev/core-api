// @flow

import * as React from 'react'
import { Link } from 'react-router-dom'
import cx from 'classnames'

import styles from './authPanel.pcss'

type Props = {
    title: string,
    children: React.Node,
    onGoBack?: () => void,
    ethLink?: boolean,
    signupLink?: boolean,
    signinLink?: boolean,
}

class AuthPanel extends React.Component<Props> {
    onBackClick = (e: SyntheticInputEvent<EventTarget>) => {
        const onGoBack = this.props.onGoBack || (() => {})
        e.preventDefault()
        onGoBack()
    }

    render = () => {
        const { title, children, onGoBack, ethLink, signupLink, signinLink } = this.props

        return (
            <div className={cx(styles.authPanel)}>
                <div
                    className={cx(styles.navbar)}
                >
                    {onGoBack ? (
                        <React.Fragment>
                            <a href="#" onClick={this.onBackClick}>
                                Back
                            </a>
                            <span />
                        </React.Fragment>
                    ) : (
                        <React.Fragment>
                            {ethLink ? (
                                <a href="#">
                                    Sign in with Ethereum
                                </a>
                            ) : (
                                <span />
                            )}
                            <span>
                                {signupLink && (
                                    <Link to="/register/signup">
                                        Sign up
                                    </Link>
                                )}
                                {signinLink && (
                                    <Link to="/login/auth">
                                        Sign in
                                    </Link>
                                )}
                            </span>
                        </React.Fragment>
                    )}
                </div>
                <div className={styles.panel}>
                    <div className={styles.header}>
                        {title}
                    </div>
                    <div className={styles.body}>
                        {children}
                    </div>
                </div>
            </div>
        )
    }
}

export default AuthPanel
