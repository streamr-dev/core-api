// @flow

import React, {Component} from 'react'
import {connect} from 'react-redux'
import Modal from 'react-modal'

class ShareDialog extends Component {
    openModal: Function
    closeModal: Function
    state: {
        open: boolean
    }
    props: {
        children: any
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
        return (
            <div>
                {React.Children.only(this.props.children, c => {
                    return React.cloneElement(c, {
                        onClick: this.openModal
                    })
                })}
                <Modal
                    isOpen={this.state.open}
                    onRequestClose={this.closeModal}
                    contentLabel="Modal"
                >
                    <h1>Modal Content</h1>
                    <p>Etc.</p>
                </Modal>
            </div>
        )
    }
}

export default connect()(ShareDialog)