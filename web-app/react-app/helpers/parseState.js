
export const parseDashboard = ({dashboard: {dashboardsById, openDashboard}}) => {
    const db = dashboardsById[openDashboard.id] || {}
    return {
        dashboard: db,
        canShare: db.new !== true && (db.ownPermissions && db.ownPermissions.includes('share')),
        canWrite: db.new !== true && (db.ownPermissions && db.ownPermissions.includes('write'))
    }
}