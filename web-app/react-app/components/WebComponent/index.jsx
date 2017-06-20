// @flow

import React, {Component} from 'react'

type type = 'streamr-chart' | 'streamr-table' | 'streamr-label' | 'streamr-map'

export default class WebComponent extends Component {
    webcomponent: HTMLElement
    bindError: Function
    unbindError: Function
    props: {
        url: string,
        type: type,
        webComponentRef: Function,
        onError: Function
    }
    static defaultProps = {
        webComponentRef: () => {},
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
        const Type = this.props.type
        return (
            <Type
                ref={item => {
                    this.webcomponent = item
                    this.props.webComponentRef(item)
                }}
                className="streamr-widget non-draggable"
                url={this.props.url}
            />
        )
    }
}