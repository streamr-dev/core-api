// @flow

import React from 'react'
import createLink from '../../../../utils/createLink'

import styles from './logo.pcss'

const Logo = () => (
    <a href="https://www.streamr.com" className={styles.root}>
        <img src={createLink('static/images/streamr-logo.svg')} alt="Streamr logo" />
    </a>
)

export default Logo
