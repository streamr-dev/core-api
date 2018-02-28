import assert from 'assert-diff'
import {parseDashboard} from '../../helpers/parseState'

describe('parseState', () => {
    describe('parseDashboard', () => {
        it('must return open dashboard', () => {
            assert.deepStrictEqual(parseDashboard({
                dashboard: {
                    dashboardsById: {
                        1: {
                            moi: 23
                        }
                    },
                    openDashboard: {
                        id: 1
                    }
                }
            }).dashboard, {
                moi: 23
            })
        })
        it('must return undefined if no dashboard is open', () => {
            assert.deepStrictEqual(parseDashboard({
                dashboard: {
                    dashboardsById: {
                        1: {
                            moi: 23
                        }
                    },
                    openDashboard: {
                        id: 2
                    }
                }
            }).dashboard, null)
        })
        describe('on unsaved dashboard', () => {
            it('must set canWrite to true', () => {
                assert.deepStrictEqual(parseDashboard({
                    dashboard: {
                        dashboardsById: {
                            1: {
                                moi: 23,
                                new: true,
                                saved: true
                            }
                        },
                        openDashboard: {
                            id: 1
                        }
                    }
                }).canWrite, true)
            })
            it('must set canShare to false', () => {
                assert.deepStrictEqual(parseDashboard({
                    dashboard: {
                        dashboardsById: {
                            1: {
                                moi: 23,
                                new: true,
                                saved: true
                            }
                        },
                        openDashboard: {
                            id: 1
                        }
                    }
                }).canShare, false)
            })
        })
        describe('on saved dashboard', () => {
            describe('with only read permission', () => {
                it('must set canWrite to false', () => {
                    assert.deepStrictEqual(parseDashboard({
                        dashboard: {
                            dashboardsById: {
                                1: {
                                    moi: 23,
                                    new: false,
                                    saved: true,
                                    ownPermissions: ['read']
                                }
                            },
                            openDashboard: {
                                id: 1
                            }
                        }
                    }).canWrite, false)
                })
                it('must set canShare to false', () => {
                    assert.deepStrictEqual(parseDashboard({
                        dashboard: {
                            dashboardsById: {
                                1: {
                                    moi: 23,
                                    new: false,
                                    saved: true,
                                    ownPermissions: ['read']
                                }
                            },
                            openDashboard: {
                                id: 1
                            }
                        }
                    }).canShare, false)
                })
            })
            describe('with write permission', () => {
                it('must set canWrite to true', () => {
                    assert.deepStrictEqual(parseDashboard({
                        dashboard: {
                            dashboardsById: {
                                1: {
                                    moi: 23,
                                    new: false,
                                    saved: true,
                                    ownPermissions: ['write']
                                }
                            },
                            openDashboard: {
                                id: 1
                            }
                        }
                    }).canWrite, true)
                })
            })
            describe('with share permission', () => {
                it('must set canShare to true', () => {
                    assert.deepStrictEqual(parseDashboard({
                        dashboard: {
                            dashboardsById: {
                                1: {
                                    moi: 23,
                                    new: false,
                                    saved: true,
                                    ownPermissions: ['share']
                                }
                            },
                            openDashboard: {
                                id: 1
                            }
                        }
                    }).canShare, true)
                })
            })
        })
    })
})