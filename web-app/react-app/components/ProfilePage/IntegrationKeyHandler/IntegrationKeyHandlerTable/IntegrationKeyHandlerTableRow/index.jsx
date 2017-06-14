// @flow

import React from 'react'

import { FormGroup } from 'react-bootstrap'
import ConfirmButton from '../../../../ConfirmButton'

import styles from './integrationKeyHandlerTableRow.pcss'


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
                        <ConfirmButton
                            confirmCallback={() => this.props.onDelete(this.props.item.id)}
                            buttonProps={{
                                bsStyle: 'danger',
                                type: 'button',
                                title: 'Delete key'
                            }}
                            confirmTitle="Are you sure?"
                            confirmMessage={`Are you sure you want to remove integration key ${this.props.item.name}?`}
                            className={styles.deleteButton}>
                            <span className="icon fa fa-trash-o"/>
                        </ConfirmButton>
                    </FormGroup>
                </td>
            </tr>
        )
    }
}