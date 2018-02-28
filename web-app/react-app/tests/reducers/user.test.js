import reducer from '../../reducers/user'
import * as actions from '../../actions/user'
import assert from 'assert-diff'

describe('User reducer', () => {
    
    it('should return the initial state', () => {
        assert.deepStrictEqual(reducer(undefined, {}), {
            currentUser: null,
            error: null,
            fetching: false,
            saved: true
        })
    })
    
    describe('GET_CURRENT_USER', () => {
        it('should set fetching = true on GET_CURRENT_USER_REQUEST', () => {
            assert.deepStrictEqual(reducer({
                some: 'state'
            }, {
                type: actions.GET_CURRENT_USER_REQUEST
            }), {
                some: 'state',
                fetching: true
            })
        })
        
        it('should set the user as currentUser on GET_CURRENT_USER_SUCCESS', () => {
            assert.deepStrictEqual(
                reducer({
                    some: 'state'
                }, {
                    type: actions.GET_CURRENT_USER_SUCCESS,
                    user: {
                        just: 'someField'
                    }
                }), {
                    some: 'state',
                    currentUser: {
                        just: 'someField'
                    },
                    fetching: false,
                    error: null,
                    saved: true
                })
        })
        
        it('should handle the error on GET_CURRENT_USER_FAILURE', () => {
            assert.deepStrictEqual(
                reducer({
                    some: 'field'
                }, {
                    type: actions.GET_CURRENT_USER_FAILURE,
                    error: new Error('test-error')
                }), {
                    some: 'field',
                    fetching: false,
                    error: new Error('test-error')
                })
        })
    })
    
    describe('SAVE_CURRENT_USER', () => {
        it('should set fetching = true on SAVE_CURRENT_USER_REQUEST', () => {
            assert.deepStrictEqual(reducer({
                some: 'state'
            }, {
                type: actions.SAVE_CURRENT_USER_REQUEST
            }), {
                some: 'state',
                fetching: true
            })
        })
        
        it('should set the user as currentUser on SAVE_CURRENT_USER_SUCCESS', () => {
            assert.deepStrictEqual(
                reducer({
                    some: 'state'
                }, {
                    type: actions.SAVE_CURRENT_USER_SUCCESS,
                    user: {
                        just: 'someField'
                    }
                }), {
                    some: 'state',
                    currentUser: {
                        just: 'someField'
                    },
                    fetching: false,
                    error: null,
                    saved: true
                })
        })
        
        it('should handle the error on SAVE_CURRENT_USER_FAILURE', () => {
            assert.deepStrictEqual(
                reducer({
                    some: 'field'
                }, {
                    type: actions.SAVE_CURRENT_USER_FAILURE,
                    error: new Error('test-error')
                }), {
                    some: 'field',
                    fetching: false,
                    error: new Error('test-error')
                })
        })
    })
    
    describe('UPDATE_CURRENT_USER', () => {
        it('should update the user on UPDATE_CURRENT_USER', () => {
            assert.deepStrictEqual(reducer({
                some: 'state',
                currentUser: {
                    name: 'test',
                    email: 'test2'
                }
            }, {
                type: actions.UPDATE_CURRENT_USER,
                user: {
                    email: 'test3',
                    timezone: 'test4'
                }
            }), {
                some: 'state',
                saved: false,
                currentUser: {
                    name: 'test',
                    email: 'test3',
                    timezone: 'test4'
                }
            })
        })
        it('should add the user if currentUser === null', () => {
            assert.deepStrictEqual(reducer({
                some: 'state',
                currentUser: null
            }, {
                type: actions.UPDATE_CURRENT_USER,
                user: {
                    name: 'test',
                    email: 'test3',
                    timezone: 'test4'
                }
            }), {
                some: 'state',
                saved: false,
                currentUser: {
                    name: 'test',
                    email: 'test3',
                    timezone: 'test4'
                }
            })
        })
    })
})