// @flow

import React, {Component} from 'react'
import axios from 'axios'
import _ from 'lodash'

import {any} from 'prop-types'

//import type {Webcomponent} from '../../../flowtype/webcomponent-types.js'

import type {StreamId, SubscriptionOptions, Subscription, ModuleOptions} from '../../../flowtype/streamr-client-types'

import type {ReactChildren} from 'react-flow-types'

type Props = {
    children?: ReactChildren,
    subscriptionOptions: SubscriptionOptions,
    url: string,
    onError: (string) => void,
    onMessage: ({}) => void,
    onSubscribed?: (opt: ?{
        from?: number
    }) => void,
    onUnsubscribed?: () => void,
    onResending?: () => void,
    onResent?: () => void,
    onNoResend?: () => void,
    onModuleJson?: (json: {
        options: ModuleOptions
    }) => void
}
export default class StreamrWidget extends Component {
    getModuleJson: Function
    getHeaders: Function
    sendRequest: Function
    onMessage: Function
    subscription: ?Subscription
    stream: StreamId
    props: Props
    static contextTypes = {
        client: any
    }
    
    constructor() {
        super()
        this.getModuleJson = this.getModuleJson.bind(this)
        this.getHeaders = this.getHeaders.bind(this)
        this.sendRequest = this.sendRequest.bind(this)
        this.onMessage = this.onMessage.bind(this)
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
        this.getModuleJson((json: {
            uiChannel?: {
                id: string
            },
            options: ModuleOptions
        }) => {
            this.props.onModuleJson && this.props.onModuleJson(json)
            const options = json.options || {}
            if (!subscriptionOptions.stream) {
                this.stream = json.uiChannel && json.uiChannel.id
            }
            if (this.stream && !this.subscription) {
                this.subscription = this.context.client.subscribe({
                    stream: this.stream,
                    authKey: subscriptionOptions.authKey,
                    partition: subscriptionOptions.partition,
                    resend_all: subscriptionOptions.resend_all || (options.uiResendAll || {}).value,
                    resend_last: subscriptionOptions.resend_last || (options.uiResendLast || {}).value,
                    resend_from: subscriptionOptions.resend_from,
                    resend_from_time: subscriptionOptions.resend_from_time,
                    resend_to: subscriptionOptions.resend_to,
                }, this.onMessage)
                safeBind(this.subscription, 'subscribed', onSubscribed)
                safeBind(this.subscription, 'unsubscribed', onUnsubscribed)
                safeBind(this.subscription, 'resending', onResending)
                safeBind(this.subscription, 'resent', onResent)
                safeBind(this.subscription, 'no_resend', onNoResend)
            }
        })
    }
    
    onMessage(msg: {}) {
        this.props.onMessage && this.props.onMessage(msg)
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