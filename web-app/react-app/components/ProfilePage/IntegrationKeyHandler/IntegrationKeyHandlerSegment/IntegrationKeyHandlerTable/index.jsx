// @flow

import React, {Component} from 'react'

import IntegrationKeyHandlerTableRow from './IntegrationKeyHandlerTableRow'
import {Table} from 'react-bootstrap'

import {titleCase} from 'change-case'

import styles from './integrationKeyHandlerTable.pcss'

import type {IntegrationKey} from '../../../../../flowtype/integration-key-types.js'

type Props = {
    fields: Array<string>,
    integrationKeys: Array<IntegrationKey>,
    onDelete: Function
}

export default class IntegrationKeyHandlerTable extends Component<Props> {
    
    render() {
        const items = this.props.integrationKeys || []
        return (
            <Table className={styles.integrationKeyTable}>
                <thead>
                    <tr>
                        <th className={styles.nameHeader}>Name</th>
                        {this.props.fields.map(f => (
                            <th key={f}>
                                {titleCase(f)}
                            </th>
                        ))}
                        <th className={styles.actionHeader}/>
                    </tr>
                </thead>
                <tbody>
                    {items.map(item => (
                        <IntegrationKeyHandlerTableRow item={item} key={item.id} fields={this.props.fields} onDelete={this.props.onDelete}/>
                    ))}
                </tbody>
            </Table>
        )
    }
}