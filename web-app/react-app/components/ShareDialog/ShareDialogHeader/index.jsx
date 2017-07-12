// @flow
import React from 'react'
import {Modal} from 'react-bootstrap'

import type {Permission} from '../../../flowtype/permission-types'

export default function ShareDialogHeader(props: {
    resourceTitle: Permission.resourceTitle
}) {
    return (
        <Modal.Header closeButton>
            <Modal.Title>Share {props.resourceTitle}</Modal.Title>
        </Modal.Header>
    )
}
