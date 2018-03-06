// @flow

import React, {Component} from 'react'
import {Panel, Row} from 'react-bootstrap'
import IntegrationKeyHandlerSegment from '../IntegrationKeyHandler/IntegrationKeyHandlerSegment'
import type {IntegrationKey} from '../../../flowtype/integration-key-types'
import {createIdentity} from '../../../actions/identity'
import {connect} from 'react-redux'
import type {ErrorInUi} from '../../../flowtype/common-types'
import type {IntegrationKeyState} from '../../../flowtype/states/integration-key-state'

type StateProps = {
    error: ?ErrorInUi,
}

type DispatchProps = {
    createIdentity: (integrationKey: IntegrationKey) => void,
}

type GivenProps = {
    service: $ElementType<IntegrationKey, 'service'>,
}

type Props = StateProps & DispatchProps & GivenProps

export class IdentityHandler extends Component<Props> {
    onNew = (integrationKey: IntegrationKey) => {
        const name = integrationKey.name
        const service = this.props.service
        delete integrationKey.name
        this.props.createIdentity({
            name,
            service,
            json: integrationKey,
        })
    }
    render() {
        return (
            <Panel header="Identities">
                <Row>
                    <IntegrationKeyHandlerSegment
                        onNew={this.onNew}
                        service={this.props.service}
                        name="Ethereum"
                        inputFields={[]}
                        tableFields={['address']}
                    />
                </Row>
            </Panel>
        )
    }
}

export const mapStateToProps = ({integrationKey: {listsByService, error}}: {integrationKey: IntegrationKeyState}, props: GivenProps): StateProps => ({
    integrationKeys: listsByService[props.service] || [],
    error
})

export const mapDispatchToProps = (dispatch: Function): DispatchProps => ({
    createIdentity(integrationKey: IntegrationKey) {
        dispatch(createIdentity(integrationKey))
    },
})

export default connect(mapStateToProps, mapDispatchToProps)(IdentityHandler)
