// @flow

import React, {Component} from 'react'
import {Button, Modal} from 'react-bootstrap'

import type {Node} from 'react'

type Props = {
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
            <Button key="2"
                {...this.props.buttonProps}
                style={{
                    padding: 0
                }}
                ref={this.props.buttonRef}
                className={this.props.className}
            >
                <div
                    onClick={this.openModal}
                    style={{
                        padding: '7px 12px',
                        width: '100%',
                        height: '100%'
                    }}
                >
                    {this.props.children}
                </div>
                <Modal key="3" {...this.props.modalProps} show={this.state.open}>
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