// @flow

import React, {Component} from 'react'
import StreamrWidget from '../StreamrWidget'

import type {StreamId} from '../../../flowtype/streamr-client-types'

import styles from './streamr-label.pcss'

import type {AnyReactElement} from 'react-flow-types'

export default class StreamrLabel extends Component {
    widget: AnyReactElement
    onMessage: Function
    state: {
        value: number | string
    }
    props: {
        url: string,
        stream?: StreamId,
        onError?: Function,
        width: ?number,
        height: ?number,
        style: {}
    }
    static defaultProps = {
        style: {}
    }
    constructor() {
        super()
        
        this.state = {
            value: ''
        }
        this.onMessage = this.onMessage.bind(this)
    }
    onMessage({value}: { value: number | string }) {
        if (this.widget) {
            this.setState({
                value
            })
        }
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
                ref={w => this.widget = w}
            >
                <span className={styles.label} style={{
                    ...this.props.style
                }}>
                    {this.state.value}
                </span>
            </StreamrWidget>
        )
    }
}