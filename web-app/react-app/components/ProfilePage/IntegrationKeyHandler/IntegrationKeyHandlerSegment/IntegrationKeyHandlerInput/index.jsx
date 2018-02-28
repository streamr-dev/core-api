// @flow

import React, {Component} from 'react'
import {FormControl, InputGroup, FormGroup, Button} from 'react-bootstrap'
import FontAwesome from 'react-fontawesome'
import serialize from 'form-serialize'

import styles from './integrationKeyHandlerInput.pcss'

import {titleCase} from 'change-case'

import type {IntegrationKey} from '../../../../../flowtype/integration-key-types'

type GivenProps = {
    fields: Array<string>,
    onNew: (integrationKey: IntegrationKey) => void
}

type Props = GivenProps

export default class IntegrationKeyHandlerInput extends Component<Props> {
    form: ?HTMLFormElement

    onSubmit = (e: {
        preventDefault: Function,
        target: HTMLFormElement
    }) => {
        e.preventDefault()
        const form: HTMLFormElement = e.target
        const data = serialize(form, {
            hash: true,
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
