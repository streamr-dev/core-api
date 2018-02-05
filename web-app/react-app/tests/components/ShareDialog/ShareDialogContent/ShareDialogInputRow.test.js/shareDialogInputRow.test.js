
import React from 'react'
import {shallow} from 'enzyme'
import assert from 'assert-diff'
import sinon from 'sinon'
import * as permissionActions from '../../../../../actions/permission.js'

import {ShareDialogInputRow, mapDispatchToProps} from '../../../../../components/ShareDialog/ShareDialogContent/ShareDialogInputRow'

describe('ShareDialogInputRow', () => {
    describe('onSubmit', () => {
        it('should call event.preventDefault', () => {
            const pdSpy = sinon.spy()
            const inputRow = shallow(
                <ShareDialogInputRow
                    resourceType=""
                    resourceId=""
                    addPermission={() => {}}
                />
            )
            inputRow.instance().onSubmit({
                preventDefault: pdSpy,
                target: {
                    email: 'test',
                    reset: () => {}
                }
            })
            assert(pdSpy.calledOnce)
        })
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
            assert(addPermission.calledOnce)
            assert(addPermission.calledWith({
                user: 'test',
                operation: 'read'
            }))
        })
        it('should call form.reset', () => {
            const inputRow = shallow(
                <ShareDialogInputRow
                    resourceType=""
                    resourceId=""
                    addPermission={() => {}}
                />
            )
            const resetSpy = sinon.spy()
            inputRow.instance().onSubmit({
                preventDefault: () => {},
                target: {
                    email: 'test',
                    reset: resetSpy
                }
            })
            assert(resetSpy.calledOnce)
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
            const inputGroup = inputRow.childAt(0)
                .childAt(0)
                .childAt(0)
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
            const inputGroup = inputRow.childAt(0)
                .childAt(0)
                .childAt(0)
            const inputGroupButton = inputGroup.childAt(1)
            const button = inputGroupButton.childAt(0)
            assert.equal(button.props().className, 'addButton')
            assert.equal(button.props().type, 'submit')
            
            const fa = button.childAt(0)
            assert.equal(fa.props().name, 'plus')
        })
    })
    
    describe('mapDispatchToProps', () => {
        it('should return an object with the right kind of props', () => {
            assert.deepStrictEqual(typeof mapDispatchToProps(), 'object')
            assert.deepStrictEqual(typeof mapDispatchToProps().addPermission, 'function')
        })
        describe('addPermission', () => {
            it('should return addResourcePermission and call it with right attrs', () => {
                const dispatchSpy = sinon.spy()
                const addStub = sinon.stub(permissionActions, 'addResourcePermission')
                    .callsFake((type, id, permission) => {
                        return `${type}-${id}-${permission.id}`
                    })
                mapDispatchToProps(dispatchSpy, {
                    resourceType: 'myType',
                    resourceId: 'myId'
                }).addPermission({
                    id: 'test'
                })
                assert(dispatchSpy.calledOnce)
                assert(dispatchSpy.calledWith('myType-myId-test'))
                assert(addStub.calledOnce)
            })
        })
    })
})
