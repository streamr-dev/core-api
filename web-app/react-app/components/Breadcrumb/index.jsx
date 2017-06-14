// @flux

import React, {Component} from 'react'
import {Breadcrumb, DropdownButton} from 'react-bootstrap'

import styles from './breadcrumb.pcss'

export class StreamrBreadcrumb extends Component {
    props: {
        children?: Array<any>
    }
    render() {
        return (
            <Breadcrumb className="breadcrumb-page">
                {this.props.children}
            </Breadcrumb>
        )
    }
}

StreamrBreadcrumb.Item = class StreamrBreadcrumbItem extends Component {
    props: {
        href?: string,
        active?: boolean,
        children: string
    }
    render() {
        return (
            <Breadcrumb.Item {...this.props} href={!this.props.active ? this.props.href : undefined}>
                {this.props.children}
            </Breadcrumb.Item>
        )
    }
}

StreamrBreadcrumb.DropdownButton = class StreamrBreadcrumbDropdown extends Component {
    props: {
        className: string,
        children: any,
        title: any
    }
    render() {
        return (
            <div className={styles.streamrDropdownContainer}>
                <DropdownButton id={`streamrDropdownButton-${Date.now()}`} {...this.props} bsSize="xs" className={`${this.props.className || ''} ${styles.streamrDropdownButton}`}>
                    {this.props.children}
                </DropdownButton>
            </div>
        )
    }
}