// @flow

import React, {Component} from 'react'
import {Panel, Row} from 'react-bootstrap'

import IntegrationKeyHandlerSegment from './IntegrationKeyHandlerSegment'

export default class IntegrationKeyHandler extends Component<{}> {

    render() {
        return (
            <Panel>
                <Panel.Heading>
                    Integration Keys
                </Panel.Heading>
                <Panel.Body>
                    <Row>
                        <IntegrationKeyHandlerSegment
                            service="ETHEREUM"
                            name="Ethereum"
                            inputFields={['privateKey']}
                            tableFields={['address']}
                        />
                    </Row>
                </Panel.Body>
            </Panel>
        )
    }
}
