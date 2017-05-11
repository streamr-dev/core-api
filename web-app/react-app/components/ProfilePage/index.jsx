
import React, {Component} from 'react'
import ProfileSettings from './ProfileSettings'
import APICredentials from '../APICredentials'
import AccountHandler from './AccountHandler'

export default class ProfilePage extends Component {
    render() {
        return (
            <div className="row">
                <div className="col-sm-6 col-md-offset-2 col-md-4">
                    <ProfileSettings />
                </div>
                <div className="col-sm-6 col-md-4">
                    <APICredentials />
                </div>
                <div className="col-sm-6 col-md-4">
                    <AccountHandler />
                </div>
            </div>
        )
    }
}