// @flow

/* eslint no-unused-vars: ["error", { "ignoreRestSiblings": true }] */

import * as React from 'react'
import cx from 'classnames'
import zxcvbn from 'zxcvbn'

import styles from './input.pcss'
import Label from '../Label'
import TextField from '../TextField'

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
    autoCompleted: boolean,
}

class Input extends React.Component<Props, State> {
    state = {
        focused: false,
        autoCompleted: false,
    }

    onFocusChange = (focused: boolean) => {
        this.setState({
            focused,
        })
    }

    onAutoComplete = (autoCompleted: boolean) => {
        this.setState({
            autoCompleted,
        })
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
        const { focused, autoCompleted } = this.state

        return (
            <div
                className={cx(styles.root, {
                    [styles.withError]: !!error && !processing,
                    [styles.focused]: !!focused,
                    [styles.processing]: !!processing,
                    [styles.filled]: !!(value || autoCompleted),
                })}
            >
                <Label value={label} strengthLevel={this.strengthLevel()} />
                <div className={styles.wrapper}>
                    <TextField
                        {...props}
                        type={type}
                        value={value}
                        onChange={onChange}
                        onAutoComplete={this.onAutoComplete}
                        onFocusChange={this.onFocusChange}
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
