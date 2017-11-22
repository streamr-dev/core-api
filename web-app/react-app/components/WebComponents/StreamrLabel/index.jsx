// @flow

import React, {Component} from 'react'
import StreamrWidget from '../StreamrWidget'

import type {StreamId} from '../../../flowtype/streamr-client-types'

import styles from './streamr-label.pcss'

export default class StreamrLabel extends Component {
    onMessage: Function
    state: {
        value: number | string
    }
    props: {
        url: string,
        stream?: StreamId,
        onError?: Function,
        fontSize?: number
    }
    constructor() {
        super()
        
        this.state = {
            value: ''
        }
        this.onMessage = this.onMessage.bind(this)
    }
    onMessage({value}: { value: number | string }) {
        this.setState({
            value
        })
    }
    render() {
        return (
            <StreamrWidget
                subscriptionOptions={{
                    stream: this.props.stream,
                    resend_last: 1
                }}
                url={this.props.url}
                onMessage={this.onMessage}
                onError={this.props.onError}
            >
                <span className={styles.label} style={{
                    fontSize: this.props.fontSize
                }}>
                    {this.state.value}
                </span>
            </StreamrWidget>
        )
    }
}