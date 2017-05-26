// @flow

import React from 'react'

import { connect } from 'react-redux'
import { getIntegrationKeysByService, createIntegrationKey, deleteIntegrationKey } from '../../../../actions/integrationKeys'

import {Col, ControlLabel} from 'react-bootstrap'

import IntegrationKeyHandlerInput from '../IntegrationKeyHandlerInput'
import IntegrationKeyHandlerTable from '../IntegrationKeyHandlerTable'

import styles from './integrationKeyHandlerSegment.pcss'

declare var Streamr: any

class IntegrationKeyHandlerSegment extends React.Component {
    
    props: {
        tableFields: Array<string>,
        inputFields: Array<string>,
        integrationKeys: Array<{
            id: string,
            name: string,
            json: {}
        }>,
        service: string,
        name: string,
        className: string,
        dispatch: Function
    }
    
    onNew: Function
    onDelete: Function
    
    constructor(props) {
        super(props)
        
        this.onNew = this.onNew.bind(this)
        this.onDelete = this.onDelete.bind(this)
    }
    componentDidMount() {
        this.props.dispatch(getIntegrationKeysByService(this.props.service))
    }
    
    onNew(integrationKey) {
        const name = integrationKey.name
        const service = this.props.service
        delete integrationKey.name
        return this.props.dispatch(createIntegrationKey({
            name,
            service,
            json: integrationKey
        }))
            .then(() => Streamr.showSuccess('IntegrationKey created successfully!'))
            .catch(e => Streamr.showError('Error!', e.message))
    }
    
    onDelete(id) {
        this.props.dispatch(deleteIntegrationKey(id))
            .then(() => Streamr.showSuccess('IntegrationKey removed successfully!'))
    }
    
    render() {
        return (
            <div className={this.props.className}>
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

const mapStateToProps = ({integrationKey}, props) => ({
    integrationKeys: integrationKey.listsByService[props.service] || [],
    error: integrationKey.error
})

export default connect(mapStateToProps)(IntegrationKeyHandlerSegment)