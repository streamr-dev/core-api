
import React from 'react'
import {shallow} from 'enzyme'
import assert from 'assert-diff'
import sinon from 'sinon'

import * as permissionActions from '../../../../../../actions/permission.js'

import {ShareDialogPermission, mapDispatchToProps} from '../../../../../../components/ShareDialog/ShareDialogContent/ShareDialogPermissionRow/ShareDialogPermission'

describe('ShareDialogPermission', () => {

    beforeEach(() => {
        global.Streamr = {}
    })

    afterEach(() => {
        delete global.Streamr
    })

    describe('onSelect', () => {
        it('should call props.setResourceHighestOperation with the given value', () => {
            const spy = sinon.spy()
            const component = shallow(<ShareDialogPermission
                resourceType=""
                resourceId=""
                permissions={[]}
                remove={() => {}}
                setResourceHighestOperation={spy}
            />)
            component.instance().onSelect({
                value: 'test'
            })
            assert(spy.calledOnce)
            assert(spy.calledWith('test'))
        })
    })

    describe('onRemove', () => {
        it('should call props.setResourceHighestOperation with the given value', () => {
            const spy = sinon.spy()
            const component = shallow(<ShareDialogPermission
                resourceType=""
                resourceId=""
                permissions={[]}
                remove={spy}
                setResourceHighestOperation={() => {}}
            />)
            component.instance().onRemove()
            assert(spy.calledOnce)
        })
    })

    describe('render', () => {
        it('renders the userLabel correctly if there is a user', () => {
            global.Streamr.user = 'test@test.test'
            const permissions = [{
                user: 'test@test.test'
            }]
            const permissionRow = shallow(
                <ShareDialogPermission
                    permissions={permissions}
                    resourceType=""
                    resourceId=""
                />
            )

            assert(permissionRow.find('.userLabel'))
            assert.equal(permissionRow.find('.userLabel').text(), 'Me(test@test.test)')
        })
        it('renders the userLabel correctly if there is no user', () => {
            const permissions = [{
                user: 'A'
            }]
            const permissionRow = shallow(
                <ShareDialogPermission
                    permissions={permissions}
                    resourceType=""
                    resourceId=""
                />
            )

            assert(permissionRow.find('.userLabel'))
            assert.equal(permissionRow.find('.userLabel').text(), 'A')
        })
        it('renders the Select correctly', () => {
            const permissions = [{
                user: 'A',
                operation: 'read'
            }, {
                user: 'B',
                operation: 'write'
            }]
            const permission = shallow(
                <ShareDialogPermission
                    permissions={permissions}
                    resourceType=""
                    resourceId=""
                />
            )
            const select = permission.find('Select')
            assert(select)
            assert.deepStrictEqual(select.props().value, 'write')
            assert.deepStrictEqual(select.props().options, [{
                value: 'read',
                label: 'can read'
            }, {
                value: 'write',
                label: 'can write'
            }, {
                value: 'share',
                label: 'can share'
            }])
            assert.deepStrictEqual(select.props().clearable, false)
            assert.deepStrictEqual(select.props().searchable, false)
            assert.deepStrictEqual(select.props().autosize, false)
            assert.deepStrictEqual(select.props().onChange, permission.instance().onSelect)
        })
        it('renders the button correctly', () => {
            const permissions = [{
                user: 'A',
                operation: 'read'
            }, {
                user: 'B',
                operation: 'write'
            }]
            const permission = shallow(
                <ShareDialogPermission
                    permissions={permissions}
                    resourceType=""
                    resourceId=""
                />
            )
            const button = permission.find('Button')
            assert(button)
            assert.deepStrictEqual(button.props().bsStyle, 'danger')
            assert.deepStrictEqual(button.props().onClick, permission.instance().onRemove)

            const fa = button.childAt(0)
            assert(fa.is('FontAwesome'))
            assert.equal(fa.props().name, 'trash-o')
        })
        it('renders the possible errors correctly', () => {
            const permissions = [{
                user: 'A',
                operation: 'read',
                error: {
                    message: 'moi'
                }
            }, {
                user: 'B',
                operation: 'write',
                error: {
                    message: 'hei'
                }
            }]
            const permission = shallow(
                <ShareDialogPermission
                    permissions={permissions}
                    resourceType=""
                    resourceId=""
                />
            )
            const errorContainer = permission.find('.errorContainer')
            assert(errorContainer)
            assert.equal(errorContainer.props().title, 'moi\nhei')

            const fa = errorContainer.childAt(0)
            assert(fa.is('FontAwesome'))
            assert.equal(fa.props().name, 'exclamation-circle')
            assert.equal(fa.props().className, 'text-danger')
        })
    })

    describe('mapDispatchToProps', () => {
        it('should return an object with the right kind of props', () => {
            assert.deepStrictEqual(typeof mapDispatchToProps(), 'object')
            assert.deepStrictEqual(typeof mapDispatchToProps().setResourceHighestOperation, 'function')
            assert.deepStrictEqual(typeof mapDispatchToProps().remove, 'function')
        })
        describe('setResourceHighestOperation', () => {
            it('should dispatch setResourceHighestOperationForUser and call it with right attrs', () => {
                const dispatchSpy = sinon.spy()
                const addStub = sinon.stub(permissionActions, 'setResourceHighestOperationForUser').callsFake((type, id, user, value) => {
                    return `${type}-${id}-${user}-${value}`
                })
                mapDispatchToProps(dispatchSpy, {
                    resourceType: 'myType',
                    resourceId: 'myId',
                    permissions: [{
                        user: 'a'
                    }]
                }).setResourceHighestOperation('test')
                assert(dispatchSpy.calledOnce)
                assert(dispatchSpy.calledWith('myType-myId-a-test'))
                assert(addStub.calledOnce)
            })
        })
        describe('removeAllResourcePermissionsByUser', () => {
            it('should dispatch setResourceHighestOperationForUser and call it with right attrs', () => {
                const dispatchSpy = sinon.spy()
                const addStub = sinon.stub(permissionActions, 'removeAllResourcePermissionsByUser').callsFake((type, id, user, value) => {
                    return `${type}-${id}-${user}`
                })
                mapDispatchToProps(dispatchSpy, {
                    resourceType: 'myType',
                    resourceId: 'myId',
                    permissions: [{
                        user: 'a'
                    }]
                }).remove()
                assert(dispatchSpy.calledOnce)
                assert(dispatchSpy.calledWith('myType-myId-a'))
                assert(addStub.calledOnce)
            })
        })
    })

})
