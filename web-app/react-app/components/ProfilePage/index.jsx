// @flow

import React, {Component} from 'react'

import { Row, Col } from 'react-bootstrap'

import ProfileSettings from './ProfileSettings'
import APICredentials from '../APICredentials'
import AccountHandler from './AccountHandler'

export default class ProfilePage extends Component {
    render() {
        return (
            <Row>
                <Col sm={6} md={4}>
                    <ProfileSettings />
                </Col>
                <Col sm={6} md={4}>
                    <APICredentials />
                </Col>
                <Col sm={6} md={4}>
                    <AccountHandler />
                </Col>
            </Row>
        )
    }
}