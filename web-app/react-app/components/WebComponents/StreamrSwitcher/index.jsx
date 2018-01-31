// @flow

import React, {Component} from 'react'
import StreamrInput from '../StreamrInput'

import styles from './streamrSwitcher.pcss'

import type {WebcomponentProps} from '../../../flowtype/webcomponent-types'

type State = {
    value: boolean
}

export default class StreamrSwitcher extends Component<WebcomponentProps, State> {
    input: ?StreamrInput
    state = {
        value: false
    }
    
    onModuleJson = ({switcherValue}: {switcherValue: boolean}) => {
        if (this.input) {
            this.setState({
                value: switcherValue
            })
        }
    }
    
    onClick = () => {
        const newValue = !this.state.value
        this.setState({
            value: newValue
        })
        this.input && this.input.sendValue(newValue)
    }
    
    render() {
        return (
            <StreamrInput
                {...this.props}
                onModuleJson={this.onModuleJson}
                ref={(input) => this.input = input}
            >
                <div className={styles.streamrSwitcher}>
                    <div className={`${styles.switcher} ${this.state.value ? styles.on : styles.off}`} onClick={this.onClick}>
                        <div className={styles.switcherInner} />
                    </div>
                </div>
            </StreamrInput>
        )
    }
}