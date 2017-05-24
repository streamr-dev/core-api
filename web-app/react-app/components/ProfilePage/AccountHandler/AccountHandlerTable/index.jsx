
import React from 'react'
import {func, array} from 'prop-types'

import AccountHandlerTableRow from './AccountHandlerTableRow'
import {Table} from 'react-bootstrap'


const unCamelCase = str => str
    .replace(/([a-z])([A-Z])/g, '$1 $2')
    .replace(/\b([A-Z]+)([A-Z])([a-z])/, '$1 $2$3')
    .replace(/^./, s => s.toUpperCase())

import styles from './accountHandlerTable.pcss'

export default class StreamrAccountHandlerTable extends React.Component {
    
    render() {
        const items = this.props.accounts || []
        return (
            <Table className={styles.accountTable}>
                <thead>
                <tr>
                    <th>Name</th>
                    {this.props.fields.map(f => (
                        <th key={f}>
                            {unCamelCase(f)}
                        </th>
                    ))}
                    <th/>
                </tr>
                </thead>
                <tbody>
                {items.map(item => (
                    <AccountHandlerTableRow item={item} key={item.id} fields={this.props.fields} onDelete={this.props.onDelete}/>
                ))}
                </tbody>
            </Table>
        )
    }
}
StreamrAccountHandlerTable.propTypes = {
    fields: array,
    accounts: array,
    onDelete: func,
}