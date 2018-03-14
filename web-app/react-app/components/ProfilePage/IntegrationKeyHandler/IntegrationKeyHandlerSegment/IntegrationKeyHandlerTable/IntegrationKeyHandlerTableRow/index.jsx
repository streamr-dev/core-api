// @flow

import React, {Component, type Node} from 'react'

import {Button} from 'react-bootstrap'
import ConfirmButton from '../../../../../ConfirmButton'
import FontAwesome from 'react-fontawesome'
import {CopyToClipboard} from 'react-copy-to-clipboard'

import styles from './integrationKeyHandlerTableRow.pcss'

import type {IntegrationKey} from '../../../../../../flowtype/integration-key-types'

export type Props = {
    fields?: Array<string | [string, (any) => Node]>,
    onDelete: (id: $ElementType<IntegrationKey, 'id'>) => void,
    copy?: string,
    show?: string,
    item: IntegrationKey
}

type State = {
    copied: boolean
}

export default class IntegrationKeyHandlerTableRow extends Component<Props, State> {
    timeout: TimeoutID
    state = {
        copied: false
    }
    onCopy = () => {
        this.setState({
            copied: true
        })
        clearTimeout(this.timeout)
        this.timeout = setTimeout(() => this.setState({
            copied: false
        }), 3000)
    }

    render() {
        const {item, onDelete, fields} = this.props
        return (
            <tr key={item.id}>
                <td>
                    {item.name}
                </td>
                {fields && fields.map(f => (
                    <td key={JSON.stringify(f)}>
                        <span className={styles.publicKey}>{Array.isArray(f) ? (
                            f[1](item.json[f[0]])
                        ) : (
                            item.json[f]
                        )}</span>
                    </td>
                ))}
                <td>
                    <div className={styles.actionButtonContainer}>
                        {this.props.copy && (
                            <CopyToClipboard
                                text={this.props.item.json[this.props.copy]}
                                onCopy={() => this.onCopy()}
                            >
                                <Button>
                                    <FontAwesome name={this.state.copied ? 'check' : 'copy'}/>
                                </Button>
                            </CopyToClipboard>
                        )}
                        <ConfirmButton
                            confirmCallback={() => onDelete(item.id)}
                            buttonProps={{
                                bsStyle: 'danger',
                                type: 'button',
                                title: 'Delete key',
                            }}
                            confirmTitle="Are you sure?"
                            confirmMessage={`Are you sure you want to remove integration key ${item.name}?`}
                            className={styles.deleteButton}>
                            <FontAwesome name="trash-o" className="icon"/>
                        </ConfirmButton>
                    </div>
                </td>
            </tr>
        )
    }
}
