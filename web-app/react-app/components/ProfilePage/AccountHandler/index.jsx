
import React from 'react'
import { Panel, Row } from 'react-bootstrap'

import StreamrAccountHandlerSegment from './AccountHandlerSegment'

export default class StreamrAccountHandler extends React.Component {
    
    render() {
        return (
            <Panel header="Accounts">
                <Row>
                    <StreamrAccountHandlerSegment
                        type="ETHEREUM"
                        name="Ethereum"
                        inputFields={['privateKey']}
                        tableFields={['publicKey']}
                    />
                    <StreamrAccountHandlerSegment
                        type="TWITTER"
                        name="Twitter"
                        inputFields={['privateKey']}
                        tableFields={['publicKey']}
                    />
                </Row>
            </Panel>
        )
    }
}