// @flow

import * as React from 'react'
import { Link } from 'react-router-dom'
import cx from 'classnames'

import styles from './authPanelNav.pcss'

type Props = {
    active?: boolean,
    onGoBack?: ?() => void,
    onUseEth?: ?() => void,
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
        const { active, onGoBack, onUseEth, signin, signup } = this.props

        return (
            <div
                className={cx(styles.root, {
                    [styles.active]: !!active,
                })}
            >
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
