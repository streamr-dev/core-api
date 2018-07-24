// @flow

import * as React from 'react'
import cx from 'classnames'

import styles from './checkbox.pcss'
import InputError from '../FormControl/InputError'

export type Props = {
    className?: string,
    checked?: boolean,
    children: React.Node,
    error?: string,
}

const Checkbox = ({ checked, className, children, error, ...props }: Props) => (
    <div className={styles.root}>
        <label className={styles.label}>
            <input
                {...props}
                type="checkbox"
                checked={!!checked}
                className={cx(styles.checkbox, className)}
            />
            <span>{children}</span>
        </label>
        <InputError error={error} />
    </div>
)

export default Checkbox
