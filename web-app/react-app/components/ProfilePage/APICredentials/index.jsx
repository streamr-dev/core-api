// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {Panel} from 'react-bootstrap'
import CredentialsControl from '../../CredentialsControl'
import {getResourceKeys, addResourceKey, removeResourceKey} from '../../../actions/key'

import type {Key, State as KeyReducerState} from '../../../flowtype/key-types'

type Props = {
    keys: Array<Key>,
    getKeys: () => void,
    addKey: (key: Key) => void,
    removeKey: (keyId: Key.id) => void,
    error: string
}

export class APICredentials extends Component<Props> {
    
    componentWillMount() {
        this.props.getKeys()
    }
    
    render() {
        const keys = this.props.keys.sort((a, b) => a.name.localeCompare(b.name))
        return (
            <Panel header="API Credentials">
                <CredentialsControl
                    keys={keys}
                    addKey={this.props.addKey}
                    removeKey={this.props.removeKey}
                    error={this.props.error}
                    permissionTypeVisible={false}
                />
            </Panel>
        )
    }
}

const mapStateToProps = ({key}: {key: KeyReducerState}) => ({
    keys: (key.byTypeAndId['USER'] || {})['me'] || [],
    error: key.error
})

const mapDispatchToProps = (dispatch: Function) => ({
    getKeys() {
        dispatch(getResourceKeys('USER', 'me'))
    },
    addKey(key: Key) {
        dispatch(addResourceKey('USER', 'me', key))
    },
    removeKey(keyId: Key.id) {
        dispatch(removeResourceKey('USER', 'me', keyId))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(APICredentials)