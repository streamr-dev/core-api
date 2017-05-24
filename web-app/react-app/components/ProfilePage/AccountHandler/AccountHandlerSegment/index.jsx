/* globals Streamr */
import React from 'react'
import {string, array, func} from 'prop-types'

import { connect } from 'react-redux'
import { getAllAccounts, createAccount, deleteAccount } from '../../../../actions/accounts'

import {Col, ControlLabel} from 'react-bootstrap'

import StreamrAccountHandlerInput from '../AccountHandlerInput'
import StreamrAccountHandlerTable from '../AccountHandlerTable'

import styles from './accountHandlerSegment.pcss'

class StreamrAccountHandlerSegment extends React.Component {
    constructor(props) {
        super(props)
        
        this.onNew = this.onNew.bind(this)
        this.onDelete = this.onDelete.bind(this)
    }
    componentDidMount() {
        this.props.dispatch(getAllAccounts())
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

StreamrAccountHandlerSegment.propTypes = {
    tableFields: array,
    inputFields: array,
    accounts: array,
    type: string,
    name: string,
    className: string,
    dispatch: func
}

const mapStateToProps = ({account}) => ({
    accounts: account.list,
    error: account.error
})

export default connect(mapStateToProps)(StreamrAccountHandlerSegment)