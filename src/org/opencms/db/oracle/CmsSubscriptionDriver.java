/*
 * File   : $Source: /alkacon/cvs/opencms/src/org/opencms/db/oracle/CmsSubscriptionDriver.java,v $
 * Date   : $Date: 2010/08/05 12:55:10 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (C) 2002 - 2009 Alkacon Software (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.db.oracle;

import org.opencms.db.CmsDbConsistencyException;
import org.opencms.db.CmsDbContext;
import org.opencms.db.CmsDbSqlException;
import org.opencms.db.generic.CmsSqlManager;
import org.opencms.db.generic.Messages;
import org.opencms.db.log.CmsLogEntry;
import org.opencms.db.log.CmsLogEntryType;
import org.opencms.db.log.CmsLogFilter;
import org.opencms.file.CmsDataAccessException;
import org.opencms.file.CmsResource;
import org.opencms.file.CmsUser;
import org.opencms.main.OpenCms;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Oracle implementation of the subscription driver.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 *  @since 8.0.0
 */
public class CmsSubscriptionDriver extends org.opencms.db.generic.CmsSubscriptionDriver {

    /**
     * @see org.opencms.db.generic.CmsSubscriptionDriver#initSqlManager(java.lang.String)
     */
    @Override
    public org.opencms.db.generic.CmsSqlManager initSqlManager(String classname) {

        return CmsSqlManager.getInstance(classname);
    }

    /**
     * @see org.opencms.db.generic.CmsSubscriptionDriver#markResourceAsVisitedBy(org.opencms.db.CmsDbContext, java.lang.String, org.opencms.file.CmsResource, org.opencms.file.CmsUser)
     */
    @Override
    public void markResourceAsVisitedBy(CmsDbContext dbc, String poolName, CmsResource resource, CmsUser user)
    throws CmsDataAccessException {

        boolean entryExists = false;
        CmsLogFilter filter = CmsLogFilter.ALL.includeType(CmsLogEntryType.USER_RESOURCE_VISITED).filterResource(
            resource.getStructureId()).filterUser(user.getId());
        // delete existing visited entry for the resource
        if (readLog(dbc, OpenCms.getSubscriptionManager().getPoolName(), filter).size() > 0) {
            entryExists = true;
            deleteLog(dbc, OpenCms.getSubscriptionManager().getPoolName(), filter);
        }

        // create new entry
        List<CmsLogEntry> newEntries = new ArrayList<CmsLogEntry>(1);
        CmsLogEntry entry = new CmsLogEntry(
            user.getId(),
            System.currentTimeMillis(),
            resource.getStructureId(),
            CmsLogEntryType.USER_RESOURCE_VISITED,
            new String[] {user.getName(), resource.getRootPath()});
        newEntries.add(entry);
        log(dbc, poolName, newEntries);

        if (!entryExists) {
            // new entry, check if maximum number of stored visited resources is exceeded
            PreparedStatement stmt = null;
            Connection conn = null;
            ResultSet res = null;
            int count = 0;

            try {
                conn = m_sqlManager.getConnection(poolName);
                stmt = m_sqlManager.getPreparedStatement(conn, dbc.currentProject(), "C_VISITED_USER_COUNT_2");

                stmt.setString(1, user.getId().toString());
                stmt.setInt(2, CmsLogEntryType.USER_RESOURCE_VISITED.getId());
                res = stmt.executeQuery();

                if (res.next()) {
                    count = res.getInt(1);
                    while (res.next()) {
                        // do nothing only move through all rows because of mssql odbc driver
                    }
                } else {
                    throw new CmsDbConsistencyException(Messages.get().container(
                        Messages.ERR_COUNTING_VISITED_RESOURCES_1,
                        user.getName()));
                }

                int maxCount = OpenCms.getSubscriptionManager().getMaxVisitedCount();
                if (count > maxCount) {
                    // delete old visited log entries
                    m_sqlManager.closeAll(dbc, null, stmt, res);
                    stmt = m_sqlManager.getPreparedStatement(
                        conn,
                        dbc.currentProject(),
                        "C_ORACLE_VISITED_USER_DELETE_5");

                    stmt.setString(1, user.getId().toString());
                    stmt.setInt(2, CmsLogEntryType.USER_RESOURCE_VISITED.getId());
                    stmt.setString(3, user.getId().toString());
                    stmt.setInt(4, CmsLogEntryType.USER_RESOURCE_VISITED.getId());
                    stmt.setInt(5, count - maxCount);
                    stmt.executeUpdate();
                }
            } catch (SQLException e) {
                throw new CmsDbSqlException(Messages.get().container(
                    Messages.ERR_GENERIC_SQL_1,
                    CmsDbSqlException.getErrorQuery(stmt)), e);
            } finally {
                m_sqlManager.closeAll(dbc, conn, stmt, res);
            }
        }
    }

}
