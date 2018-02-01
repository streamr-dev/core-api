// @flow

import React, {Component} from 'react'
import StreamrWidget from '../StreamrWidget'
import _ from 'lodash'

import styles from './complexStreamrWidget.pcss'

import type {ModuleOptions, StreamId, SubscriptionOptions} from '../../../flowtype/streamr-client-types'

type Props = {
    url: string,
    subscriptionOptions?: SubscriptionOptions,
    stream?: StreamId,
    height?: ?number,
    width?: ?number,
    onError?: ?Function,
    renderWidget: (root: ?HTMLDivElement, ModuleOptions) => void,
    onMessage: (any) => void,
    className: string,
    onResize?: (width: ?number, height: ?number) => void
}

type State = {
    options: ModuleOptions
}

export default class ComplexStreamrWidget extends Component<Props, State> {
    root: ?HTMLDivElement
    widget: ?StreamrWidget
 
    state = {
        options: {}
    }
    
    static defaultProps = {
        className: ''
    }

    onModuleJson = ({options}: { options: ModuleOptions }) => {
        const opt = _.mapValues(options, 'value')
        if (this.root) {
            this.setState({
                options: {
                    ...this.state.options,
                    ...(opt || {})
                }
            })
            this.props.renderWidget(this.root, opt)
        }
    }
    
    componentWillReceiveProps(newProps: Props) {
        const changed = (key) => newProps[key] != undefined && newProps[key] !== this.props[key]
        
        if (changed('width') || changed('height')) {
            this.props.onResize && this.props.onResize(newProps['width'], newProps['height'])
        }
    }
    
    render() {
        return (
            <StreamrWidget
                subscriptionOptions={{
                    stream: this.props.stream
                }}
                onModuleJson={this.onModuleJson}
                url={this.props.url}
                onMessage={this.props.onMessage}
                onError={this.props.onError}
                ref={(w) => this.widget = w}
            >
                <div
                    ref={root => this.root = root}
                    className={`${styles.root} ${this.props.className}`}
                />
            </StreamrWidget>
        )
    }
}
