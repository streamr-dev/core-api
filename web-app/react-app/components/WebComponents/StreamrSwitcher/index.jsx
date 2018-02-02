// @flow

import React, {Component} from 'react'
import StreamrInput from '../StreamrInput'

import styles from './streamrSwitcher.pcss'
import type {StreamId, SubscriptionOptions} from '../../../flowtype/streamr-client-types'

type State = {
    value: boolean
}

type Props = {
    url: string,
    subscriptionOptions?: SubscriptionOptions,
    stream?: StreamId,
    height?: ?number,
    width?: ?number,
    onError?: ?Function
}

export default class StreamrSwitcher extends Component<Props, State> {
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