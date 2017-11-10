// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {Modal} from 'react-bootstrap'
import ShareDialogHeader from './ShareDialogHeader'
import ShareDialogContent from './ShareDialogContent'
import ShareDialogFooter from './ShareDialogFooter'

import {saveUpdatedResourcePermissions} from '../../actions/permission'

import type {ReactChildren} from 'react-flow-types'
import type {Permission} from '../../flowtype/permission-types'

export class ShareDialog extends Component {
    save: Function
    props: {
        resourceId: Permission.resourceId,
        resourceType: Permission.resourceType,
        resourceTitle: string,
        children?: ReactChildren,
        save: Function,
        isOpen: boolean,
        onClose: () => void
    }
    
    constructor() {
        super()
        this.save = this.save.bind(this)
    }
    
    save() {
        this.props.save()
            .then(() => {
                this.props.onClose()
            })
    }
    
    render() {
        return (
            <Modal
                show={this.props.isOpen}
                onHide={this.props.onClose}
                backdrop="static"
            >
                <ShareDialogHeader
                    resourceTitle={this.props.resourceTitle}
                />
                <ShareDialogContent
                    resourceTitle={this.props.resourceTitle}
                    resourceType={this.props.resourceType}
                    resourceId={this.props.resourceId}
                />
                <ShareDialogFooter
                    save={this.save}
                    closeModal={this.props.onClose}
                />
            </Modal>
        )
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