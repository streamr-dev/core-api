
import React from 'react'
import {string, array, func} from 'prop-types'

import { connect } from 'react-redux'
import { getAllAccounts, createAccount, deleteAccount } from '../../../../actions/accounts'

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
        this.props.dispatch(createAccount({
            name,
            type,
            json: account
        }))
    }
    
    onDelete(id) {
        this.props.dispatch(deleteAccount(id))
    }
    
    render() {
        return (
            <div className={this.props.className}>
                <div className="col-xs-12">
                    <label className={styles.label}>
                        {this.props.name}
                    </label>
                    <StreamrAccountHandlerTable fields={this.props.fields} accounts={this.props.accounts} onDelete={this.onDelete}/>
                    <StreamrAccountHandlerInput fields={this.props.fields} onNew={this.onNew}/>
                </div>
            </div>
        )
    }
}

StreamrAccountHandlerSegment.propTypes = {
    fields: array,
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