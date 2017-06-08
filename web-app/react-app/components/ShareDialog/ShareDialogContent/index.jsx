// @flow

import React, {Component} from 'react'
import {Row, Col, Button} from 'react-bootstrap'
import Switcher from 'react-switcher'

import styles from './shareDialogContent.pcss'

export default class ShareDialogContent extends Component {
    input: HTMLInputElement
    props: {
        isPublic: boolean,
        onIsPublicChange: () => void,
        onPush: (any) => void,
        list: Array<any>
    }
    
    render() {
        return (
            <Row>
                <Col xs={12} className={styles.ownerRow}>
                    <div className={styles.ownerLabel}>
                        Owner:
                    </div>
                    <div className={styles.owner}>
                        <strong>tester1@streamr.com</strong>
                    </div>
                    <div className={styles.readAccessLabel}>
                        Public read access
                    </div>
                    <div className={styles.readAccess}>
                        <Switcher on={this.props.isPublic} onClick={this.props.onIsPublicChange}/>
                    </div>
                </Col>
                {this.props.list.map(i => <div key={i}>{i}</div>)}
                <Col xs={12}>
                    <div className="input-group">
                        <input type="text" className="new-user-field form-control"
                               placeholder="Enter email address" ref={i => this.input = i}/>
                        <span className="input-group-btn">
                            <Button className="new-user-button btn btn-default pull-right"
                                    onClick={() => this.props.onPush(this.input.value)}>
                                <span className="icon fa fa-plus"/>
                            </Button>
                        </span>
                    </div>
                </Col>
            </Row>
        )
    }
}