
import React from 'react'
import {shallow} from 'enzyme'
import assert from 'assert-diff'
import ModuleList from '../../../../../../../components/DashboardPage/Sidebar/CanvasList/CanvasInList/ModuleList'

describe('ModuleList', () => {
    
    describe('render', () => {
        it('must render only modules with uiChannel', () => {
            const modules = [{
                hash: 1,
                uiChannel: {
                    just: 'something'
                }
            }, {
                hash: 2,
                uiChannel: false
            }, {
                hash: 3,
                uiChannel: 'nonFalse'
            }, {
                hash: 4
            }]
            const el = shallow(<ModuleList
                modules={modules}
            />)
            assert.equal(el.children().length, 2)
            assert.deepStrictEqual(el.childAt(0).props().module, modules[0])
            assert.deepStrictEqual(el.childAt(1).props().module, modules[2])
        })
        it('must render the modules in alphabetical order by name', () => {
            const modules = [{
                name: 'b',
                hash: 1,
                uiChannel: true
            }, {
                name: 'c',
                hash: 2,
                uiChannel: true
            }, {
                name: 'a',
                hash: 3,
                uiChannel: true
            }]
            const el = shallow(<ModuleList
                modules={modules}
            />)
            assert.deepStrictEqual(el.childAt(0).props().module, modules[2])
            assert.deepStrictEqual(el.childAt(1).props().module, modules[0])
            assert.deepStrictEqual(el.childAt(2).props().module, modules[1])
        })
    })
    
})
