// @flow

import React, {Component} from 'react'
import {Col, ControlLabel} from 'react-bootstrap'
import IntegrationKeyHandlerInput from './IntegrationKeyHandlerInput'
import IntegrationKeyHandlerTable from './IntegrationKeyHandlerTable'

import styles from './integrationKeyHandlerSegment.pcss'

import type {IntegrationKey} from '../../../../flowtype/integration-key-types'
import type {Props as TableProps} from './IntegrationKeyHandlerTable'
import type {Props as InputProps} from './IntegrationKeyHandlerInput'

type GivenProps = {
    className?: string,
    name?: $ElementType<IntegrationKey, 'name'>,
    showInput: boolean
}

type Props = InputProps & TableProps & GivenProps

export default class IntegrationKeyHandlerSegment extends Component<Props> {
    static defaultProps = {
        showInput: true
    }
    render() {
        return (
            <div className={this.props.className || ''}>
                <Col xs={12}>
                    {this.props.name && (
                        <ControlLabel className={styles.label}>
                            {this.props.name}
                        </ControlLabel>
                    )}
                    <IntegrationKeyHandlerTable
                        tableFields={this.props.tableFields}
                        integrationKeys={this.props.integrationKeys}
                        onDelete={this.props.onDelete}
                        copy={this.props.copy}
                    />
                    {this.props.showInput && (
                        <IntegrationKeyHandlerInput
                            inputFields={this.props.inputFields}
                            onNew={this.props.onNew}
                        />
                    )}
                </Col>
            </div>
        )
    }
}
