// @flow

import React from 'react'
import classnames from 'classnames'

import styles from './authPageFooter.pcss'

const AuthPageFooter = () => (
    <footer className={classnames(styles.authPageFooter, 'ff-plex-mono', 'uppercase', 'fw-semibold')}>
        Made with ❤️ & ☕️ by Streamr Network AG in 2018
    </footer>
)

export default AuthPageFooter
