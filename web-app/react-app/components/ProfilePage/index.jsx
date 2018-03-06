// @flow

import React, {Component} from 'react'

import {Col, Row} from 'react-bootstrap'

import ProfileSettings from './ProfileSettings'
import APICredentials from './APICredentials'
import IntegrationKeyHandler from './IntegrationKeyHandler'
import Notifier from '../StreamrNotifierWrapper'
import IdentityHandler from './IdentityHandler/index'

export default class ProfilePage extends Component<{}> {
    render() {
        return (
            <Row>
                <Notifier/>
                <Col xs={12} sm={6}>
                    <ProfileSettings/>
                </Col>
                <Col xs={12} sm={6}>
                    <APICredentials/>
                    <IdentityHandler/>
                    <IntegrationKeyHandler/>
                </Col>
            </Row>
        )
    }
}
