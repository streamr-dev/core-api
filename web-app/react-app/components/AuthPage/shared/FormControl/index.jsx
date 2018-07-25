// @flow

/* eslint no-unused-vars: ["error", { "ignoreRestSiblings": true }] */

import * as React from 'react'
import cx from 'classnames'
import zxcvbn from 'zxcvbn'

import Switch from '../Switch'
import StatusBox from './StatusBox'
import InputError from './InputError'
import styles from './formControl.pcss'
import { getDisplayName } from '../utils'
import type { ValueFormatter, FieldSetter } from '../types'

type Props = {
    error?: string,
    label: string,
    measureStrength?: boolean,
    name: string,
    onChange?: FieldSetter,
    processing?: boolean,
    type?: string,
    value: string,
}

type State = {
    focused: boolean,
    autoCompleted: boolean,
    lastKnownError: string,
}

const formControl = (WrappedComponent: React.ComponentType<any>, valueFormatter?: ValueFormatter<any>) => (
    class FormControl extends React.Component<Props, State> {
        static displayName = `FormControl(${getDisplayName(WrappedComponent)})`

        state = {
            focused: false,
            autoCompleted: false,
            lastKnownError: '',
        }

        setAutoCompleted = (autoCompleted: boolean) => {
            this.setState({
                autoCompleted,
            })
        }

        componentDidUpdate = (prevProps: Props) => {
            const { error } = this.props

            if (error && prevProps.error !== error) {
                this.setState({
                    lastKnownError: error,
                })
            }
        }

        strengthLevel = () => {
            const { value, type, measureStrength } = this.props

            if (type !== 'password' || !measureStrength || !value) {
                return -1
            }

            return [0, 1, 1, 2, 2][zxcvbn(value).score]
        }

        onChange = (payload: any) => {
            const { onChange, name } = this.props
            const formatter = valueFormatter || ((obj) => obj)

            if (onChange) {
                onChange(name, formatter(payload))
            }
        }

        onFocusChange = ({ type }: SyntheticEvent<EventTarget>) => {
            this.setState({
                focused: type === 'focus',
            })
        }

        render() {
            const { processing, error, value, label, measureStrength, ...props } = this.props
            const { lastKnownError, focused, autoCompleted } = this.state
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
                        <WrappedComponent
                            {...props}
                            value={value}
                            onChange={this.onChange}
                            onBlur={this.onFocusChange}
                            onFocus={this.onFocusChange}
                            onAutoComplete={this.setAutoCompleted}
                        />
                    </StatusBox>
                    <InputError error={(!processing && !!error) ? lastKnownError : null} />
                </div>
            )
        }
    }
)

export default formControl
