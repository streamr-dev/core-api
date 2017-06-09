
import reducer from '../../reducers/permission'
import * as actions from '../../actions/permission'
import expect from 'expect'
import _ from 'lodash'

describe('Permission reducer', () => {
    
    beforeEach(() => {
        global._ = _
    })
    
    afterEach(() => {
        delete global._
    })
    
    it('should return the initial state', () => {
        expect(
            reducer(undefined, {})
        ).toEqual({
            byTypeAndId: {},
            error: null,
            fetching: false
        })
    })
    
    it('should handle GET_RESOURCE_PERMISSIONS', () => {
        expect(
            reducer({}, {
                type: actions.GET_RESOURCE_PERMISSIONS_REQUEST
            })
        ).toEqual({
            fetching: true
        })
        
        expect(
            reducer({
                byTypeAndId: {}
            }, {
                type: actions.GET_RESOURCE_PERMISSIONS_SUCCESS,
                resourceId: 'testResourceId',
                resourceType: 'testResourceType',
                permissions: ['test', 'test2']
            })
        ).toEqual({
            byTypeAndId: {
                testResourceType: {
                    testResourceId: {
                        permissions: ['test', 'test2'],
                        saving: false
                    }
                }
            },
            fetching: false,
            error: null
        })
        
        expect(
            reducer({}, {
                type: actions.GET_RESOURCE_PERMISSIONS_FAILURE,
                error: new Error('test-error')
            })
        ).toEqual({
            fetching: false,
            error: new Error('test-error')
        })
    })
    
    it('should handle UPDATE_AND_SAVE_RESOURCE_PERMISSION', () => {
        expect(
            reducer({
                byTypeAndId: {
                    testResourceType: {
                        testResourceId: {
                            permissions: ['test', 'test2'],
                            saving: false
                        }
                    }
                }
            }, {
                type: actions.UPDATE_AND_SAVE_RESOURCE_PERMISSION_REQUEST,
                resourceId: 'testResourceId',
                resourceType: 'testResourceType'
            })
        ).toEqual({
            byTypeAndId: {
                testResourceType: {
                    testResourceId: {
                        permissions: ['test', 'test2'],
                        saving: true
                    }
                }
            }
        })
        
        expect(
            reducer({
                byTypeAndId: {}
            }, {
                type: actions.GET_RESOURCE_PERMISSIONS_SUCCESS,
                resourceId: 'testResourceId',
                resourceType: 'testResourceType',
                permissions: ['test', 'test2']
            })
        ).toEqual({
            byTypeAndId: {
                testResourceType: {
                    testResourceId: {
                        permissions: ['test', 'test2'],
                        saving: false
                    }
                }
            },
            fetching: false,
            error: null
        })
        
        expect(
            reducer({}, {
                type: actions.GET_RESOURCE_PERMISSIONS_FAILURE,
                error: new Error('test-error')
            })
        ).toEqual({
            fetching: false,
            error: new Error('test-error')
        })
    })
})