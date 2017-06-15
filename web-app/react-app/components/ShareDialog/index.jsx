// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {Modal, Button} from 'react-bootstrap'
import ShareDialogContent from './ShareDialogContent'

import {saveUpdatedResourcePermissions} from '../../actions/permission'

import type {ReactChildren} from 'react-flow-types'
import type {Permission} from '../../flowtype/permission-types'

class ShareDialog extends Component {
    openModal: Function
    closeModal: Function
    save: Function
    state: {
        open: boolean
    }
    props: {
        resourceId: Permission.resourceId,
        resourceType: Permission.resourceType,
        resourceTitle: string,
        children?: ReactChildren,
        save: Function
    }
    
    constructor() {
        super()
        this.state = {
            open: false
        }
        this.openModal = this.openModal.bind(this)
        this.closeModal = this.closeModal.bind(this)
        this.save = this.save.bind(this)
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
    
    save() {
        this.props.save()
            .then(() => {
                this.closeModal()
            })
    }
    
    render() {
        const Child = React.Children.only(this.props.children)
        let i = 0
        let childsChildren = React.Children.map(Child.props.children, c => {
            const el = React.isValidElement(c) ? React.cloneElement(c, {
                key: i
            }) : c
            i++
            return el
        }) || []
        childsChildren.push(
            <Modal
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
                        onClick={this.save}
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

const mapStateToProps = ({permission}, ownProps) => ({
    permissions: permission.byTypeAndId[ownProps.resourceType] && permission.byTypeAndId[ownProps.resourceType][ownProps.resourceId] || []
})

const mapDispatchToProps = (dispatch, ownProps) => ({
    save() {
        return dispatch(saveUpdatedResourcePermissions(ownProps.resourceType, ownProps.resourceId))
    }
})

export default connect (mapStateToProps, mapDispatchToProps)(ShareDialog)