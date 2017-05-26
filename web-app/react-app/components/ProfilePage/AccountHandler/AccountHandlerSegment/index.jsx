// @flow

import React from 'react'

import { connect } from 'react-redux'
import { getAccountsByType, createAccount, deleteAccount } from '../../../../actions/accounts'

import {Col, ControlLabel} from 'react-bootstrap'

import StreamrAccountHandlerInput from '../AccountHandlerInput'
import StreamrAccountHandlerTable from '../AccountHandlerTable'

import styles from './accountHandlerSegment.pcss'

declare var Streamr: any

class StreamrAccountHandlerSegment extends React.Component {
    
    props: {
        tableFields: Array<string>,
        inputFields: Array<string>,
        accounts: Array<{
            id: string,
            name: string,
            json: {}
        }>,
        type: string,
        name: string,
        className: string,
        dispatch: Function
    }
    
    onNew: Function
    onDelete: Function
    
    constructor(props) {
        super(props)
        
        this.onNew = this.onNew.bind(this)
        this.onDelete = this.onDelete.bind(this)
    }
    componentDidMount() {
        this.props.dispatch(getAccountsByType(this.props.type))
    }
    
    onNew(account) {
        const name = account.name
        const type = this.props.type
        delete account.name
        return this.props.dispatch(createAccount({
            name,
            type,
            json: account
        }))
            .then(() => Streamr.showSuccess('Account created successfully!'))
            .catch(Streamr.showError)
    }
    
    onDelete(id) {
        this.props.dispatch(deleteAccount(id))
            .then(() => Streamr.showSuccess('Account removed successfully!'))
    }
    
    render() {
        return (
            <div className={this.props.className}>
                <Col xs={12}>
                    <ControlLabel className={styles.label}>
                        {this.props.name}
                    </ControlLabel>
                    <StreamrAccountHandlerTable
                        fields={this.props.tableFields}
                        accounts={this.props.accounts}
                        onDelete={this.onDelete}
                    />
                    <StreamrAccountHandlerInput
                        fields={this.props.inputFields}
                        onNew={this.onNew}
                    />
                </Col>
            </div>
        )
    }
}

const mapStateToProps = ({account}, props) => ({
    accounts: account.listsByType[props.type] || [],
    error: account.error
})

export default connect(mapStateToProps)(StreamrAccountHandlerSegment)