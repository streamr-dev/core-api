// @flow

import * as React from 'react'
import cx from 'classnames'
import styles from './button.pcss'

type Props = {
    className?: string,
}

const Button = ({ className, ...props }: Props) => (
    <button
        type="submit"
        {...props}
        className={cx(className, styles.button)}
    />
)

export default Button
