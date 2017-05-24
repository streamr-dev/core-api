// @flow

import React from 'react'
import {func, array} from 'prop-types'
import {FormControl, InputGroup, FormGroup, Button} from 'react-bootstrap'
import serialize from 'form-serialize'

import styles from './accountHandlerInput.pcss'

const unCamelCase = str => str
    .replace(/([a-z])([A-Z])/g, '$1 $2')
    .replace(/\b([A-Z]+)([A-Z])([a-z])/, '$1 $2$3')
    .replace(/^./, s => s.toUpperCase())

export default class StreamrAccountHandlerInput extends React.Component {
    
    form: HTMLFormElement
    onSubmit: Function
    
    constructor() {
        super()
        this.onSubmit = this.onSubmit.bind(this)
    }
    
    onSubmit(e: Event) {
        e.preventDefault()
        const data = serialize(this.form, {
            hash: true
        })
        this.props.onNew(data)
        this.form.reset()
    }
    
    render() {
        return (
            <form className={styles.accountInputForm} ref={i => this.form = i} onSubmit={this.onSubmit}>
                <FormGroup>
                    <InputGroup className={styles.accountInputGroup}>
                        {['name', ...this.props.fields].map(field => (
                            <FormControl
                                key={field}
                                name={field}
                                type="text"
                                className={styles.accountInput}
                                placeholder={unCamelCase(field)}
                            />
                        ))}
                        <InputGroup.Button className={styles.buttonContainer}>
                            <Button bsStyle="default" type="submit">
                                <i className="icon fa fa-plus"/>
                            </Button>
                        </InputGroup.Button>
                    </InputGroup>
                </FormGroup>
            </form>
        )
    }
}
StreamrAccountHandlerInput.propTypes = {
    fields: array,
    onNew: func
}