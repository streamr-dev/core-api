// @flow

import React, {Component} from 'react'
import {Button} from 'react-bootstrap'
import FontAwesome from 'react-fontawesome'

declare var ConfirmButton: Function

export default class DashboardTools extends Component {
    componentDidMount() {
        new ConfirmButton(this.removeButton, {
            title: 'Are you sure?',
            message: `Are you sure you want to remove dashboard key ${this.props.dashboard.name}?`
        }, res => {
            if (res) {
                this.onDelete(id)
            }
        })
    }
    onDelete() {
    
    }
    render() {
        return (
            <div className="menu-content">
                <Button
                    block
                    className="save-button"
                    title="Save dashboard"
                    bsStyle="primary"
                >
                    Save
                </Button>
                <Button
                    block
                    className="share-button"
                    onclick={() => {}}
                >
                    <FontAwesome name="user" />  Share
                </Button>
                <Button
                    block
                    bsStyle="default"
                    className="delete-button"
                    title="Delete dashboard"
                    inputRef={item => this.removeButton = item}
                >
                    Delete
                </Button>
            </div>
        )
    }
}