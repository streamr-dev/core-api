// @flow

import React, {Component} from 'react'

import {FormControl} from 'react-bootstrap'

export default class NameEditor extends Component {
    
    removeButton: HTMLButtonElement
    
    props: {
        name: ?string,
        onChange: Function
    }
    
    render() {
        return (
            <div className="menu-content">
                <label>Dashboard name</label>
                <FormControl
                    type="text"
                    className="dashboard-name title-input"
                    name="dashboard-name"
                    placeholder="Dashboard name"
                    defaultValue={this.props.name || ''}
                    onChange={this.props.onChange}
                />
            </div>
        )
    }
}