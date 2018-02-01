// @flow

import React, {Component} from 'react'
import {Button, Modal} from 'react-bootstrap'

import type {Node} from 'react'

type GivenProps = {
    confirmCallback: (any) => void,
    cancelCallback?: (any) => void,
    buttonRef?: Function,
    confirmTitle?: string | Component<any>,
    confirmMessage: string | Component<any>,
    children?: Node,
    modalProps?: {},
    buttonProps?: {},
    className?: string
}

type Props = GivenProps

type State = {
    open: boolean
}

export default class ConfirmButton extends Component<Props, State> {
    
    static defaultProps = {
        confirmTitle: 'Are you sure?',
        cancelCallback: () => {}
    }
    
    state = {
        open: false
    }
    
    openModal = () => {
        this.setState({
            open: true
        })
    }
    
    closeModal = () => {
        this.setState({
            open: false
        })
    }
    
    closeAndExecuteFunction = (func?: (any) => void) => {
        this.closeModal()
        if (func) {
            func()
        }
    }
    
    render() {
        return (
            <Button
                {...this.props.buttonProps}
                onClick={(e) => {
                    // An ugly fix to prevent clicking the modal from firing this
                    if (e.target === e.currentTarget) {
                        this.openModal()
                    }
                }}
                ref={this.props.buttonRef}
                className={this.props.className}
            >
                {this.props.children}
                <Modal {...this.props.modalProps} show={this.state.open}>
                    <Modal.Header>
                        <Modal.Title>
                            {this.props.confirmTitle}
                        </Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        {this.props.confirmMessage}
                    </Modal.Body>
                    <Modal.Footer>
                        <Button onClick={() => this.closeAndExecuteFunction(this.props.cancelCallback)}>
                            Cancel
                        </Button>
                        <Button onClick={() => this.closeAndExecuteFunction(this.props.confirmCallback)} bsStyle="primary">
                            OK
                        </Button>
                    </Modal.Footer>
                </Modal>
            </Button>
        )
    }
}