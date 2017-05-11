import React from 'react'
import {string, func, array} from 'prop-types'
import ReactDOM from 'react-dom'

/* global ConfirmButton */

const unCamelCase = str => str
    .replace(/([a-z])([A-Z])/g, '$1 $2')
    .replace(/\b([A-Z]+)([A-Z])([a-z])/, '$1 $2$3')
    .replace(/^./, s => s.toUpperCase())

class StreamrAccountHandlerInput extends React.Component {
    
    constructor() {
        super()
        this.inputs = {}
    }
    
    render() {
        return (
            <form className="input-group form-inline new-account-item-form" ref={i => this.form = i} style={{
                display: 'flex',
                width: '100%'
            }}>
                {['name', ...this.props.fields].map(field => (
                    <input
                        key={field}
                        type="text"
                        ref={i => this.inputs[field] = i}
                        name={field}
                        className="form-control"
                        placeholder={unCamelCase(field)}
                        style={{
                            flex: '1'
                        }}
                    />
                ))}
                <span className="input-group-btn" onClick={e => {
                    e.preventDefault()
                    let data = {}
                    for (const input in this.inputs) {
                        if (!this.inputs[input].value) {
                            return
                        }
                        data[input] = this.inputs[input].value
                    }
                    this.form.reset()
                    this.props.onNew(data, e)
                }} style={{
                    width: 'auto',
                    flex: '0',
                    display: 'inline-block'
                }}>
                    <button className="new-account-item-button btn btn-default" type="button" style={{
                        height: '100%',
                    }}>
                        <span className="icon fa fa-plus"/>
                    </button>
                </span>
            </form>
        )
    }
}
StreamrAccountHandlerInput.propTypes = {
    fields: array,
    onNew: func
}

class StreamrAccountHandlerTable extends React.Component {
    
    render() {
        const items = this.props.items || []
        return (
            <table className="table">
                <thead>
                <tr>
                    <th>Name</th>
                    {this.props.fields.map(f => (
                        <th key={f}>
                            {unCamelCase(f)}
                        </th>
                    ))}
                    <th/>
                </tr>
                </thead>
                <tbody>
                {items.map(item => (
                    <tr key={item[this.props.idField]}>
                        <td>
                            {item.name}
                        </td>
                        {this.props.fields.map(f => (
                            <td key={f}>
                                {item[f]}
                            </td>
                        ))}
                        <td>
                            <button
                                ref={el => {
                                    new ConfirmButton(el, {}, res => {
                                        if (res) {
                                            this.props.onDelete(item[this.props.idField])
                                        }
                                    })
                                }}
                                type="button"
                                className="form-group account-item-delete-button btn btn-danger pull-right"
                                title="Delete key">
                                <span className="icon fa fa-trash-o"/>
                            </button>
                        </td>
                    </tr>
                ))}
                </tbody>
            </table>
        )
    }
}
StreamrAccountHandlerTable.propTypes = {
    fields: array,
    items: array,
    onDelete: func,
    idField: string
}

class StreamrAccountHandlerSegment extends React.Component {
    constructor(props) {
        super(props)
        this.state = {
            items: []
        }
        this.onNew = this.onNew.bind(this)
        this.onDelete = this.onDelete.bind(this)
    }
    
    onNew(data) {
        this.setState({
            items: [...this.state.items, data]
        })
    }
    
    onDelete(id) {
        if (id === undefined) {
            return
        }
        let items = this.state.items
        let j
        for (let i = 0; i < this.state.items.length; i++) {
            if (this.state.items[i][this.props.idField] === id) {
                j = i
            }
        }
        delete items[j]
        this.setState({
            items
        })
    }
    
    render() {
        return (
            <div className="col-xs-12">
                <label>
                    {this.props.name}
                </label>
                <StreamrAccountHandlerTable fields={this.props.fields} items={this.state.items}
                                            idField={this.props.idField} onDelete={this.onDelete}/>
                <StreamrAccountHandlerInput fields={this.props.fields} onNew={this.onNew}/>
            </div>
        )
    }
}
StreamrAccountHandlerSegment.propTypes = {
    fields: array,
    items: array,
    name: string,
    idField: string
}

class StreamrAccountHandler extends React.Component {
    
    render() {
        return (
            <div className="streamr-account-handler row">
                <StreamrAccountHandlerSegment name="Ethereum" fields={['privateKey']} idField="privateKey"/>
            </div>
        )
    }
}

ReactDOM.render(React.createElement(StreamrAccountHandler, {}, null), document.getElementById('streamrAccountHandler'))
