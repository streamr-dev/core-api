import reducer from '../../reducers/notification'
import * as actions from '../../actions/notification'
import assert from 'assert-diff'

describe('Notification reducer', () => {
    
    it('should return the initial state', () => {
        assert.deepStrictEqual(reducer(undefined, {}), {
            byId: {}
        })
    })
    
    describe('CREATE_NOTIFICATION', () => {
        it('should return right kind of object', () => {
            assert.deepStrictEqual(reducer({
                byId: {
                    '0': {
                        id: '0',
                        hei: 'heihei'
                    }
                }
            }, {
                type: actions.CREATE_NOTIFICATION,
                notification: {
                    id: '1',
                    moi: 'moimoi'
                }
            }), {
                byId: {
                    '0': {
                        id: '0',
                        hei: 'heihei'
                    },
                    '1': {
                        id: '1',
                        moi: 'moimoi'
                    }
                }
            })
        })
    })
    describe('REMOVE_NOTIFICATION', () => {
        it('should return right kind of object', () => {
            assert.deepStrictEqual(reducer({
                byId: {
                    '0': {
                        id: '0',
                        hei: 'heihei'
                    },
                    '1': {
                        id: '1',
                        moi: 'moimoi'
                    },
                    '2': {
                        id: '2',
                        hei: 'moihei'
                    }
                }
            }, {
                type: actions.REMOVE_NOTIFICATION,
                id: '1'
            }), {
                byId: {
                    '0': {
                        id: '0',
                        hei: 'heihei'
                    },
                    '2': {
                        id: '2',
                        hei: 'moihei'
                    }
                }
            })
        })
    })
})