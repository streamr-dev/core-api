// @flow
import React from 'react'
import {Button, Modal} from 'react-bootstrap'

export default function ShareDialogFooter (props) {
    return (
        <Modal.Footer>
            <Button
                bsStyle="primary"
                onClick={props.save}
            >
                Save
            </Button>
            <Button
                bsStyle="default"
                onClick={props.closeModal}
            >
                Close
            </Button>
        </Modal.Footer>
    )
}