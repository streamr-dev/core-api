// @flow

import React, {Component} from 'react'

import IntegrationKeyHandlerTableRow from './IntegrationKeyHandlerTableRow'
import {Table} from 'react-bootstrap'

import {titleCase} from 'change-case'

import styles from './integrationKeyHandlerTable.pcss'

import type {IntegrationKey} from '../../../../../flowtype/integration-key-types.js'
import type {Props as TableRowProps} from './IntegrationKeyHandlerTableRow'

export type Props = {
    integrationKeys: Array<IntegrationKey>,
    tableFields?: $ElementType<TableRowProps, 'fields'>,
    onDelete: $ElementType<TableRowProps, 'onDelete'>,
    copy?: $ElementType<TableRowProps, 'copy'>
}

export default class IntegrationKeyHandlerTable extends Component<Props> {

    render() {
        const {integrationKeys, tableFields, onDelete} = this.props
        return (
            <Table className={styles.integrationKeyTable}>
                <thead>
                    <tr>
                        <th className={styles.nameHeader}>Name</th>
                        {tableFields && tableFields.map(f => (
                            <th key={JSON.stringify(f)}>
                                {titleCase(Array.isArray(f) ? f[0] : f)}
                            </th>
                        ))}
                        <th className={styles.actionHeader}/>
                    </tr>
                </thead>
                <tbody>
                    {integrationKeys.map(item => (
                        <IntegrationKeyHandlerTableRow
                            item={item}
                            key={item.id}
                            fields={tableFields}
                            onDelete={onDelete}
                            copy={this.props.copy}
                        />
                    ))}
                </tbody>
            </Table>
        )
    }
}
