// @flow

import React, {Component} from 'react'

import { FormGroup } from 'react-bootstrap'
import ConfirmButton from '../../../../../ConfirmButton'
import FontAwesome from 'react-fontawesome'

import styles from './integrationKeyHandlerTableRow.pcss'

import type {IntegrationKey} from '../../../../../../flowtype/integration-key-types'

type Props = {
    fields: Array<string>,
    onDelete: Function,
    item: IntegrationKey
}

export default class IntegrationKeyHandlerTableRow extends Component<Props> {
    
    render() {
        const {item, onDelete, fields} = this.props
        return (
            <tr key={item.id}>
                <td>
                    {item.name}
                </td>
                {fields.map(f => (
                    <td key={f}>
                        <span className={styles.publicKey}>{item.json[f]}</span>
                    </td>
                ))}
                <td>
                    <FormGroup className="pull-right">
                        <ConfirmButton
                            confirmCallback={() => onDelete(item.id)}
                            buttonProps={{
                                bsStyle: 'danger',
                                type: 'button',
                                title: 'Delete key'
                            }}
                            confirmTitle="Are you sure?"
                            confirmMessage={`Are you sure you want to remove integration key ${item.name}?`}
                            className={styles.deleteButton}>
                            <FontAwesome name="trash-o" className="icon"/>
                        </ConfirmButton>
                    </FormGroup>
                </td>
            </tr>
        )
    }
}