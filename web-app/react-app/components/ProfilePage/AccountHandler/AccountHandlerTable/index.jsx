/* global ConfirmButton */

import React from 'react'
import {func, array} from 'prop-types'

import styles from './accountHandlerTable.pcss'

const unCamelCase = str => str
    .replace(/([a-z])([A-Z])/g, '$1 $2')
    .replace(/\b([A-Z]+)([A-Z])([a-z])/, '$1 $2$3')
    .replace(/^./, s => s.toUpperCase())

export default class StreamrAccountHandlerTable extends React.Component {
    
    render() {
        const items = this.props.accounts || []
        return (
            <table className="table">
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
                    <tr key={item.id}>
                        <td>
                            {item.name}
                        </td>
                        {this.props.fields.map(f => (
                            <td key={f}>
                                {item.json[f]}
                            </td>
                        ))}
                        <td>
                            <button
                                ref={el => {
                                    new ConfirmButton(el, {}, res => {
                                        if (res) {
                                            this.props.onDelete(item.id)
                                        }
                                    })
                                }}
                                type="button"
                                className={`form-group account-item-delete-button btn btn-danger pull-right ${styles.deleteButton}`}
                                title="Delete key">
                                <span className="icon fa fa-trash-o"/>
                            </button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        )
    }
}
StreamrAccountHandlerTable.propTypes = {
    fields: array,
    accounts: array,
    onDelete: func,
}