
import React from 'react'
import {shallow} from 'enzyme'
import assert from 'assert-diff'
import sinon from 'sinon'

import {ShareDialogOwnerRow} from '../../../../../components/ShareDialog/ShareDialogContent/ShareDialogOwnerRow'

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
            assert(ownerRow.is('.ownerRow'))
            assert.equal(ownerRow.find('.owner').text(), 'test user')
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
})
