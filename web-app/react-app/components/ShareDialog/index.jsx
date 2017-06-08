// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {Modal, Button} from 'react-bootstrap'
import ShareDialogContent from './ShareDialogContent'

class ShareDialog extends Component {
    openModal: Function
    closeModal: Function
    state: {
        open: boolean,
        isPublic: boolean,
        list: Array<any>
    }
    props: {
        children: any,
        resourceTitle: string,
        resourceId: string,
        resourceType: 'DASHBOARD' | 'CANVAS' | 'STREAM'
    }
    
    constructor() {
        super()
        this.state = {
            open: false,
            isPublic: false,
            list: []
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
        let childsChildren = React.Children.map(Child.props.children, c => React.isValidElement(c) ? React.cloneElement(c, {
            key: Math.random()
        }) : c) || []
        childsChildren.push(
            <Modal
                animation={false}
                key={Math.random()}
                show={this.state.open}
                onHide={this.closeModal}
            >
                <Modal.Header closeButton>
                    <Modal.Title>Share {this.props.resourceTitle}</Modal.Title>
                </Modal.Header>
                <Modal.Body>
                    <ShareDialogContent isPublic={this.state.isPublic} list={this.state.list} onIsPublicChange={() => this.setState({
                        isPublic: !this.state.isPublic,
                    })} onPush={item => this.setState({
                        list: [...this.state.list, item]
                    })}/>
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

export default connect()(ShareDialog)