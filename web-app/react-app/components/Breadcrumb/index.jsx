// @flux

import React, {Component} from 'react'

export class Breadcrumb extends Component {
    props: {
        children?: Array<any>
    }
    render() {
        return (
            <div className="breadcrumb breadcrumb-page" style={{
                paddingLeft: '25px'
            }}>
                {this.props.children}
            </div>
        )
    }
}

export class BreadcrumbItem extends Component {
    props: {
        href?: string,
        active?: boolean,
        children: string
    }
    render() {
        return (
            <li className={this.props.active ? 'active' : ''}>
                <a href={this.props.href}>
                    {this.props.children}
                </a>
            </li>
        )
    }
}