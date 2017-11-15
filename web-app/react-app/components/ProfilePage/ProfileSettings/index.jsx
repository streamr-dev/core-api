// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import moment from 'moment-timezone'
import Select from 'react-select'
import createLink from '../../../helpers/createLink'
import {Panel, Form, FormControl, FormGroup, ControlLabel, InputGroup, Button} from 'react-bootstrap'
import 'react-select/dist/react-select.css'

import {getCurrentUser, updateCurrentUserName, updateCurrentUserTimezone, saveCurrentUser} from '../../../actions/user'

import type {User} from '../../../flowtype/user-types'

export class ProfileSettings extends Component {
    
    props: {
        user: User,
        getCurrentUser: Function,
        updateCurrentUserName: Function,
        updateCurrentUserTimezone: Function,
        saveCurrentUser: Function
    }
    onSubmit: Function
    onNameChange: Function
    onTimezoneChange: Function
    
    constructor() {
        super()
        this.onNameChange = this.onNameChange.bind(this)
        this.onTimezoneChange = this.onTimezoneChange.bind(this)
        //this.onSubmit = this.onSubmit.bind(this)
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
    //onSubmit(e: Event) {
    //    e.preventDefault()
    //    this.props.saveCurrentUser(this.props.user)
    //}
    render() {
        const options = moment.tz.names().map(tz => ({
            value: tz,
            label: tz
        }))
        return (
            <Panel header="Profile Settings">
                <Form method="POST" action="update">
                    <FormGroup>
                        <ControlLabel>
                            Email
                        </ControlLabel>
                        <div>{this.props.user.username}</div>
                    </FormGroup>
        
                    <FormGroup>
                        <ControlLabel>
                            Password
                        </ControlLabel>
                        <div>
                            <a href={createLink('profile/changePwd')}>
                                Change Password
                            </a>
                        </div>
                    </FormGroup>
        
                    <FormGroup>
                        <ControlLabel>
                            Full Name
                        </ControlLabel>
                        <FormControl
                            name="name"
                            className="form-control"
                            value={this.props.user.name || ''}
                            onChange={this.onNameChange}
                            required
                        />
                    </FormGroup>
            
                    <FormGroup>
                        <ControlLabel htmlFor="timezone">
                            Timezone
                        </ControlLabel>
                        <Select
                            placeholder="Select timezone"
                            options={options}
                            value={this.props.user.timezone}
                            name="timezone"
                            onChange={this.onTimezoneChange}
                            required={true}
                            clearable={false}
                        />
                    </FormGroup>
                    
                    <FormGroup>
                        <InputGroup>
                            <Button
                                type="submit"
                                name="submit"
                                bsStyle="primary"
                                bsSize="lg"
                            >
                                Save
                            </Button>
                        </InputGroup>
                    </FormGroup>
                </Form>
            </Panel>
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
    },
    saveCurrentUser(user) {
        dispatch(saveCurrentUser(user))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(ProfileSettings)