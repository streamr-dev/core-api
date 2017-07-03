// @flow
import React from 'react'
import {Modal} from 'react-bootstrap'

export default function ShareDialogHeader(props) {
    return (
        <Modal.Header closeButton>
            <Modal.Title>Share {props.resourceTitle}</Modal.Title>
        </Modal.Header>
    )
}
