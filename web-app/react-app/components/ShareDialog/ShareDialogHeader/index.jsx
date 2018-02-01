// @flow
import React, {Component} from 'react'
import {Modal} from 'react-bootstrap'

type GivenProps = {
    resourceTitle: string
}

type Props = GivenProps

export default class ShareDialogHeader extends Component<Props> {
    render() {
        return (
            <Modal.Header closeButton>
                <Modal.Title>Share {this.props.resourceTitle}</Modal.Title>
            </Modal.Header>
        )
    }
}
