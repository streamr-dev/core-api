// @flow

import * as React from 'react'
import cx from 'classnames'
import zxcvbn from 'zxcvbn'

import styles from './input.pcss'
import Label from '../Label'

type Props = {
    type?: string,
    label: string,
    error?: string,
    className?: string,
    processing?: boolean,
    value: string,
    onChange: (SyntheticInputEvent<EventTarget>) => void,
    meastureStrength?: boolean,
}

type State = {
    focused: boolean,
    autoFilled: boolean,
}

class Input extends React.Component<Props, State> {
    state = {
        focused: false,
        autoFilled: false,
    }

    onFocusChange = ({ type }: SyntheticInputEvent<EventTarget>) => {
        this.setState({
            focused: type === 'focus',
        })
    }

    onAnimationStart = ({ animationName }: SyntheticAnimationEvent<EventTarget>) => {
        if (animationName === styles.onAutoFillStart || animationName === styles.onAutoFillCancel) {
            this.setState({
                autoFilled: animationName === styles.onAutoFillStart,
            })
        }
    }

    strengthLevel = () => {
        const { value, type, meastureStrength } = this.props

        if (type !== 'password' || !meastureStrength || !value) {
            return -1
        }

        return zxcvbn(value).score
    }

    render = () => {
        const { label, error, processing, value, onChange, type, meastureStrength, ...props } = this.props
        const { focused, autoFilled } = this.state

        return (
            <div
                className={cx(styles.root, {
                    [styles.withError]: !!error && !processing,
                    [styles.focused]: !!focused,
                    [styles.processing]: !!processing,
                    [styles.filled]: !!(value || autoFilled),
                })}
            >
                <Label value={label} strengthLevel={this.strengthLevel()} />
                <div className={styles.wrapper}>
                    <input
                        {...props}
                        type={type}
                        className={styles.input}
                        value={value}
                        onChange={onChange}
                        onAnimationStart={this.onAnimationStart}
                        onFocus={this.onFocusChange}
                        onBlur={this.onFocusChange}
                    />
                </div>
                {!!error && !processing && (
                    <div className={styles.error}>{error}</div>
                )}
            </div>
        )
    }
}

export default Input
