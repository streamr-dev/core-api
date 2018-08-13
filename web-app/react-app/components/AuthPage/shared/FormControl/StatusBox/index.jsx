// @flow

import * as React from 'react'
import cx from 'classnames'

import styles from './statusBox.pcss'

type Props = {
    className?: string,
    caution?: boolean,
    error?: boolean,
    processing?: boolean,
    success?: boolean,
    active?: boolean,
    children: React.Node,
}

const StatusBox = ({ children, caution, error, processing, success, active, className }: Props) => (
    <div
        className={cx(styles.root, className, {
            [styles.caution]: !!caution,
            [styles.error]: !!error,
            [styles.processing]: !!processing,
            [styles.success]: !!success,
            [styles.active]: !!active,
        })}
    >
        {children}
    </div>
)

export default StatusBox
