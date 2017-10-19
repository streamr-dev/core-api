// @flow

declare var Streamr: any
declare var StreamrCredentialsControl: any

import React, {Component} from 'react'

export default class APICredentials extends Component {
    
    apiHandlerEl: HTMLDivElement // Typechecking may not work correctly but without this line it does not work at all
    
    componentDidMount() {
        new StreamrCredentialsControl({
            el: this.apiHandlerEl,
            url: Streamr.createLink({
                uri: 'api/v1/users/me/keys'
            }),
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