// @flow

import React, {Component} from 'react'
import {Button} from 'react-bootstrap'
import StreamrInput from '../StreamrInput'

import type {StreamId, SubscriptionOptions} from '../../../flowtype/streamr-client-types'

type Props = {
    url: string,
    subscriptionOptions?: SubscriptionOptions,
    stream?: StreamId,
    height?: ?number,
    width?: ?number,
    onError?: ?Function
}

type State = {
    name: string
}

export default class StreamrButton extends Component<Props, State> {
    input: ?StreamrInput
    state = {
        name: 'Button'
    }
    
    onMessage = ({state: buttonName}: { state: string }) => {
        if (this.input) {
            if (buttonName) {
                this.setState({
                    name: buttonName
                })
            }
        }
    }
    
    onClick = () => {
        this.input && this.input.sendValue()
    }
    
    assignInputRef = (widget: ?StreamrInput) => {
        this.input = widget
    }
    
    render() {
        return (
            <StreamrInput
                {...this.props}
                onMessage={this.onMessage}
                ref={this.assignInputRef}
            >
                <Button
                    onClick={this.onClick}
                    bsStyle="primary"
                    bsSize="lg"
                >
                    {this.state.name}
                </Button>
            </StreamrInput>
        )
    }
}