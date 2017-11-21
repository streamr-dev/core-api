// @flow

import React, {Component} from 'react'
import {Breadcrumb, DropdownButton} from 'react-bootstrap'
import FontAwesome from 'react-fontawesome'

import type {ReactChildren} from 'react-flow-types'

import styles from './breadcrumb.pcss'

export class StreamrBreadcrumb extends Component {
    props: {
        children?: ReactChildren
    }
    render() {
        return (
            <Breadcrumb className={`${styles.breadcrumb} breadcrumb-page`}>
                {this.props.children}
            </Breadcrumb>
        )
    }
}

export class StreamrBreadcrumbItem extends Component {
    props: {
        href?: string,
        active?: boolean,
        children?: ReactChildren
    }
    render() {
        return (
            <Breadcrumb.Item {...this.props} href={!this.props.active ? this.props.href : undefined}>
                {this.props.children}
            </Breadcrumb.Item>
        )
    }
}

export class StreamrBreadcrumbDropdownButton extends Component {
    props: {
        className?: string,
        children?: ReactChildren
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

export class StreamrBreadcrumbToolbar extends Component {
    props: {
        children?: ReactChildren
    }
    render() {
        return (
            <div className={styles.toolbar}>
                {this.props.children}
            </div>
        )
    }
}

export class StreamrBreadcrumbToolbarButton extends Component {
    props: {
        iconName: string,
        onClick: Function
    }
    render() {
        return (
            <div className={styles.button} onClick={this.props.onClick}>
                <FontAwesome name={this.props.iconName}/>
            </div>
        )
    }
}