
import React from 'react'
import {shallow} from 'enzyme'
import assert from 'assert-diff'
import sinon from 'sinon'
import CanvasInList from '../../../../../../components/DashboardPage/Sidebar/CanvasList/CanvasInList'

describe('CanvasInList', () => {
    let sandbox
    
    beforeEach(() => {
        sandbox = sinon.sandbox.create()
    })
    
    afterEach(() => {
        sandbox.restore()
    })
    
    describe('render', () => {
        it('must render ModuleList correctly', () => {
            const modules = [{
                a: 1
            }, {
                b: 2
            }]
            const el = shallow(<CanvasInList
                canvas={{
                    modules
                }}
            />)
            assert.deepStrictEqual(el.find('ModuleList').props().modules, modules)
        })
        it('must render the checked module length correctly', () => {
            const modules = [{
                a: 1
            }, {
                b: 2
            }, {
                c: 3
            }]
            const el = shallow(<CanvasInList
                canvas={{
                    modules
                }}
            />)
            assert.equal(el.find('.howmanychecked').text(), '')
            modules[0].checked = true
            el.setProps({
                canvas: {
                    modules
                }
            })
            assert.equal(el.find('.howmanychecked').text(), '1')
            modules[2].checked = true
            el.setProps({
                canvas: {
                    modules
                }
            })
            assert.equal(el.find('.howmanychecked').text(), '2')
        })
    })
    
})
