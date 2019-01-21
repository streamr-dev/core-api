// @flow

import React from 'react'
import cx from 'classnames'

import styles from './footer.pcss'

type Props = {
    className?: string,
}

const Footer = ({ className }: Props) => (
    <div className={cx(className, styles.root)}>
        <div className={styles.inner}>
            Made with ❤️ & ☕️ by Streamr Contributors
        </div>
    </div>
)

export default Footer
