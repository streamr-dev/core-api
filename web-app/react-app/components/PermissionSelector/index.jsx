//// @flow
//
//import React, {Component} from 'react'
//import {connect} from 'react-redux'
//import {Modal, Button} from 'react-bootstrap'
//import ShareDialogContent from './ShareDialogContent'
//
//class ShareDialog extends Component {
//    openModal: Function
//    closeModal: Function
//    state: {
//        open: boolean,
//        isPublic: boolean
//    }
//    props: {
//        children: any,
//        resourceTitle: string
//    }
//
//    constructor() {
//        super()
//        this.state = {
//            open: false,
//            isPublic: false
//        }
//        this.openModal = this.openModal.bind(this)
//        this.closeModal = this.closeModal.bind(this)
//    }
//
//    openModal() {
//        this.setState({
//            open: true
//        })
//    }
//
//    closeModal() {
//        this.setState({
//            open: false
//        })
//    }
//
//    render() {
//        const Child = React.Children.only(this.props.children)
//        // TODO: Better key
//        let childsChildren = React.Children.map(Child.props.children, c => React.isValidElement(c) ? React.cloneElement(c, {
//            key: Math.random()
//        }) : c) || []
//        childsChildren.push(
//            <Modal
//                key={Math.random()}
//                show={this.state.open}
//                onHide={this.closeModal}
//            >
//                <Modal.Header closeButton>
//                    <Modal.Title>Share {this.props.resourceTitle}</Modal.Title>
//                </Modal.Header>
//                <Modal.Body>
//
//                </Modal.Body>
//                <Modal.Footer>
//                    <Button
//                        bsStyle="primary"
//                        onClick={this.closeModal}
//                    >
//                        Save
//                    </Button>
//                    <Button
//                        bsStyle="default"
//                        onClick={this.closeModal}
//                    >
//                        Close
//                    </Button>
//                </Modal.Footer>
//            </Modal>
//        )
//        return React.cloneElement(Child, {
//            onClick: this.openModal
//        }, childsChildren)
//    }
//}
//
//export default connect()(ShareDialog)