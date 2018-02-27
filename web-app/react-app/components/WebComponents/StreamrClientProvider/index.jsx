// @flow

import React, {Component} from 'react'

import {any} from 'prop-types'

import type {StreamrClient} from '../../../flowtype/streamr-client-types'
import type {Node} from 'react'

type Props = {
    client: StreamrClient,
    children?: Node
}

let didWarnAboutChangingClient = false

function warnAboutChangingClient() {
    if (didWarnAboutChangingClient) {
        return
    }
    didWarnAboutChangingClient = true

    console.warn(
        '<StreamrClientProvider> does not support changing `client` on the fly.',
    )
}

export default class StreamrClientProvider extends Component<Props> {
    client: StreamrClient
    static childContextTypes = {
        client: any,
    }

    getChildContext() {
        return {
            client: this.client,
        }
    }

    constructor(props: Props) {
        super(props)
        this.client = props.client
    }

    componentWillReceiveProps(nextProps: Props) {
        if (nextProps.client !== this.props.client) {
            warnAboutChangingClient()
        }
    }

    render() {
        return React.Children.only(this.props.children)
    }
}
