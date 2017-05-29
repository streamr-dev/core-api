/* global Streamr */

import React, {Component} from 'react'
import moment from 'moment-timezone'
import axios from 'axios'
import Select from 'react-select'
import 'react-select/dist/react-select.css'

export default class ProfileSettings extends Component {
    constructor() {
        super()
        this.state = {
            user: {
                username: Streamr.user,
                name: '',
                timezone: ''
            }
        }
        this.onNameChange = this.onNameChange.bind(this)
        this.onTimezoneChange = this.onTimezoneChange.bind(this)
    }
    componentDidMount() {
        axios.get(Streamr.createLink({
            uri: 'api/v1/users/me'
        })).then(({data}) => {
            this.setState({
                user: data
            })
        })
    }
    onNameChange(e) {
        this.setState({
            user: {
                ...this.state.user,
                name: e.target.value
            }
        })
    }
    onTimezoneChange(selected) {
        this.setState({
            user: {
                ...this.state.user,
                timezone: selected.value
            }
        })
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
                            <div>{this.state.user.username}</div>
                        </div>
            
                        <div className="form-group">
                            <label className="control-label">Password</label>
                            <div>
                                <a href="/unifina-core/profile/changePwd">Change Password</a>
                            </div>
                        </div>
            
                        <div className="form-group ">
                            <label className="control-label">Full Name</label>
                            <input name="name" type="text" className="form-control" value={this.state.user.name} onChange={this.onNameChange} required />
                        </div>
                
                        <div className="form-group ">
                            <label htmlFor="timezone" className="control-label">Timezone</label>
                            <Select
                                placeholder="Select timezone"
                                options={options}
                                value={this.state.user.timezone}
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