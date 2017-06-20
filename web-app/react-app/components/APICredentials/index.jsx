// @flow

import React, {Component} from 'react'
import createLink from '../../createLink'

declare var StreamrCredentialsControl: any
declare var Streamr: {
    user: string
}

export default class APICredentials extends Component {
    apiHandlerEl: HTMLDivElement
    
    componentDidMount() {
        new StreamrCredentialsControl({
            el: this.apiHandlerEl,
            url: createLink('api/v1/users/me/keys'),
            username: Streamr.user
        })
    }
    
    render() {
        return (
            <div className="panel">
                <div className="panel-heading">
                    <span className="panel-title">API Credentials</span>
                    <div className="panel-heading-controls">
                    
                    </div>
                </div>
                <div className="panel-body">
                    <div ref={item => this.apiHandlerEl = item} className="credentials-control row"/>
                </div>
            </div>
        )
    }
}