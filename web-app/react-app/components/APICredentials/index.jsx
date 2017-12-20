// @flow

declare var Streamr: any
declare var StreamrCredentialsControl: any

import React, {Component} from 'react'
import {Panel} from 'react-bootstrap'

export default class APICredentials extends Component<{}> {
    
    apiHandlerEl: ?HTMLDivElement // Typechecking may not work correctly but without this line it does not work at all
    
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
            <Panel header="API Credentials">
                <div ref={item => this.apiHandlerEl = item} className="credentials-control row"/>
            </Panel>
        )
    }
}