// @flow

import React from 'react'

import IntegrationKeyHandlerTableRow from './IntegrationKeyHandlerTableRow'
import {Table} from 'react-bootstrap'

const unCamelCase = (str: string) => str
    .replace(/([a-z])([A-Z])/g, '$1 $2')
    .replace(/\b([A-Z]+)([A-Z])([a-z])/, '$1 $2$3')
    .replace(/^./, s => s.toUpperCase())

import styles from './integrationKeyHandlerTable.pcss'

type IntegrationKey = {
    id: string,
    name: string,
    json: {}
}

export default class IntegrationKeyHandlerTable extends React.Component {
    
    props: {
        fields: Array<string>,
        integrationKeys: Array<IntegrationKey>,
        onDelete: Function
    }
    
    render() {
        const items = this.props.integrationKeys || []
        return (
            <Table className={styles.integrationKeyTable}>
                <thead>
                    <tr>
                        <th className={styles.nameHeader}>Name</th>
                        {this.props.fields.map(f => (
                            <th key={f}>
                                {unCamelCase(f)}
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