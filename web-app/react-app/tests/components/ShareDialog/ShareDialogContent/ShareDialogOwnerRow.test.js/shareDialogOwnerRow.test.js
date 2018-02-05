
import React from 'react'
import {shallow} from 'enzyme'
import assert from 'assert-diff'
import sinon from 'sinon'

import * as permissionActions from '../../../../../actions/permission.js'

import {ShareDialogOwnerRow, mapStateToProps, mapDispatchToProps} from '../../../../../components/ShareDialog/ShareDialogContent/ShareDialogOwnerRow'

describe('ShareDialogOwnerRow', () => {
    describe('onAnonymousAccessChange', () => {
        it('calls props.addPublicPermission if !props.anonymousPermission', () => {
            const addPublicPermission = sinon.spy()
            const ownerRow = shallow(
                <ShareDialogOwnerRow
                    resourceType=""
                    resourceId=""
                    owner=""
                    addPublicPermission={addPublicPermission}
                    revokePublicPermission={() => {
                        throw new Error('should not be called')
                    }}
                />
            )
            ownerRow.instance().onAnonymousAccessChange()
            assert(addPublicPermission.called)
        })
        it('calls props revokePublicPermission if props.anonymousPermission', () => {
            const revokePublicPermission = sinon.spy()
            const ownerRow = shallow(
                <ShareDialogOwnerRow
                    permissions={[]}
                    resourceType=""
                    resourceId=""
                    owner=""
                    anonymousPermission="not undefined"
                    getResourcePermissions={() => {}}
                    addPublicPermission={() => {
                        throw new Error('should not be called')
                    }}
                    revokePublicPermission={revokePublicPermission}
                />
            )
            ownerRow.instance().onAnonymousAccessChange()
            assert(revokePublicPermission.called)
        })
    })
    
    describe('render', () => {
        it('renders with correct texts', () => {
            const ownerRow = shallow(
                <ShareDialogOwnerRow
                    resourceType="testType"
                    resourceId="testId"
                    owner="test user"
                    addPublicPermission={() => {}}
                    revokePublicPermission={() => {}}
                />
            )
            assert.equal(ownerRow.find('.readAccessLabel').text(), 'Public read access')
        })
        describe('Switcher', () => {
            it('renders switcher correctly', () => {
                const ownerRow = shallow(
                    <ShareDialogOwnerRow
                        resourceType="testType"
                        resourceId="testId"
                        owner="test user"
                        addPublicPermission={() => {}}
                        revokePublicPermission={() => {}}
                    />
                )
                const switcher = ownerRow.find('.readAccess').childAt(0)
                assert(switcher.props().on === false)
                assert(switcher.props().onClick === ownerRow.instance().onAnonymousAccessChange)
            })
            it('sets props.on to true if anonymousPermission exists', () => {
                const ownerRow = shallow(
                    <ShareDialogOwnerRow
                        resourceType="testType"
                        resourceId="testId"
                        owner="test user"
                        anonymousPermission="not undefined"
                        addPublicPermission={() => {}}
                        revokePublicPermission={() => {}}
                    />
                )
                const switcher = ownerRow.find('.readAccess').childAt(0)
                assert(switcher.props().on === true)
            })
        })
    })
    
    describe('mapStateToProps', () => {
        describe('owner field', () => {
            it('should find the owner from the permissions by the null id', () => {
                const byTypeAndId = {
                    type: {
                        id: [{
                            id: 'asdfasdf',
                            user: 'test1'
                        }, {
                            id: null,
                            user: 'test2'
                        }]
                    }
                }
                assert.equal(mapStateToProps({
                    permission: {
                        byTypeAndId
                    }
                }, {
                    resourceType: 'type',
                    resourceId: 'id'
                }).owner, 'test2')
            })
            it('should not accept a permission with new: true as owner', () => {
                const byTypeAndId = {
                    type: {
                        id: [{
                            id: null,
                            new: true,
                            user: 'test1'
                        }, {
                            id: null,
                            user: 'test2'
                        }]
                    }
                }
                assert.equal(mapStateToProps({
                    permission: {
                        byTypeAndId
                    }
                }, {
                    resourceType: 'type',
                    resourceId: 'id'
                }).owner, 'test2')
            })
        })
        describe('anonymousPermission field', () => {
            it('should find the anonymousPermission from the permissions by anonymous: true', () => {
                const byTypeAndId = {
                    type: {
                        id: [{
                            id: 'asdfasdf',
                            anonymous: true
                        }, {
                            id: null,
                            user: 'test2'
                        }]
                    }
                }
                assert.deepStrictEqual(mapStateToProps({
                    permission: {
                        byTypeAndId
                    }
                }, {
                    resourceType: 'type',
                    resourceId: 'id'
                }).anonymousPermission, {
                    id: 'asdfasdf',
                    anonymous: true
                })
            })
            it('should not accept removed permissions', () => {
                const byTypeAndId = {
                    type: {
                        id: [{
                            id: 'aaa',
                            anonymous: true,
                            removed: true
                        }, {
                            id: 'bbb',
                            anonymous: true,
                            removed: false
                        }, {
                            id: null,
                            user: 'test2'
                        }]
                    }
                }
                assert.deepStrictEqual(mapStateToProps({
                    permission: {
                        byTypeAndId
                    }
                }, {
                    resourceType: 'type',
                    resourceId: 'id'
                }).anonymousPermission, {
                    id: 'bbb',
                    anonymous: true,
                    removed: false
                })
            })
            it('should return undefined if no anonymous permission found', () => {
                const byTypeAndId = {
                    type: {
                        id: [{
                            id: null,
                            user: 'test2'
                        }]
                    }
                }
                assert.deepStrictEqual(mapStateToProps({
                    permission: {
                        byTypeAndId
                    }
                }, {
                    resourceType: 'type',
                    resourceId: 'id'
                }).anonymousPermission, undefined)
            })
        })
        describe('error situations', () => {
            it('should just return undefineds if invalid id', () => {
                const byTypeAndId = {
                    type: {
                        id: []
                    }
                }
                assert.deepStrictEqual(mapStateToProps({
                    permission: {
                        byTypeAndId
                    }
                }, {
                    resourceType: 'type',
                    resourceId: 'anotherId'
                }), {
                    anonymousPermission: undefined,
                    owner: undefined
                })
            })
            it('should just return undefineds if invalid type', () => {
                const byTypeAndId = {
                    type: {
                        id: []
                    }
                }
                assert.deepStrictEqual(mapStateToProps({
                    permission: {
                        byTypeAndId
                    }
                }, {
                    resourceType: 'anotherType',
                    resourceId: 'anotherId'
                }), {
                    anonymousPermission: undefined,
                    owner: undefined
                })
            })
        })
    })
    
    describe('mapDispatchToProps', () => {
        it('should return an object with the right kind of props', () => {
            assert.deepStrictEqual(typeof mapDispatchToProps(), 'object')
            assert.deepStrictEqual(typeof mapDispatchToProps().addPublicPermission, 'function')
            assert.deepStrictEqual(typeof mapDispatchToProps().revokePublicPermission, 'function')
        })
        
        describe('addPublicPermission', () => {
            it('should dispatch addResourcePermission and call it with right attrs', () => {
                const dispatchSpy = sinon.spy()
                const addStub = sinon.stub(permissionActions, 'addResourcePermission').callsFake((type, id, opt) => {
                    assert(opt.anonymous)
                    assert.equal(opt.operation, 'read')
                    return `${type}-${id}`
                })
                mapDispatchToProps(dispatchSpy, {
                    resourceType: 'myType',
                    resourceId: 'myId'
                }).addPublicPermission()
                assert(dispatchSpy.calledOnce)
                assert(dispatchSpy.calledWith('myType-myId'))
                assert(addStub.calledOnce)
            })
        })
        
        describe('revokePublicPermission', () => {
            it('should dispatch removeResourcePermission and call it with right attrs', () => {
                const anonymousPermission = {
                    id: 'hehehehe'
                }
                const dispatchSpy = sinon.spy()
                const revokeStub = sinon.stub(permissionActions, 'removeResourcePermission').callsFake((type, id, ap) => {
                    return `${type}-${id}-${ap.id}`
                })
                mapDispatchToProps(dispatchSpy, {
                    resourceType: 'myType',
                    resourceId: 'myId',
                    anonymousPermission
                }).revokePublicPermission(anonymousPermission)
                assert(dispatchSpy.calledOnce)
                assert(dispatchSpy.calledWith('myType-myId-hehehehe'))
                assert(revokeStub.calledOnce)
            })
        })
    })
})
