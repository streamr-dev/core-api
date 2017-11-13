
import React from 'react'
import {shallow} from 'enzyme'
import assert from 'assert-diff'

import {ShareDialogPermissionRow} from '../../../../../components/ShareDialog/ShareDialogContent/ShareDialogPermissionRow'

describe('ShareDialogPermissionRow', () => {
    
    describe('render', () => {
        it('renders the permissions correctly', () => {
            const permissions = [{
                field: 1,
                user: 'A'
            }, {
                field: 2,
                user: 'B'
            }, {
                field: 3,
                user: 'C'
            }, {
                field: 4,
                user: 'B'
            }, {
                field: 5,
                user: 'A'
            }, {
                field: 6,
                user: 'A'
            }]
            const permissionRow = shallow(
                <ShareDialogPermissionRow
                    permissions={permissions}
                    resourceType="testType"
                    resourceId="testId"
                />
            )
            
            assert.equal(permissionRow.children().length, 3)
            assert.deepStrictEqual(permissionRow.childAt(0).props(), {
                permissions: [{
                    field: 1,
                    user: 'A'
                }, {
                    field: 5,
                    user: 'A'
                }, {
                    field: 6,
                    user: 'A'
                }],
                resourceType: 'testType',
                resourceId: 'testId'
            })
            
            assert.deepStrictEqual(permissionRow.childAt(1).props(), {
                permissions: [{
                    field: 2,
                    user: 'B'
                }, {
                    field: 4,
                    user: 'B'
                }],
                resourceType: 'testType',
                resourceId: 'testId'
            })
            
            assert.deepStrictEqual(permissionRow.childAt(2).props(), {
                permissions: [{
                    field: 3,
                    user: 'C'
                }],
                resourceType: 'testType',
                resourceId: 'testId'
            })
        })
    })
})
