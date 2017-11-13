
import React from 'react'
import {shallow} from 'enzyme'
import assert from 'assert-diff'
import sinon from 'sinon'

import {ShareDialogInputRow} from '../../../../../components/ShareDialog/ShareDialogContent/ShareDialogInputRow'

describe('ShareDialogInputRow', () => {
    describe('onSubmit', () => {
        it('should serialize the form', () => {
            const addPermission = sinon.spy()
            const inputRow = shallow(
                <ShareDialogInputRow
                    resourceType=""
                    resourceId=""
                    addPermission={addPermission}
                />
            )
            inputRow.instance().onSubmit({
                preventDefault: () => {},
                target: {
                    reset: () => {},
                    email: 'test'
                }
            })
            assert.equal(addPermission.callCount, 1)
            assert.deepStrictEqual(addPermission.args[0][0], {
                user: 'test',
                operation: 'read'
            })
        })
        it('should call form.reset', () => {
            const inputRow = shallow(
                <ShareDialogInputRow
                    resourceType=""
                    resourceId=""
                    addPermission={() => {}}
                />
            )
            const reset = sinon.spy()
            inputRow.instance().onSubmit({
                preventDefault: () => {},
                target: {
                    email: 'test',
                    reset
                }
            })
            assert(reset.called)
        })
    })
    
    describe('render', () => {
        it('renders the tree correctly', () => {
            const inputRow = shallow(
                <ShareDialogInputRow
                    resourceType=""
                    resourceId=""
                    addPermission={() => {}}
                />
            )
            assert(inputRow.is('.inputRow'))
            assert.equal(inputRow.children().length, 1)
            const form = inputRow.childAt(0)
            assert(form.is('form'))
            assert(form.props().onSubmit === inputRow.instance().onSubmit)
            assert.equal(form.children().length, 1)
            const formGroup = form.childAt(0)
            assert.equal(formGroup.children().length, 1)
            const inputGroup = formGroup.childAt(0)
            assert.equal(inputGroup.children().length, 2)
        })
        it('renders input correctly', () => {
            const inputRow = shallow(
                <ShareDialogInputRow
                    resourceType=""
                    resourceId=""
                    addPermission={() => {}}
                />
            )
            const inputGroup = inputRow.childAt(0).childAt(0).childAt(0)
            const input = inputGroup.childAt(0)
            assert.equal(input.props().type, 'email')
            assert.equal(input.props().placeholder, 'Enter email address')
            assert.equal(input.props().name, 'email')
        })
        it('renders button correctly', () => {
            const inputRow = shallow(
                <ShareDialogInputRow
                    resourceType=""
                    resourceId=""
                    addPermission={() => {}}
                />
            )
            const inputGroup = inputRow.childAt(0).childAt(0).childAt(0)
            const inputGroupButton = inputGroup.childAt(1)
            const button = inputGroupButton.childAt(0)
            assert.equal(button.props().className, 'addButton')
            assert.equal(button.props().type, 'submit')
            
            const fa = button.childAt(0)
            assert.equal(fa.props().name, 'plus')
        })
    })
})
