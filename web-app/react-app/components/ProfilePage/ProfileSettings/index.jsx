// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import moment from 'moment-timezone'
import Select from 'react-select'
import 'react-select/dist/react-select.css'

import {getCurrentUser, updateCurrentUserName, updateCurrentUserTimezone} from '../../../actions/user'

import type {User} from '../../../types/user-types'

export class ProfileSettings extends Component {
    
    props: {
        user: User,
        getCurrentUser: Function,
        updateCurrentUserName: Function,
        updateCurrentUserTimezone: Function
    }
    onNameChange: Function
    onTimezoneChange: Function
    
    constructor() {
        super()
        this.onNameChange = this.onNameChange.bind(this)
        this.onTimezoneChange = this.onTimezoneChange.bind(this)
    }
    componentDidMount() {
        this.props.getCurrentUser()
    }
    onNameChange({target}: {target: any}) {
        this.props.updateCurrentUserName(target.value)
    }
    onTimezoneChange({target}: {target: any}) {
        this.props.updateCurrentUserTimezone(target.value)
    }
    render() {
        const options = moment.tz.names().map(tz => ({
            value: tz,
            label: tz
        }))
        return (
            <div className="panel ">
                <form method="POST" action="update">
                    <div className="panel-heading">
                        <span className="panel-title">Profile Settings</span>
                    </div>
                    <div className="panel-body">
                        <div className="form-group ">
                            <label className="control-label">Email</label>
                            <div>{this.props.user.username}</div>
                        </div>
            
                        <div className="form-group">
                            <label className="control-label">Password</label>
                            <div>
                                <a href="/unifina-core/profile/changePwd">Change Password</a>
                            </div>
                        </div>
            
                        <div className="form-group ">
                            <label className="control-label">Full Name</label>
                            <input name="name" type="text" className="form-control" value={this.props.user.name || ''} onChange={this.onNameChange} required />
                        </div>
                
                        <div className="form-group ">
                            <label htmlFor="timezone" className="control-label">Timezone</label>
                            <Select
                                placeholder="Select timezone"
                                options={options}
                                value={this.props.user.timezone}
                                name="timezone"
                                onChange={this.onTimezoneChange}
                                required={true}
                                clearable={false}
                            />
                        </div>
                        <div className="form-group">
                            <input type="submit" name="submit" className="save btn btn-lg btn-primary" value="Save" id="submit" />
                        </div>
                    </div>
                </form>
            </div>
        )
    }
}

const mapStateToProps = ({user}) => ({
    user: user.currentUser || {}
})

const mapDispatchToProps = (dispatch) => ({
    getCurrentUser() {
        dispatch(getCurrentUser())
    },
    updateCurrentUserName(name) {
        dispatch(updateCurrentUserName(name))
    },
    updateCurrentUserTimezone(tz) {
        dispatch(updateCurrentUserTimezone(tz))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(ProfileSettings)