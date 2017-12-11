// @flow

import React, {Component} from 'react'
import StreamrWidget from '../StreamrWidget'
import _ from 'lodash'

import styles from './complexStreamrWidget.pcss'

import type {ModuleOptions, StreamId} from '../../../flowtype/streamr-client-types'
//import type {WebcomponentProps} from '../../../flowtype/webcomponent-types'

// TODO: Why just importing WebcomponentProps does not work?

type Props = {
    url: string,
    stream?: StreamId,
    height: ?number,
    width: ?number,
    onError: ?Function
} & {
    renderWidget: (HTMLDivElement, ModuleOptions) => void,
    onMessage: (any) => void
}

type State = {
    options: ModuleOptions
}

export default class ComplexStreamrWidget extends Component<Props, State> {
    root: ?HTMLDivElement
    widget: ?StreamrWidget
    map: ?any
 
    state = {
        options: {}
    }

    onModuleJson = ({options}: { options: ModuleOptions }) => {
        const opt = _.mapValues(options, 'value')
        if (this.root) {
            this.setState({
                options: opt
            })
            this.props.renderWidget(this.root, opt)
        }
    }
    
    componentWillReceiveProps(newProps: Props) {
        const changed = (key) => newProps[key] != undefined && newProps[key] !== this.props[key]
        
        if (changed('width') || changed('height')) {
            this.map && this.map.redraw()
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
                <div ref={root => this.root = root} className={styles.root}/>
            </StreamrWidget>
        )
    }
}