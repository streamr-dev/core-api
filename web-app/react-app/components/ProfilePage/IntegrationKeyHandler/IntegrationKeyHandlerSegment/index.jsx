// @flow

import React, {Component} from 'react'

import { connect } from 'react-redux'
import { getIntegrationKeysByService, createIntegrationKey, deleteIntegrationKey } from '../../../../actions/integrationKey'

import {Col, ControlLabel} from 'react-bootstrap'

import IntegrationKeyHandlerInput from './IntegrationKeyHandlerInput'
import IntegrationKeyHandlerTable from './IntegrationKeyHandlerTable'

import styles from './integrationKeyHandlerSegment.pcss'

import type {IntegrationKey} from '../../../../flowtype/integration-key-types'

type Props = {
    tableFields: Array<string>,
    inputFields: Array<string>,
    integrationKeys: Array<{
        id: string,
        name: string,
        json: {}
    }>,
    service: string,
    name: string,
    className: string
}

class IntegrationKeyHandlerSegment extends Component<Props> {
    
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

const mapStateToProps = ({integrationKey: {listsByService, error}}, props) => ({
    integrationKeys: listsByService[props.service] || [],
    error
})

const mapDispatchToProps = (dispatch: Function) => ({
    deleteIntegrationKey(id) {
        dispatch(deleteIntegrationKey(id))
    },
    createIntegrationKey(key) {
        dispatch(createIntegrationKey(key))
    },
    getIntegrationKeysByService(service) {
        dispatch(getIntegrationKeysByService(service))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(IntegrationKeyHandlerSegment)