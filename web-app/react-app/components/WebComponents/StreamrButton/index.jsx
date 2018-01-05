// @flow

import React, {Component} from 'react'
import {Button} from 'react-bootstrap'
import StreamrInput from '../StreamrInput'

import type {WebcomponentProps as Props} from '../../../flowtype/webcomponent-types'

type State = {
    name: string
}

export default class StreamrButton extends Component<Props, State> {
    widget: ?StreamrInput
    state = {
        name: 'Button'
    }
    
    onMessage = ({state: buttonName}: { state?: string }) => {
        if (this.widget) {
            if (buttonName) {
                this.setState({
                    name: buttonName
                })
            }
        }
    }
    
    onClick = () => {
        this.widget && this.widget.sendValue()
    }
    
    render() {
        return (
            <StreamrInput
                {...this.props}
                onMessage={this.onMessage}
                ref={(widget) => this.widget = widget}
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