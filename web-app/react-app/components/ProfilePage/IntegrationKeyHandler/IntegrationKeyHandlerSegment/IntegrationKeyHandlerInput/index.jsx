// @flow

import React from 'react'
import {FormControl, InputGroup, FormGroup, Button} from 'react-bootstrap'
import FontAwesome from 'react-fontawesome'
import serialize from 'form-serialize'

import styles from './integrationKeyHandlerInput.pcss'

import {titleCase} from 'change-case'

export default class IntegrationKeyHandlerInput extends React.Component {
    
    form: HTMLFormElement
    onSubmit: Function
    props: {
        fields: Array<string>,
        onNew: Function
    }
    
    constructor() {
        super()
        this.onSubmit = this.onSubmit.bind(this)
    }
    
    onSubmit(e: Event) {
        e.preventDefault()
        const form = e.target
        const data = serialize(form, {
            hash: true
        })
        this.props.onNew(data)
        form.reset()
    }
    
    render() {
        return (
            <form className={styles.integrationKeyInputForm} onSubmit={this.onSubmit}>
                <FormGroup>
                    <InputGroup className={styles.integrationKeyInputGroup}>
                        {['name', ...this.props.fields].map(field => (
                            <FormControl
                                key={field}
                                name={field}
                                type="text"
                                className={styles.integrationKeyInput}
                                placeholder={titleCase(field)}
                            />
                        ))}
                        <InputGroup.Button className={styles.buttonContainer}>
                            <Button bsStyle="default" type="submit">
                                <FontAwesome name="plus" className="icon"/>
                            </Button>
                        </InputGroup.Button>
                    </InputGroup>
                </FormGroup>
            </form>
        )
    }
}