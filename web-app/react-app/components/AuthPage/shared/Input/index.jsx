// @flow

import React from 'react'
import cx from 'classnames'

import styles from './input.pcss'

type Props = {
    label?: string,
    error?: string,
    className?: string,
    processing?: boolean,
    value: string,
    onChange: (SyntheticInputEvent<EventTarget>) => void,
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

    render = () => {
        const { label, error, processing, value, onChange, ...props } = this.props
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
                {!!label && <label>{label}</label>}
                <div className={styles.wrapper}>
                    <input
                        {...props}
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
