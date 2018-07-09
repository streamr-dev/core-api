// @flow

/* eslint no-unused-vars: ["error", { "ignoreRestSiblings": true }] */

import * as React from 'react'
import cx from 'classnames'
import zxcvbn from 'zxcvbn'

import styles from './input.pcss'
import TextField from '../TextField'
import StatusBox from '../StatusBox'
import Switch from '../Switch'
import InputError from '../InputError'

type Props = {
    type?: string,
    label: string,
    error?: string,
    className?: string,
    processing?: boolean,
    value: string,
    onChange: (SyntheticInputEvent<EventTarget>) => void,
    measureStrength?: boolean,
}

type State = {
    focused: boolean,
    autoCompleted: boolean,
    lastKnownError: string,
}

class Input extends React.Component<Props, State> {
    state = {
        focused: false,
        autoCompleted: false,
        lastKnownError: '',
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
        const { value, type, measureStrength } = this.props

        if (type !== 'password' || !measureStrength || !value) {
            return -1
        }

        return [0, 1, 1, 2, 2][zxcvbn(value).score]
    }

    componentDidUpdate = (prevProps: Props) => {
        const { error } = this.props

        if (error && prevProps.error !== error) {
            this.setState({
                lastKnownError: error,
            })
        }
    }

    render = () => {
        const { label, error, processing, value, onChange, type, measureStrength, ...props } = this.props
        const { focused, autoCompleted, lastKnownError } = this.state
        const strength = this.strengthLevel()

        return (
            <div
                className={cx(styles.root, {
                    [styles.withError]: !!error && !processing,
                    [styles.focused]: !!focused,
                    [styles.processing]: !!processing,
                    [styles.filled]: !!(value || autoCompleted),
                })}
            >
                <label>
                    <Switch current={strength + 1}>
                        <span>{label}</span>
                        <span className={styles.weak}>Password is weak</span>
                        <span className={styles.moderate}>Password is not strong</span>
                        <span className={styles.strong}>Password is quite strong</span>
                    </Switch>
                </label>
                <StatusBox
                    className={styles.statusBar}
                    processing={!!processing}
                    error={!!error || strength === 0}
                    caution={strength === 1}
                    success={strength === 2}
                >
                    <TextField
                        {...props}
                        type={type}
                        value={value}
                        onChange={onChange}
                        onAutoComplete={this.onAutoComplete}
                        onFocusChange={this.onFocusChange}
                    />
                </StatusBox>
                <InputError error={(!processing && !!error) ? lastKnownError : null} />
            </div>
        )
    }
}

export default Input
