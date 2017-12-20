
import assert from 'assert-diff'
import {mapStateToProps} from '../../../../../components/DashboardPage/Sidebar/CanvasList'

describe('CanvasList', () => {
    
    describe('mapStateToProps', () => {
        it('must return canvases or an empty list', () => {
            const canvases = [1,2,3]
            assert.deepStrictEqual(mapStateToProps({
                canvas: {
                    list: canvases
                },
                dashboard: {
                    dashboardsById: {},
                    openDashboard: {}
                }
            }).canvases, canvases)
            assert.deepStrictEqual(mapStateToProps({
                canvas: {},
                dashboard: {
                    dashboardsById: {},
                    openDashboard: {}
                }
            }).canvases, [])
        })
        it('must show canvases if dashboard is new', () => {
            assert(mapStateToProps({
                canvas: {},
                dashboard: {
                    dashboardsById: {
                        1: {
                            new: true
                        }
                    },
                    openDashboard: {
                        id: 1
                    }
                }
            }).showCanvases)
        })
        it('must show canvases if dashboards own permissions contain write permission', () => {
            assert(mapStateToProps({
                canvas: {},
                dashboard: {
                    dashboardsById: {
                        1: {
                            ownPermissions: ['write']
                        }
                    },
                    openDashboard: {
                        id: 1
                    }
                }
            }).showCanvases)
        })
        it('must not show canvases if not permission to write', () => {
            assert(!mapStateToProps({
                canvas: {},
                dashboard: {
                    dashboardsById: {
                        1: {}
                    },
                    openDashboard: {
                        id: 1
                    }
                }
            }).showCanvases)
        })
    })
    
})
