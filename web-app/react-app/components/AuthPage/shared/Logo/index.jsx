// @flow

import React from 'react'
import cx from 'classnames'

import createLink from '../../../../utils/createLink'
import styles from './logo.pcss'

type Props = {
    className?: string,
}

const Logo = ({ className }: Props) => (
    <div className={cx(className, styles.root)}>
        <a href="https://www.streamr.com" className={styles.link}>
            <img src={createLink('static/images/streamr-logo.svg')} alt="Streamr logo" />
        </a>
    </div>
)

export default Logo
