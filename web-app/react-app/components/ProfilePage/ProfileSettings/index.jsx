// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import moment from 'moment-timezone'
import Select from 'react-select'
import createLink from '../../../helpers/createLink'
import {Panel, Form, FormControl, FormGroup, ControlLabel, InputGroup, Button} from 'react-bootstrap'
import 'react-select/dist/react-select.css'

import {getCurrentUser, updateCurrentUserName, updateCurrentUserTimezone, saveCurrentUser} from '../../../actions/user'

import type {UserState} from '../../../flowtype/states/user-state'
import type {User} from '../../../flowtype/user-types'

type StateProps = {
    user: User
}

type DispatchProps = {
    getCurrentUser: () => void,
    updateCurrentUserName: (name: $ElementType<User, 'name'>) => void,
    updateCurrentUserTimezone: (timezone: $ElementType<User, 'timezone'>) => void,
    saveCurrentUser: (user: User) => void
}

type Props = StateProps & DispatchProps

export class ProfileSettings extends Component<Props> {
    
    componentDidMount() {
        // TODO: move to (yet nonexistent) router
        this.props.getCurrentUser()
    }
    onNameChange = ({target}: {target: {
        value: $ElementType<User, 'name'>
    }}) => {
        this.props.updateCurrentUserName(target.value)
    }
    onTimezoneChange = ({target}: {target: {
        value: $ElementType<User, 'timezone'>
    }}) => {
        this.props.updateCurrentUserTimezone(target.value)
    }
    onSubmit = (e: Event) => {
        e.preventDefault()
        this.props.saveCurrentUser(this.props.user)
    }
    render() {
        const options = moment.tz.names().map(tz => ({
            value: tz,
            label: tz
        }))
        return (
            <Panel header="Profile Settings">
                <Form onSubmit={this.onSubmit}>
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
                            value={this.props.user.name || ''}
                            onChange={this.onNameChange}
                            required
                        />
                    </FormGroup>
            
                    <FormGroup>
                        <ControlLabel>
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

export const mapStateToProps = ({user}: {user: UserState}) => ({
    user: user.currentUser || {}
})

export const mapDispatchToProps = (dispatch: Function) => ({
    getCurrentUser() {
        dispatch(getCurrentUser())
    },
    updateCurrentUserName(name: $ElementType<User, 'name'>) {
        dispatch(updateCurrentUserName(name))
    },
    updateCurrentUserTimezone(tz: $ElementType<User, 'timezone'>) {
        dispatch(updateCurrentUserTimezone(tz))
    },
    saveCurrentUser(user: User) {
        dispatch(saveCurrentUser(user))
    }
})

export default connect(mapStateToProps, mapDispatchToProps)(ProfileSettings)