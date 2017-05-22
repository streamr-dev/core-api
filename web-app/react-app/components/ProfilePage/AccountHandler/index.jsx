
import React from 'react'

import StreamrAccountHandlerSegment from './AccountHandlerSegment'

export default class StreamrAccountHandler extends React.Component {
    
    render() {
        return (
            <div className="streamr-account-handler panel">
                <div className="panel-heading">
                    Accounts
                </div>
                <div className="panel-body">
                    <StreamrAccountHandlerSegment className="row" type="ETHEREUM" name="Ethereum" fields={['privateKey']}/>
                </div>
            </div>
        )
    }
}