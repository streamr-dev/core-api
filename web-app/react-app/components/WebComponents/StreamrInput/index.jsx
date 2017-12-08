// @flow

import React, {Component} from 'react'
import StreamrWidget from '../StreamrWidget'

import type {SubscriptionOptions, StreamId} from '../../../flowtype/streamr-client-types'
import type {ReactChildren} from 'react-flow-types'

export default class StreamrInput extends Component {
    widget: StreamrWidget
    alreadyAsked: ?boolean
    props: {
        stream: StreamId,
        subscriptionOptions: SubscriptionOptions,
        url: string,
        onError: Function,
        onMessage: Function,
        onModuleJson: Function,
        children: ReactChildren,
        widgetRef?: Function
    }
    constructor() {
        super()
    }
    
    render() {
        return (
            <StreamrWidget
                subscriptionOptions={{
                    stream: this.props.stream,
                    resend_last: 1
                }}
                url={this.props.url}
                onError={this.props.onError}
                onMessage={this.props.onMessage}
                onmodulejson={this.props.onModuleJson}
                ref={(widget) => {
                    this.widget = widget
                    this.props.widgetRef && this.props.widgetRef(widget)
                }}
            >
                {React.Children.only(this.props.children)}
            </StreamrWidget>
        )
    }
}