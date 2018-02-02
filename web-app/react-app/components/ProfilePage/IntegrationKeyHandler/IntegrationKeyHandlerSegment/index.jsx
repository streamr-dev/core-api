// @flow

import React, {Component} from 'react'

import { connect } from 'react-redux'
import { getIntegrationKeysByService, createIntegrationKey, deleteIntegrationKey } from '../../../../actions/integrationKey'

import {Col, ControlLabel} from 'react-bootstrap'

import IntegrationKeyHandlerInput from './IntegrationKeyHandlerInput'
import IntegrationKeyHandlerTable from './IntegrationKeyHandlerTable'

import styles from './integrationKeyHandlerSegment.pcss'

import type {IntegrationKeyState} from '../../../../flowtype/states/integration-key-state'
import type {IntegrationKey} from '../../../../flowtype/integration-key-types'

type StateProps = {
    integrationKeys: Array<IntegrationKey>
}

type DispatchProps = {
    deleteIntegrationKey: (id: $ElementType<IntegrationKey, 'id'>) => void,
    createIntegrationKey: (key: IntegrationKey) => void,
    getIntegrationKeysByService: (service: $ElementType<IntegrationKey, 'service'>) => void
}

type GivenProps = {
    tableFields: Array<string>,
    inputFields: Array<string>,
    service: $ElementType<IntegrationKey, 'service'>,
    name: $ElementType<IntegrationKey, 'name'>,
    className: string
}

type Props = StateProps & DispatchProps & GivenProps

export class IntegrationKeyHandlerSegment extends Component<Props> {
    
    componentDidMount() {
        // TODO: Move to (yet non-existent) router
        this.props.getIntegrationKeysByService(this.props.service)
    }
    
    onNew = (integrationKey: IntegrationKey) => {
        const name = integrationKey.name
        const service = this.props.service
        delete integrationKey.name
        return this.props.createIntegrationKey({
            name,
            service,
            json: integrationKey
        })
    }
    
    onDelete = (id: $ElementType<IntegrationKey, 'id'>) => {
        this.props.deleteIntegrationKey(id)
    }
    
    render() {
        return (
            <div className={this.props.className || ''}>
                <Col xs={12}>
                    <ControlLabel className={styles.label}>
                        {this.props.name}
                    </ControlLabel>
                    <IntegrationKeyHandlerTable
                        fields={this.props.tableFields}
                        integrationKeys={this.props.integrationKeys}
                        onDelete={this.onDelete}
                    />
                    <IntegrationKeyHandlerInput
                        fields={this.props.inputFields}
                        onNew={this.onNew}
                    />
                </Col>
            </div>
        )
    }
}

export const mapStateToProps = ({integrationKey: {listsByService}}: {integrationKey: IntegrationKeyState}, props: Props): StateProps => ({
    integrationKeys: listsByService[props.service] || []
})

export const mapDispatchToProps = (dispatch: Function): DispatchProps => ({
    deleteIntegrationKey(id: $ElementType<IntegrationKey, 'id'>) {
        dispatch(deleteIntegrationKey(id))
    },
    createIntegrationKey(key: IntegrationKey) {
        dispatch(createIntegrationKey(key))
    },
    getIntegrationKeysByService(service: $ElementType<IntegrationKey, 'service'>) {
        dispatch(getIntegrationKeysByService(service))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(IntegrationKeyHandlerSegment)