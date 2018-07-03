// @flow

import React from 'react'
import createLink from '../../../utils/createLink'

import styles from './authPageNav.pcss'

const AuthPageNav = () => (
    <nav className={styles.authPageNav}>
        <a className={styles.logoContainer} href="https://www.streamr.com">
            <img src={createLink('static/images/streamr-logo.svg')} className={styles.logo} />
        </a>
    </nav>
)

export default AuthPageNav
