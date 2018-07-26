// @flow

import * as React from 'react'
import { Option as ReactSelectOption } from 'react-select'

import styles from './selectOption.pcss'

type Props = {
    children: React.Node,
    isSelected?: boolean,
}

class SelectOption extends React.Component<Props> {
    render() {
        const { children, isSelected, ...props } = this.props

        return (
            <ReactSelectOption
                {...props}
                isSelected={isSelected}
            >
                <div className={styles.root}>
                    <div className={styles.icon}>
                        {!!isSelected && (
                            <svg viewBox="0 0 10 8" xmlns="http://www.w3.org/2000/svg">
                                <path
                                    d="M1.83 3.885A1 1 0 1 0 .359 5.24l2.2 2.389a1 1 0 0 0 1.443.03l5.657-5.657A1 1 0 0 0 8.244.587l-4.92 4.92L1.83 3.885z"
                                    fill="#323232"
                                    fillRule="nonzero"
                                />
                            </svg>
                        )}
                    </div>
                    <div className={styles.label}>
                        {children}
                    </div>
                </div>
            </ReactSelectOption>
        )
    }
}

export default SelectOption
