// @flow

import React, {Component} from 'react'
import axios from 'axios'
import _ from 'lodash'

import {any} from 'prop-types'

//import type {Webcomponent} from '../../../flowtype/webcomponent-types.js'

import type {StreamId, SubscriptionOptions, Subscription} from '../../../flowtype/streamr-client-types'

import type {ReactChildren} from 'react-flow-types'

type Props = {
    children?: ReactChildren,
    subscriptionOptions: SubscriptionOptions,
    url: string,
    onError: (string) => void,
    onMessage: (any) => void,
    onSubscribed?: (opt: ?{
        from?: number
    }) => void,
    onUnsubscribed?: () => void,
    onResending?: () => void,
    onResent?: () => void,
    onNoResend?: () => void,
}
export default class StreamrWidget extends Component {
    getModuleJson: Function
    getHeaders: Function
    sendRequest: Function
    subscription: ?Subscription
    stream: StreamId
    props: Props
    static contextTypes = {
        client: any
    }
    
    constructor(props: Props) {
        super(props)
        this.getModuleJson = this.getModuleJson.bind(this)
        this.getHeaders = this.getHeaders.bind(this)
        this.sendRequest = this.sendRequest.bind(this)
    }
    
    getHeaders() {
        return this.context.client.options.authKey ? {
            'Authorization': `Token ${this.context.client.options.authKey}`
        } : {}
    }
    
    getModuleJson(callback: (any) => void) {
        this.sendRequest({
            type: 'json'
        })
            .then((res: {
                data: {
                    json: {
                        uiChannel: {
                            id: string
                        }
                    }
                }
            }) => {
                callback(res.data.json)
            })
            .catch((res) => {
                if (process.env.NODE_ENV === 'production') {
                    console.error('Error while communicating with widget')
                } else {
                    console.error('Error while communicating with widget')
                    console.error(JSON.stringify(res))
                }
                if (this.props.onError) {
                    this.props.onError(res)
                }
            })
    }
    
    componentDidMount() {
        const safeBind = (sub: ?Subscription, event: string, callback: ?Function) => {
            if (sub && callback && event) {
                sub.bind(event, callback)
            }
        }
        const {subscriptionOptions, onSubscribed, onUnsubscribed, onResending, onResent, onNoResend} = this.props
        new Promise((resolve) => {
            if (!this.props.subscriptionOptions.stream) {
                this.getModuleJson((json) => {
                    this.stream = json.uiChannel && json.uiChannel.id
                    resolve()
                })
            } else {
                resolve()
            }
        })
            .then(() => {
                if (this.stream && !this.subscription) {
                    this.subscription = this.context.client.subscribe({
                        stream: this.stream,
                        authKey: subscriptionOptions.authKey,
                        partition: subscriptionOptions.partition,
                        resend_all: subscriptionOptions.resend_all,
                        resend_last: subscriptionOptions.resend_last,
                        resend_from: subscriptionOptions.resend_from,
                        resend_from_time: subscriptionOptions.resend_from_time,
                        resend_to: subscriptionOptions.resend_to,
                    }, this.props.onMessage)
                    safeBind(this.subscription, 'subscribed', onSubscribed)
                    safeBind(this.subscription, 'unsubscribed', onUnsubscribed)
                    safeBind(this.subscription, 'resending', onResending)
                    safeBind(this.subscription, 'resent', onResent)
                    safeBind(this.subscription, 'no_resend', onNoResend)
                }
            })
    }
    
    componentWillUnmount() {
        if (this.subscription) {
            this.context.client.unsubscribe(this.subscription)
            this.subscription = undefined
        }
    }
    
    sendRequest(msg: {}): Promise<any> {
        return axios.post(`${this.props.url}/request`, msg, {
            headers: {
                ...this.getHeaders()
            }
        })
    }
    
    componentWillReceiveProps(newProps: Props) {
        if (newProps.subscriptionOptions !== undefined && !_.isEqual(this.props.subscriptionOptions, newProps.subscriptionOptions)) {
            console.warn('Updating stream subscriptionOptions on the fly is not (yet) possible')
        }
    }
    
    render() {
        return React.Children.only(this.props.children)
    }
}