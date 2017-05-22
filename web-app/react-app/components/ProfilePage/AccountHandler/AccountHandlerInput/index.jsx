
import React from 'react'
import {func, array} from 'prop-types'

import styles from './accountHandlerInput.pcss'

const unCamelCase = str => str
    .replace(/([a-z])([A-Z])/g, '$1 $2')
    .replace(/\b([A-Z]+)([A-Z])([a-z])/, '$1 $2$3')
    .replace(/^./, s => s.toUpperCase())

export default class StreamrAccountHandlerInput extends React.Component {
    
    constructor() {
        super()
        this.inputs = {}
    }
    
    render() {
        return (
            <form className={`input-group form-inline new-account-item-form ${styles.accountInput}`} ref={i => this.form = i} style={{
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