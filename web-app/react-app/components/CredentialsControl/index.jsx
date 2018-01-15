// @flow

import React, {Component} from 'react'
import {Table, Button, Modal} from 'react-bootstrap'
import {CopyToClipboard} from 'react-copy-to-clipboard'
import FontAwesome from 'react-fontawesome'
import ConfirmButton from '../ConfirmButton'

import type {Key} from '../../flowtype/key-types'

type Props = {
    keys: Array<Key>,
    addKey: (key: Key) => void,
    removeKey: (id: Key.id) => void,
    error?: ?string,
    permissionTypeVisible: boolean
}

type State = {
    showKey: ?Key
}

export default class CredentialsControl extends Component<Props, State> {
    static defaultProps = {
        permissionTypeVisible: false
    }
    state = {
        showKey: null
    }
    onShowKey = (key: Key) => {
        this.setState({
            showKey: key
        })
    }
    onHideKey = () => {
        this.setState({
            showKey: null
        })
    }
    renderKey = (key: Key) => {
        return (
            <tr key={key.id}>
                <td>{key.name}</td>
                {this.props.permissionTypeVisible && <td>{key.permission}</td>}
                <td>
                    <CopyToClipboard text={key.id}>
                        <Button>
                            <FontAwesome name="copy"/>
                        </Button>
                    </CopyToClipboard>
                    <Button onClick={() => this.onShowKey(key)}>
                        <FontAwesome name="eye"/>
                        <Modal show={this.state.showKey != null} onHide={this.onHideKey}>
                            <Modal.Header>
                                <Modal.Title>
                                    Key {this.state.showKey.name}
                                </Modal.Title>
                            </Modal.Header>
                            <Modal.Body>{this.state.showKey.id}</Modal.Body>
                        </Modal>
                    </Button>
                    <ConfirmButton successCallback={this.props.removeKey}>
                        <Button bsStyle="danger">
                            <FontAwesome name="trash-o"/>
                        </Button>
                    </ConfirmButton>
                </td>
            </tr>
        )
    }
    
    render() {
        return (
            <form onSubmit={this.onSubmit}>
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
            </form>
        )
    }
}