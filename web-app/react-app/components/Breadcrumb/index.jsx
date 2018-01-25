// @flow

import React, {Component} from 'react'
import {Breadcrumb, DropdownButton} from 'react-bootstrap'
import FontAwesome from 'react-fontawesome'

import type {Node} from 'react'

type Children = string | Node | Array<Node>

import styles from './breadcrumb.pcss'

export class StreamrBreadcrumb extends Component<{
    children?: Children
}> {
    render() {
        return (
            <Breadcrumb className={styles.breadcrumb}>
                {this.props.children}
            </Breadcrumb>
        )
    }
}

export class StreamrBreadcrumbItem extends Component<{
    href?: string,
    active?: boolean,
    children?: Children
}> {
    render() {
        return (
            <Breadcrumb.Item {...this.props} href={!this.props.active ? this.props.href : undefined}>
                {this.props.children}
            </Breadcrumb.Item>
        )
    }
}

export class StreamrBreadcrumbDropdownButton extends Component<{
    className?: string,
        children?: Children
}> {
    render() {
        return (
            <div className={styles.streamrDropdownContainer}>
                <DropdownButton
                    id={`streamrDropdownButton-${Date.now()}`}
                    {...this.props}
                    bsSize="xs"
                    className={`${this.props.className || ''} ${styles.streamrDropdownButton}`}
                >
                    {this.props.children}
                </DropdownButton>
            </div>
        )
    }
}

export class StreamrBreadcrumbToolbar extends Component<{
    children?: Children
}> {
    render() {
        return (
            <div className={styles.toolbar}>
                {this.props.children}
            </div>
        )
    }
}

export class StreamrBreadcrumbToolbarButton extends Component<{
    iconName: string,
    onClick: Function
}> {
    render() {
        return (
            <div className={styles.button} onClick={this.props.onClick}>
                <FontAwesome name={this.props.iconName}/>
            </div>
        )
    }
}