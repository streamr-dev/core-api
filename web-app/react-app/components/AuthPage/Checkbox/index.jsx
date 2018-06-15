// @flow

import * as React from 'react'
import cx from 'classnames'

import styles from './checkbox.pcss'

export type Props = {
    className?: string,
    value?: boolean,
    children: React.Node,
}

const Checkbox = ({ value, className, children, ...props }: Props) => (
    <label className={styles.root}>
        <input
            {...props}
            type="checkbox"
            checked={!!value}
            className={cx(styles.checkbox, className)}
        />
        <span>{children}</span>
    </label>
)

export default Checkbox
