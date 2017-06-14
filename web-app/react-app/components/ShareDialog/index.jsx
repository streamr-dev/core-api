// @flow

import React, {Component} from 'react'
import {Modal, Button} from 'react-bootstrap'
import ShareDialogContent from './ShareDialogContent'

import type {Permission} from '../../flowtype/permission-types'

export default class ShareDialog extends Component {
    openModal: Function
    closeModal: Function
    state: {
        open: boolean
    }
    props: {
        resourceId: Permission.resourceId,
        resourceType: Permission.resourceType,
        resourceTitle: string,
        children?: any
    }
    
    constructor() {
        super()
        this.state = {
            open: false
        }
        this.openModal = this.openModal.bind(this)
        this.closeModal = this.closeModal.bind(this)
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
    
    render() {
        const Child = React.Children.only(this.props.children)
        // TODO: Better key
        let i = 0
        let childsChildren = React.Children.map(Child.props.children, (c, i) => {
            const el = React.isValidElement(c) ? React.cloneElement(c, {
                key: i
            }) : c
            i++
            return el
        }) || []
        childsChildren.push(
            <Modal
                //animation={false}
                key={i}
                show={this.state.open}
                onHide={this.closeModal}
                backdrop="static"
            >
                <Modal.Header closeButton>
                    <Modal.Title>Share {this.props.resourceTitle}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <ShareDialogContent resourceTitle={this.props.resourceTitle} resourceType={this.props.resourceType} resourceId={this.props.resourceId} />
                </Modal.Body>
                <Modal.Footer>
                    <Button
                        bsStyle="primary"
                        onClick={this.closeModal}
                    >
                        Save
                    </Button>
                    <Button
                        bsStyle="default"
                        onClick={this.closeModal}
                    >
                        Close
                    </Button>
                </Modal.Footer>
            </Modal>
        )
        return React.cloneElement(Child, {
            onClick: this.openModal
        }, childsChildren)
    }
}