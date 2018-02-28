// @flow

import React, {Component} from 'react'
import {Table, Button, Modal, FormControl, InputGroup} from 'react-bootstrap'
import {CopyToClipboard} from 'react-copy-to-clipboard'
import serialize from 'form-serialize'
import FontAwesome from 'react-fontawesome'
import ConfirmButton from '../ConfirmButton'
import Select from 'react-select'
import 'react-select/dist/react-select.css'

import type {Key} from '../../flowtype/key-types'
import type {OnSubmitEvent} from '../../flowtype/common-types'

import styles from './credentialsControl.pcss'

type Props = {
    keys: Array<Key>,
    addKey: (key: Key) => void,
    removeKey: (id: $ElementType<Key, 'id'>) => void,
    permissionTypeVisible: boolean
}

type State = {
    showKey: ?Key | false,
    copied: $ElementType<Key, 'id'> | false,
    permissionSelectorValue: string
}

export default class CredentialsControl extends Component<Props, State> {
    copyTimeoutId: ?TimeoutID

    static defaultProps = {
        permissionTypeVisible: false,
    }

    state = {
        showKey: null,
        copied: false,
        permissionSelectorValue: 'read',
    }

    onShowKey = (key: Key) => {
        this.setState({
            showKey: key,
        })
    }

    onHideKey = () => {
        this.setState({
            showKey: false,
        })
    }

    onSubmit = (e: OnSubmitEvent) => {
        e.preventDefault()
        const data = serialize(e.target, {
            hash: true,
        })
        this.props.addKey(data)
        e.target.reset()
    }

    componentWillUnmount() {
        this.copyTimeoutId && clearTimeout(this.copyTimeoutId)
    }

    onCopy = (id: $ElementType<Key, 'id'>) => {
        this.setState({
            copied: id,
        })
        this.copyTimeoutId && clearTimeout(this.copyTimeoutId)
        this.copyTimeoutId = setTimeout(() => {
            this.setState({
                copied: false,
            })
        }, 3000)
    }

    onPermissionSelect = (value: string) => {
        this.setState({
            permissionSelectorValue: value,
        })
    }

    renderKey = (key: Key) => {
        return (
            <tr key={key.id} className={styles.key}>
                <td>{key.name}</td>
                {this.props.permissionTypeVisible && <td>{key.permission}</td>}
                <td className={styles.actionColumn}>
                    <div className={styles.actionButtonContainer}>
                        <CopyToClipboard
                            text={key.id}
                            onCopy={() => this.onCopy(key.id)}
                        >
                            <Button>
                                <FontAwesome name={this.state.copied === key.id ? 'check' : 'copy'}/>
                            </Button>
                        </CopyToClipboard>
                        <Button onClick={() => this.onShowKey(key)}>
                            <FontAwesome name="eye"/>
                        </Button>
                        <ConfirmButton
                            confirmMessage={`Are you sure you want to remove key ${key.name}?`}
                            confirmCallback={() => this.props.removeKey(key.id)}
                            buttonProps={{
                                bsStyle: 'danger',
                            }}
                        >
                            <FontAwesome name="trash-o"/>
                        </ConfirmButton>
                    </div>
                </td>
            </tr>
        )
    }

    render() {
        return (
            <form onSubmit={this.onSubmit} className={styles.credentialsControl}>
                <Table>
                    <thead>
                        <tr>
                            <th>Name</th>
                            {this.props.permissionTypeVisible && <th>Permission</th>}
                            <th/>
                        </tr>
                    </thead>
                    <tbody>
                        {this.props.keys.map(this.renderKey)}
                    </tbody>
                </Table>
                <div className={styles.inputContainer}>
                    <InputGroup>
                        <FormControl
                            name="name"
                            placeholder="Key name"
                            required
                        />
                        {this.props.permissionTypeVisible && (
                            <InputGroup.Button className={styles.permissionSelector}>
                                <Select
                                    name="permission"
                                    options={[{
                                        value: 'read',
                                        label: 'Can Read',
                                    }, {
                                        value: 'write',
                                        label: 'Can Write',
                                    }, {
                                        value: 'share',
                                        label: 'Can Share',
                                    }]}
                                    value={this.state.permissionSelectorValue}
                                    onChange={this.onPermissionSelect}
                                    clearable={false}
                                    searchable={false}
                                    autosize={false}
                                    required
                                />
                            </InputGroup.Button>
                        )}
                        <InputGroup.Button>
                            <Button className={styles.addButton} type="submit">
                                <FontAwesome name="plus"/>
                            </Button>
                        </InputGroup.Button>
                    </InputGroup>
                </div>
                <Modal show={Boolean(this.state.showKey)} onHide={this.onHideKey}>
                    <Modal.Header closeButton>
                        <Modal.Title>
                            Key {this.state.showKey && this.state.showKey.name}
                        </Modal.Title>
                    </Modal.Header>
                    <Modal.Body>{this.state.showKey && this.state.showKey.id}</Modal.Body>
                </Modal>
            </form>
        )
    }
}
