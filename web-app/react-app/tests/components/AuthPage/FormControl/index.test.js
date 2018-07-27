/* eslint no-unused-vars: ["error", { "ignoreRestSiblings": true }] */

import React from 'react'
import { mount } from 'enzyme'
import zxcvbn from 'zxcvbn'
import sinon from 'sinon'

import FormControl from '../../../../components/AuthPage/shared/FormControl'
import InputError from '../../../../components/AuthPage/shared/FormControl/InputError'

const formatter = ({ value }) => value
const UnwrappedField = () => null
const Field = FormControl(UnwrappedField, formatter)

describe(FormControl.name, () => {
    describe('methods', () => {
        describe('#strengthLevel', () => {
            const l = (props) => mount(
                <Field
                    type="password"
                    measureStrength
                    value="x"
                    {...props}
                />
            )
                .instance()
                .strengthLevel()

            it('gives non-negative value for a non-empty password field with measureStrength flag set', () => {
                expect(zxcvbn('x').score).toBe(0) // — making sure.
                expect(l({})).toBe(0)
            })

            it('gives -1 for a non-password input', () => {
                expect(l({
                    type: 'text',
                })).toBe(-1)
            })

            it('gives -1 if measureStrength is not set', () => {
                expect(l({
                    measureStrength: false,
                })).toBe(-1)
            })

            it('gives -1 for an empty field', () => {
                expect(l({
                    value: '',
                })).toBe(-1)
            })
        })

        describe('#onChange', () => {
            const change = (props) => (payload) => mount(
                <Field {...props} />
            )
                .instance()
                .onChange(payload)

            it('calls props.onChange with field name and formatted value', () => {
                const onChange = sinon.spy()
                change({
                    name: 'myField',
                    onChange,
                })({
                    value: 'myValue',
                })
                expect(onChange.calledOnceWithExactly('myField', 'myValue'))
            })
        })
    })

    describe('label', () => {
        const mockStrength = (strengthLevel, callback) => {
            sinon.stub(Field.prototype, 'strengthLevel').callsFake(() => strengthLevel)
            const el = mount(
                <Field
                    label="fancy label"
                />
            )
            callback(el.find('label').text())
            Field.prototype.strengthLevel.restore()
        }

        it('displays label from props for negative strength', () => {
            mockStrength(-1, (label) => {
                expect(label).toBe('fancy label')
            })
        })

        it('displays "weak password" message for 0 strength', () => {
            mockStrength(0, (label) => {
                expect(label).toMatch(/is weak/)
            })
        })

        it('displays "moderate password" message for 1 strength', () => {
            mockStrength(1, (label) => {
                expect(label).toMatch(/is not strong/)
            })
        })

        it('displays "strong password" message for 2 strength', () => {
            mockStrength(2, (label) => {
                expect(label).toMatch(/is quite strong/)
            })
        })
    })

    it('passes props to the wrapped component instance', () => {
        const el = mount(<Field value="value" />)
        const field = el.find(UnwrappedField)
        expect(field.exists()).toBe(true)
        const { onChange, onFocusChange, setAutoCompleted } = el.instance()
        expect(field.props()).toMatchObject({
            value: 'value',
            onChange,
            onBlur: onFocusChange,
            onFocus: onFocusChange,
            onAutoComplete: setAutoCompleted,
        })
    })

    describe('errors', () => {
        it('gets rendered as an empty error block by default', () => {
            expect(mount(
                <Field />
            )
                .find(InputError)
                .text()
            ).toBe('')
        })

        it('displays the last known error', () => {
            expect(mount(
                <Field
                    error="last known error"
                />
            )
                .find(InputError)
                .text()
            ).toBe('last known error')
        })

        it('is empty when the instance is processing', () => {
            expect(mount(
                <Field
                    error="last known error"
                    processing
                />
            )
                .find(InputError)
                .text()
            ).toBe('')
        })
    })
})
