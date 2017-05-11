/* global Streamr, StreamrCredentialsControl */

import React, {Component} from 'react'

export default class APICredentials extends Component {
    componentDidMount() {
        this.apiHandler = new StreamrCredentialsControl({
            el: this.apiHandlerEl,
            url: Streamr.createLink({
                uri: '/api/v1/users/me/keys'
            }),
            username: Streamr.users
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