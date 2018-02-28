// @flow

import React, {Component} from 'react'
import ComplexStreamrWidget from '../ComplexStreamrWidget'

declare var StreamrChart: Function

import type {ModuleOptions, StreamId, SubscriptionOptions} from '../../../flowtype/streamr-client-types'

import styles from './streamrChartComponent.pcss'

type Options = ModuleOptions | {
    rangeDropdown: boolean,
    showHideButtons: boolean,
    displayTitle: boolean,
    init: ?boolean
}

type Props = {
    url: string,
    subscriptionOptions?: SubscriptionOptions,
    stream?: StreamId,
    height?: ?number,
    width?: ?number,
    onError?: ?Function
}

type State = {
    options: Options
}

export default class StreamrChartComponent extends Component<Props, State> {
    chart: ?StreamrChart
    state = {
        options: {
            rangeDropdown: false,
            showHideButton: false,
        },
    }

    componentWillReceiveProps(newProps: Props) {
        const changed = (key) => newProps[key] != undefined && newProps[key] !== this.props[key]

        if (changed('width') || changed('height')) {
            this.chart && this.chart.redraw()
        }
    }

    renderWidget = (root: ?HTMLDivElement, options: Options) => {
        if (root) {
            this.chart = new StreamrChart(root, {
                ...this.state.options,
                ...options,
            })
        }
    }

    onMessage = (msg: {}) => {
        this.chart && this.chart.handleMessage(msg)
    }

    onResize = () => {
        this.chart && this.chart.resize()
    }

    render() {
        return (
            <ComplexStreamrWidget
                className={styles.streamrChartComponent}
                stream={this.props.stream}
                url={this.props.url}
                onError={this.props.onError}
                width={this.props.width}
                height={this.props.height}
                onMessage={this.onMessage}
                renderWidget={this.renderWidget}
                onResize={this.onResize}
            />
        )
    }
}
