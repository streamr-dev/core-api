// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import {Modal} from 'react-bootstrap'
import ShareDialogHeader from './ShareDialogHeader'
import ShareDialogContent from './ShareDialogContent'
import ShareDialogFooter from './ShareDialogFooter'

import {saveUpdatedResourcePermissions} from '../../actions/permission'

import type {Node} from 'react'
import type {ResourceType, ResourceId} from '../../flowtype/permission-types'

type DispatchProps = {
    save: () => Promise<void>
}

type GivenProps = {
    resourceId: ResourceId,
    resourceType: ResourceType,
    resourceTitle: string,
    children?: Node,
    isOpen: boolean,
    onClose: () => void
}

type Props = DispatchProps & GivenProps

export class ShareDialog extends Component<Props> {

    save = () => {
        this.props.save()
            .then(() => this.props.onClose())
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
                    onClose={this.save}
                />
                <ShareDialogFooter
                    save={this.save}
                    closeModal={this.props.onClose}
                />
            </Modal>
        )
    }
}

export const mapDispatchToProps = (dispatch: Function, ownProps: GivenProps): DispatchProps => ({
    save() {
        return dispatch(saveUpdatedResourcePermissions(ownProps.resourceType, ownProps.resourceId))
    }
})

export default connect (null, mapDispatchToProps)(ShareDialog)
