
import React, {Component} from 'react'
import moment from 'moment-timezone'

debugger

export default class ProfileSettings extends Component {
    render() {
        return (
            <div className="panel ">
                <form method="POST" action="update">
                    <div className="panel-heading">
                        <span className="panel-title">Profile Settings</span>
                    </div>
                    <div className="panel-body">
                        <div className="form-group ">
                            <label className="control-label">
                                Email
                            </label>
                            <div>
                                tester1@streamr.com
                            </div>
                        </div>
            
                        <div className="form-group">
                            <label className="control-label">
                                Password
                            </label>
                            <div>
                                <a href="/unifina-core/profile/changePwd">
                                    Change Password
                                </a>
                            </div>
                        </div>
            
                        <div className="form-group ">
                            <label className="control-label">
                                Full Name
                            </label>
                            <input name="name" type="text" className="form-control" value="Tester One" required="" />
                
                        </div>
                
                        <div className="form-group ">
                            <label htmlFor="timezone" className="control-label">
                                Timezone
                            </label>
                            <select name="timezone" id="timezone" className="form-control">
                                {moment.tz.names().map(tz => {
                                    <option value={tz}>{tz}</option>
                                })}
                            </select>
                
                        </div>
                        <div className="col-sm-12">
                            <input type="submit" name="submit" className="save btn btn-lg btn-primary" value="Save" id="submit" />
                        </div>
                    </div>
                </form>
            </div>
        )
    }
}