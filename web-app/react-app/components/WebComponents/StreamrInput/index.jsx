// @flow

import React, {Component} from 'react'
import StreamrWidget from '../StreamrWidget'

import type {Node} from 'react'
import type {StreamId, SubscriptionOptions} from '../../../flowtype/streamr-client-types'

type Props = {
    url: string,
    subscriptionOptions?: SubscriptionOptions,
    stream?: StreamId,
    height?: ?number,
    width?: ?number,
    onError?: ?Function,
    onMessage?: ?({state: string}) => void,
    onModuleJson?: ?Function,
    children: Node,
    widgetRef?: (widget: ?StreamrWidget) => void
}

export default class StreamrInput extends Component<Props> {
    widget: ?StreamrWidget
    
    componentDidMount = () => {
        this.widget && this.widget.sendRequest({
            type: 'getState'
        })
            .then(({data}) => this.props.onMessage && this.props.onMessage(data))
    }
    
    sendValue = (value: ?any) => {
        this.widget && this.widget.sendRequest({
            type: 'uiEvent',
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
                onError={this.props.onError}
                onMessage={this.props.onMessage}
                onModuleJson={this.props.onModuleJson}
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