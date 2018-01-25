// @flow

import React, {Component} from 'react'

import { connect } from 'react-redux'
import { getIntegrationKeysByService, createIntegrationKey, deleteIntegrationKey } from '../../../../actions/integrationKey'

import {Col, ControlLabel} from 'react-bootstrap'

import IntegrationKeyHandlerInput from './IntegrationKeyHandlerInput'
import IntegrationKeyHandlerTable from './IntegrationKeyHandlerTable'

import styles from './integrationKeyHandlerSegment.pcss'

import type {IntegrationKey, State as IntegrationKeyState} from '../../../../flowtype/integration-key-types'

type Props = {
    tableFields: Array<string>,
    inputFields: Array<string>,
    integrationKeys: Array<IntegrationKey>,
    service: IntegrationKey.service,
    name: IntegrationKey.name,
    className: string,
    deleteIntegrationKey: (id: IntegrationKey.id) => void,
    createIntegrationKey: (key: IntegrationKey) => void,
    getIntegrationKeysByService: (service: IntegrationKey.service) => void
}

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
    
    onDelete = (id: IntegrationKey.id) => {
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

export const mapStateToProps = ({integrationKey: {listsByService, error}}: {integrationKey: IntegrationKeyState}, props: Props) => ({
    integrationKeys: listsByService[props.service] || [],
    error
})

export const mapDispatchToProps = (dispatch: Function) => ({
    deleteIntegrationKey(id: IntegrationKey.id) {
        dispatch(deleteIntegrationKey(id))
    },
    createIntegrationKey(key: IntegrationKey) {
        dispatch(createIntegrationKey(key))
    },
    getIntegrationKeysByService(service: IntegrationKey.service) {
        dispatch(getIntegrationKeysByService(service))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(IntegrationKeyHandlerSegment)