// @flow

import React, {Component} from 'react'
import StreamrWidget from '../StreamrWidget'

import styles from './streamr-label.pcss'

import type {StreamId} from '../../../flowtype/streamr-client-types'
//import type {WebcomponentProps} from '../../../flowtype/webcomponent-types'

// TODO: Why just importing WebcomponentProps does not work?

type Props = {
    url: string,
    stream?: StreamId,
    height: ?number,
    width: ?number,
    onError: ?Function
} & {
    style: {}
}

type State = {
    value: number | string
}

export default class StreamrLabel extends Component<Props, State> {
    widget: ?StreamrWidget
    static defaultProps = {
        style: {}
    }
    state = {
        value: ''
    }
    onMessage = ({value}: { value: number | string }) => {
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
                <span
                    className={styles.label}
                    style={{
                        ...this.props.style
                    }}
                >
                    {this.state.value}
                </span>
            </StreamrWidget>
        )
    }
}