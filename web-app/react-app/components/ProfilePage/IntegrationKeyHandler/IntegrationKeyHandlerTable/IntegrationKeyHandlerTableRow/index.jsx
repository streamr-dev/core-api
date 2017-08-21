// @flow

import React from 'react'

import { FormGroup } from 'react-bootstrap'

import styles from './integrationKeyHandlerTableRow.pcss'

declare var ConfirmButton: any

export default class IntegrationKeyHandlerTableRow extends React.Component {
    
    props: {
        fields: Array<string>,
        onDelete: Function,
        item: {
            id: string,
            name: string,
            json: {}
        }
    }
    
    removeButton: HTMLButtonElement
    
    componentDidMount() {
        new ConfirmButton(this.removeButton, {
            title: 'Are you sure?',
            message: `Are you sure you want to remove integration key ${this.props.item.name}?`
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
                    <FormGroup className="pull-right">
                        <button
                            ref={el => this.removeButton = el}
                            type="button"
                            className={`btn btn-danger ${styles.deleteButton}`}
                            title="Delete key">
                            <span className="icon fa fa-trash-o"/>
                        </button>
                    </FormGroup>
                </td>
            </tr>
        )
    }
}