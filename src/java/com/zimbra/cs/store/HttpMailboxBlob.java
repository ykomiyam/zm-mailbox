/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2004, 2005, 2006, 2007, 2008, 2009 Zimbra, Inc.
 * 
 * The contents of this file are subject to the Yahoo! Public License
 * Version 1.0 ("License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 * http://www.zimbra.com/license.
 * 
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied.
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.cs.store;

import java.io.IOException;

import com.zimbra.common.service.ServiceException;
import com.zimbra.cs.mailbox.Mailbox;

public class HttpMailboxBlob extends MailboxBlob {
    protected HttpMailboxBlob(Mailbox mbox, int itemId, int revision, String locator) {
        super(mbox, itemId, revision, locator);
    }

    @Override public Blob getLocalBlob() throws IOException {
        StoreManager sm = StoreManager.getInstance();

        LocalBlobCache blobcache = ((HttpStoreManager) sm).getBlobCache();
        Blob blob = blobcache.get(this);
        if (blob != null)
            return blob;

        try {
            blob = sm.storeIncoming(sm.getContent(this), mSize == null ? -1 : mSize.intValue(), null);
            setSize(blob.getRawSize());
            if (mDigest != null)
                setDigest(blob.getDigest());
            return blobcache.cache(this, blob);
        } catch (ServiceException e) {
            throw new IOException("fetching local blob: " + e);
        }
    }

    @Override public int hashCode() {
        return getLocator().hashCode();
    }

    @Override public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof HttpMailboxBlob))
            return false;
        return getLocator().equals(((HttpMailboxBlob) other).getLocator());
    }
}
