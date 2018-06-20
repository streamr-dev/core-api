// @flow

/* eslint no-unused-vars: ["error", { "ignoreRestSiblings": true }] */

import * as React from 'react'
import cx from 'classnames'
import styles from './button.pcss'

type Props = {
    className?: string,
    proceed?: boolean,
}

const Button = ({ className, proceed, ...props }: Props) => (
    <button
        type="button"
        {...props}
        className={cx(className, styles.button)}
    />
)

export default Button
