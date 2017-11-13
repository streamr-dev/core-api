// @flow

import React, {Component} from 'react'

import type {Webcomponent} from '../../flowtype/webcomponent-types.js'

export default class WebComponent extends Component {
    webcomponent: HTMLElement
    bindError: Function
    unbindError: Function
    props: {
        type: Webcomponent.type,
        url: Webcomponent.url,
        webComponentRef: Function,
        onError: Function
    }
    static defaultProps = {
        onError: () => {}
    }
    constructor() {
        super()
        this.bindError = this.bindError.bind(this)
        this.unbindError = this.unbindError.bind(this)
    }
    
    bindError() {
        this.webcomponent.addEventListener('error', this.props.onError)
    }
    
    unbindError() {
        this.webcomponent.removeEventListener('error', this.props.onError)
    }
    
    componentDidUpdate() {
        this.bindError()
    }
    
    componentWillUpdate() {
        this.unbindError()
    }
    
    render() {
        const {type: WebcomponentType} = this.props
        return (
            <WebcomponentType
                ref={item => {
                    this.webcomponent = item
                    if (this.props.webComponentRef) {
                        this.props.webComponentRef(item)
                    }
                }}
                className="streamr-widget non-draggable"
                url={this.props.url}
            />
        )
    }
}