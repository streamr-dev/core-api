// @flow

import React, {Component} from 'react'
import {Button, Modal} from 'react-bootstrap'

import type {Element, ReactChildren} from 'react-flow-types'

export default class ConfirmButton extends Component {
    openModal: Function
    closeModal: Function
    closeAndExecuteFunction: Function
    state: {
        open: boolean
    }
    
    props: {
        confirmCallback: Function,
        cancelCallback?: Function,
        buttonRef?: Function,
        confirmTitle?: string | Element<any>,
        confirmMessage: string | Element<any>,
        children?: ReactChildren,
        modalProps?: {},
        buttonProps?: {},
        className?: string
    }
    
    static defaultProps = {
        confirmTitle: 'Are you sure?',
        cancelCallback: () => {}
    }
    
    constructor() {
        super()
        
        this.state = {
            open: false
        }
        
        this.openModal = this.openModal.bind(this)
        this.closeModal = this.closeModal.bind(this)
        this.closeAndExecuteFunction = this.closeAndExecuteFunction.bind(this)
    }
    
    openModal() {
        this.setState({
            open: true
        })
    }
    
    closeModal() {
        this.setState({
            open: false
        })
    }
    
    closeAndExecuteFunction(func: () => void) {
        return () => {
            this.closeModal()
            func()
        }
    }
    
    render() {
        return (
            <Button {...this.props.buttonProps} onClick={this.openModal} ref={this.props.buttonRef} className={this.props.className}>
                {this.props.children}
                <Modal {...this.props.modalProps} show={this.state.open}>
                    <Modal.Header closeButton>
                        <Modal.Title>
                            {this.props.confirmTitle}
                        </Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        {this.props.confirmMessage}
                    </Modal.Body>
                    <Modal.Footer>
                        <Button onClick={this.closeAndExecuteFunction(this.props.cancelCallback)}>
                            Cancel
                        </Button>
                        <Button onClick={this.closeAndExecuteFunction(this.props.confirmCallback)} bsStyle="primary">
                            OK
                        </Button>
                    </Modal.Footer>
                </Modal>
            </Button>
        )
    }
}