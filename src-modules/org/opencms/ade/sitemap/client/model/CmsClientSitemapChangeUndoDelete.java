/*
 * File   : $Source: /alkacon/cvs/opencms/src-modules/org/opencms/ade/sitemap/client/model/Attic/CmsClientSitemapChangeUndoDelete.java,v $
 * Date   : $Date: 2010/12/17 08:45:30 $
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

package org.opencms.ade.sitemap.client.model;

import org.opencms.ade.sitemap.client.CmsSitemapTreeItem;
import org.opencms.ade.sitemap.client.CmsSitemapView;
import org.opencms.ade.sitemap.client.control.CmsSitemapController;
import org.opencms.ade.sitemap.shared.CmsClientSitemapEntry;
import org.opencms.xml.sitemap.CmsDetailPageInfo;
import org.opencms.xml.sitemap.CmsDetailPageTable;

/**
 * Sitemap change object for undoing a deletion.<p>
 * 
 * @author Georg Westenberger
 * 
 * @version $Revision: 1.1 $
 * 
 * @since 8.0.0
 */
public class CmsClientSitemapChangeUndoDelete extends CmsClientSitemapChangeNew {

    /** The sitemap tree item which was deleted. */
    protected CmsSitemapTreeItem m_treeItem;

    /** The detail page info bean. */
    private CmsDetailPageInfo m_detailPageInfo;

    /** The original detail page index. */
    private int m_originalDetailPageIndex;

    /**
     * Creates a new change for a given sitemap entry.<p>
     * 
     * @param entry the sitemap entry 
     */
    public CmsClientSitemapChangeUndoDelete(CmsClientSitemapEntry entry) {

        super(entry);
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeNew#applyToModel(org.opencms.ade.sitemap.client.control.CmsSitemapController)
     */
    @Override
    public void applyToModel(CmsSitemapController controller) {

        super.applyToModel(controller);
        controller.getData().getClipboardData().getDeletions().remove(getEntry());
        CmsDetailPageTable detailPages = controller.getDetailPageTable();
        if (m_detailPageInfo != null) {
            controller.addDetailPageInfo(m_detailPageInfo);
            detailPages.move(m_detailPageInfo.getId(), m_originalDetailPageIndex);
        }
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeNew#applyToView(org.opencms.ade.sitemap.client.CmsSitemapView)
     */
    @Override
    public void applyToView(CmsSitemapView view) {

        super.applyToView(view);
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeNew#revert()
     */
    @Override
    public I_CmsClientSitemapChange revert() {

        throw new UnsupportedOperationException();
    }

    /**
     * Sets the detail page info which should be restored on Undo.<p>
     * 
     * @param detailPageInfo the detail page information bean 
     * @param originalDetailPageIndex the original detail page index 
     */
    public void setDetailPageInfo(CmsDetailPageInfo detailPageInfo, int originalDetailPageIndex) {

        m_detailPageInfo = detailPageInfo;
        m_originalDetailPageIndex = originalDetailPageIndex;
    }

    /**
     * Sets the corresponding tree item from a delete operation.<p>
     * 
     * @param treeItem the item to set
     */
    @Override
    public void setTreeItem(CmsSitemapTreeItem treeItem) {

        m_treeItem = treeItem;
    }

    /**
     * @see org.opencms.ade.sitemap.client.model.CmsClientSitemapChangeNew#getTreeItem()
     */
    @Override
    protected CmsSitemapTreeItem getTreeItem() {

        return m_treeItem;
    }

}