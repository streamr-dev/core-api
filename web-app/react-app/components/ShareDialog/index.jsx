// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {Modal} from 'react-bootstrap'
import ShareDialogHeader from './ShareDialogHeader'
import ShareDialogContent from './ShareDialogContent'
import ShareDialogFooter from './ShareDialogFooter'

import {saveUpdatedResourcePermissions} from '../../actions/permission'

import type {Node} from 'react'
import type {Permission, State as PermissionState} from '../../flowtype/permission-types'

type Props = {
    resourceId: Permission.resourceId,
    resourceType: Permission.resourceType,
    resourceTitle: string,
    children?: Node,
    save: Function,
    isOpen: boolean,
    onClose: () => void
}

export class ShareDialog extends Component<Props> {
    
    save = () => {
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

export const mapStateToProps = ({permission}: {permission: PermissionState}, ownProps: Props) => ({
    permissions: permission.byTypeAndId[ownProps.resourceType] && permission.byTypeAndId[ownProps.resourceType][ownProps.resourceId] || []
})

export const mapDispatchToProps = (dispatch: Function, ownProps: Props) => ({
    save() {
        return dispatch(saveUpdatedResourcePermissions(ownProps.resourceType, ownProps.resourceId))
    }
})

export default connect (mapStateToProps, mapDispatchToProps)(ShareDialog)