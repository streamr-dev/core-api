
import reducer from '../../reducers/canvas'
import * as actions from '../../actions/canvas'
import expect from 'expect'
import _ from 'lodash'

describe('Canvas reducer', () => {
    
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
            list: [],
            error: null,
            fetching: false
        })
    })
    
    it('should handle GET_RUNNING_CANVASES', () => {
        expect(
            reducer({}, {
                type: actions.GET_RUNNING_CANVASES_REQUEST
            })
        ).toEqual({
            fetching: true
        })
        
        expect(
            reducer({}, {
                type: actions.GET_RUNNING_CANVASES_SUCCESS,
                canvases: [{
                    id: 1,
                    name: 'A'
                }, {
                    id: 2,
                    name: 'B'
                }, {
                    id: 3,
                    name: 'B'
                }]
            })
        ).toEqual({
            fetching: false,
            list: [{
                id: 1,
                name: 'A'
            }, {
                id: 2,
                name: 'B'
            }, {
                id: 3,
                name: 'B'
            }],
            error: null
        })
        
        expect(
            reducer({
                list: ['test']
            }, {
                type: actions.GET_RUNNING_CANVASES_FAILURE,
                error: new Error('test-error')
            })
        ).toEqual({
            fetching: false,
            list: ['test'],
            error: new Error('test-error')
        })
    })
})