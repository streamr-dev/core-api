// @flow
import React, {Component} from 'react'
import {Button, Modal} from 'react-bootstrap'

type Props = {
    save: Function,
    closeModal: Function
}

import styles from './shareDialogFooter.pcss'

export default class ShareDialogFooter extends Component<Props> {
    render() {
        return (
            <Modal.Footer>
                <Button
                    bsStyle="default"
                    onClick={this.props.closeModal}
                    className={styles.cancelButton}
                >
                    Cancel
                </Button>
                <Button
                    bsStyle="primary"
                    onClick={this.props.save}
                    className={styles.saveButton}
                >
                    Save
                </Button>
            </Modal.Footer>
        )
    }
}