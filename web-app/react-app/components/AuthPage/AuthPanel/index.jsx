// @flow

import * as React from 'react'
import { Link } from 'react-router-dom'
import cx from 'classnames'

import createLink from '../../../utils/createLink'

import styles from './authPanel.pcss'

type Props = {
    title: string,
    children: React.Node,
    onGoBack?: () => void,
}

class AuthPanel extends React.Component<Props> {
    onBackClick = (e: SyntheticInputEvent<EventTarget>) => {
        const onGoBack = this.props.onGoBack || (() => {})
        e.preventDefault()
        onGoBack()
    }

    render = () => {
        const { title, children, onGoBack } = this.props

        return (
            <div className={cx(styles.authPanel)}>
                <a href="https://www.streamr.com" className={styles.logo}>
                    <img src={createLink('static/images/streamr-logo.svg')} />
                </a>
                <div
                    className={cx(styles.navbar)}
                >
                    {onGoBack ? (
                        <a href="#" onClick={this.onBackClick}>
                            Back
                        </a>
                    ) : (
                        <React.Fragment>
                            <div />
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
                <div className={styles.footer}>
                    Made with ❤️ & ☕️ by Streamr Network AG in 2018
                </div>
            </div>
        )
    }
}

export default AuthPanel
