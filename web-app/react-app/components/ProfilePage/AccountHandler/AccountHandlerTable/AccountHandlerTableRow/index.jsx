/* global ConfirmButton */

import React from 'react'
import {func, array, object} from 'prop-types'

import styles from './accountHandlerTableRow.pcss'

export default class StreamrAccountHandlerTable extends React.Component {
    componentDidMount() {
        new ConfirmButton(this.removeButton, {
            title: 'Are you sure?',
            message: `Are you sure you want to remove account ${this.props.item.name}?`
        }, res => {
            if (res) {
                this.props.onDelete(this.props.item.id)
            }
        })
    }
    render() {
        const item = this.props.item
        return (
            <tr key={item.id}>
                <td>
                    {item.name}
                </td>
                {this.props.fields.map(f => (
                    <td key={f}>
                        <span className={styles.publicKey}>{item.json[f]}</span>
                    </td>
                ))}
                <td>
                    <button
                        ref={el => this.removeButton = el}
                        type="button"
                        className={`form-group account-item-delete-button btn btn-danger pull-right ${styles.deleteButton}`}
                        title="Delete key">
                        <span className="icon fa fa-trash-o"/>
                    </button>
                </td>
            </tr>
        )
    }
}
StreamrAccountHandlerTable.propTypes = {
    fields: array,
    accounts: array,
    onDelete: func,
    item: object
}