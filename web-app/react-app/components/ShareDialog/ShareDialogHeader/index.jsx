// @flow
import React, {Component} from 'react'
import {Modal} from 'react-bootstrap'

type Props = {
    resourceTitle: string
}

export default class ShareDialogHeader extends Component<Props> {
    render() {
        return (
            <Modal.Header closeButton>
                <Modal.Title>Share {this.props.resourceTitle}</Modal.Title>
            </Modal.Header>
        )
    }
}
