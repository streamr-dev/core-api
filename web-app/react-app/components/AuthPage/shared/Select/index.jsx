// @flow

import React from 'react'
import ReactSelect from 'react-select'
import classnames from 'classnames'

import styles from './select.pcss'

type Props = {
    placeholder: string,
    className?: ?string,
    onFocus?: (Event) => void,
    onBlur?: (Event) => void,
    [string]: any,
}

type State = {
    focused: boolean,
}

class Select extends React.Component<Props, State> {
    static defaultProps = {
        placeholder: 'Select...'
    }

    state = {
        focused: false,
    }

    onFocus() {
        this.setState({
            focused: true,
        })
    }

    onBlur() {
        this.setState({
            focused: false,
        })
    }

    render() {
        const { className, placeholder, onFocus, onBlur, ...props } = this.props
        return (
            <div className={classnames(styles.selectContainer, {
                [styles.focused]: this.state.focused,
            })}>
                <label>
                    {placeholder}
                </label>
                <ReactSelect
                    {...props}
                    onFocus={(e) => {
                        onFocus && onFocus(e)
                        this.onFocus()
                    }}
                    onBlur={(e) => {
                        onBlur && onBlur(e)
                        this.onBlur()
                    }}
                    className={classnames(styles.select, className)}
                />
            </div>
        )
    }
}

export default Select
