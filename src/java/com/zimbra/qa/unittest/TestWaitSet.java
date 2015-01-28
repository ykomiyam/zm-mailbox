/*
 * ***** BEGIN LICENSE BLOCK *****
 * Zimbra Collaboration Suite Server
 * Copyright (C) 2007, 2008, 2009, 2010, 2011, 2013, 2014 Zimbra, Inc.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software Foundation,
 * version 2 of the License.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 * ***** END LICENSE BLOCK *****
 */
package com.zimbra.qa.unittest;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.zimbra.client.ZFolder;
import com.zimbra.common.localconfig.LC;
import com.zimbra.common.util.Pair;
import com.zimbra.cs.account.Account;
import com.zimbra.cs.account.Provisioning;
import com.zimbra.cs.service.mail.WaitSetRequest;
import com.zimbra.cs.service.mail.WaitSetRequest.TypeEnum;
import com.zimbra.cs.session.IWaitSet;
import com.zimbra.cs.session.WaitSetAccount;
import com.zimbra.cs.session.WaitSetError;
import com.zimbra.cs.session.WaitSetMgr;
import com.zimbra.cs.util.ProvisioningUtil;

/**
 *
 */
public class TestWaitSet  {

    private static final String NAME_PREFIX = TestWaitSet.class.getSimpleName();
    private static final String WS_USER_NAME = NAME_PREFIX + "_ws_test_user";
    private static final String USER_1_NAME = NAME_PREFIX + "_user1";
    private static final String USER_2_NAME = NAME_PREFIX + "_user2";
    private boolean originalLCSetting = false;

    private static final String FAKE_ACCOUNT_ID = "fake";

    @Before
    public void setUp() throws Exception {
        cleanUp();
    	originalLCSetting = ProvisioningUtil.getServerAttribute(Provisioning.A_zimbraIndexManualCommit, true);
        Provisioning.getInstance().getLocalServer().setIndexManualCommit(true);
        TestUtil.createAccount(USER_1_NAME);
        TestUtil.createAccount(USER_2_NAME);
    }

    public void cleanUp()
    throws Exception {
        if(TestUtil.accountExists(USER_1_NAME)) {
            TestUtil.deleteAccount(USER_1_NAME);
        }
        if(TestUtil.accountExists(USER_2_NAME)) {
            TestUtil.deleteAccount(USER_2_NAME);
        }
        if(TestUtil.accountExists(WS_USER_NAME)) {
            TestUtil.deleteAccount(WS_USER_NAME);
        }
    }

    @Test
    public void testWaitSets() throws Exception {
        runMeFirst();
        runMeSecond();
    }

    private void runMeFirst() throws Exception {
        String waitSetId;
        List<WaitSetError> errors;

        {
            Account user1Acct = TestUtil.getAccount(USER_1_NAME);
            List<WaitSetAccount> add = new ArrayList<WaitSetAccount>();
            add.add(new WaitSetAccount(user1Acct.getId(), null, TypeEnum.m.getTypes()));

            Pair<String, List<WaitSetError>> result =
                WaitSetMgr.create(FAKE_ACCOUNT_ID, true, TypeEnum.m.getTypes(), false, add);
            waitSetId = result.getFirst();
            errors = result.getSecond();
        }

        try {
            String curSeqNo = "0";
            Assert.assertEquals(0, errors.size());

            { // waitset shouldn't signal until message added to a mailbox
                WaitSetRequest.Callback cb = new WaitSetRequest.Callback();

                // wait shouldn't find anything yet
                IWaitSet ws = WaitSetMgr.lookup(waitSetId);
                errors = ws.doWait(cb, "0", null, null);
                Assert.assertEquals(0, errors.size());
                synchronized(cb) { Assert.assertEquals(false, cb.completed); }

                // inserting a message to existing account should trigger waitset
                String sender = TestUtil.getAddress(USER_1_NAME);
                String recipient = TestUtil.getAddress(USER_1_NAME);
                String subject = NAME_PREFIX + " testWaitSet 1";
                TestUtil.addMessageLmtp(subject, recipient, sender);
                try { Thread.sleep(500); } catch (Exception e) {}
                synchronized(cb) { Assert.assertEquals(true, cb.completed); }
                curSeqNo = cb.seqNo;
            }

            { // waitset should pick up added user
                WaitSetRequest.Callback cb = new WaitSetRequest.Callback();

                IWaitSet ws = WaitSetMgr.lookup(waitSetId);

                // create a new account, shouldn't trigger waitset
                Account user2Acct = TestUtil.getAccount(USER_2_NAME);
                List<WaitSetAccount> add2 = new ArrayList<WaitSetAccount>();
                add2.add(new WaitSetAccount(user2Acct.getId(), null, TypeEnum.m.getTypes()));
                errors = ws.doWait(cb, curSeqNo, add2, null);
                // wait shouldn't find anything yet
                Assert.assertEquals(0, errors.size());
                synchronized(cb) { Assert.assertEquals(false, cb.completed); }

                // adding a message to the new account SHOULD trigger waitset
                String sender = TestUtil.getAddress(WS_USER_NAME);
                String recipient = TestUtil.getAddress(USER_2_NAME);
                String subject = NAME_PREFIX + " testWaitSet 3";
                TestUtil.addMessageLmtp(subject, recipient, sender);
                try { Thread.sleep(500); } catch (Exception e) {}
                synchronized(cb) { Assert.assertEquals(true, cb.completed); }
                curSeqNo = cb.seqNo;
            }
        } finally {
            WaitSetMgr.destroy(null, FAKE_ACCOUNT_ID, waitSetId);
        }
    }

    public void runMeSecond() throws Exception {
        Pair<String, List<WaitSetError>> result =
            WaitSetMgr.create(FAKE_ACCOUNT_ID, true, TypeEnum.all.getTypes(), true, null);

        String waitSetId = result.getFirst();
        String curSeqNo = "0";
        List<WaitSetError> errors = result.getSecond();
        Assert.assertEquals(0, errors.size());

        try {

            { // waitset shouldn't signal until message added to a mailbox
                WaitSetRequest.Callback cb = new WaitSetRequest.Callback();

                // wait shouldn't find anything yet
                IWaitSet ws = WaitSetMgr.lookup(waitSetId);
                errors = ws.doWait(cb, "0", null, null);
                Assert.assertEquals(0, errors.size());
                synchronized(cb) { Assert.assertEquals(false, cb.completed); }

                // inserting a message to existing account should trigger waitset
                String sender = TestUtil.getAddress(USER_1_NAME);
                String recipient = TestUtil.getAddress(USER_1_NAME);
                String subject = NAME_PREFIX + " testWaitSet 1";
                TestUtil.addMessageLmtp(subject, recipient, sender);
                try { Thread.sleep(500); } catch (Exception e) {}
                synchronized(cb) { Assert.assertEquals(true, cb.completed); }
                curSeqNo = cb.seqNo;
            }

            { // waitset should remain signalled until sequence number is increased
                WaitSetRequest.Callback cb = new WaitSetRequest.Callback();
                IWaitSet ws = WaitSetMgr.lookup(waitSetId);
                errors = ws.doWait(cb, "0", null, null);
                try { Thread.sleep(500); } catch (Exception e) {}
                Assert.assertEquals(0, errors.size());
                synchronized(cb) { Assert.assertEquals(true, cb.completed); }
                curSeqNo = cb.seqNo;
            }

            { // waitset shouldn't signal until a document is added
                WaitSetRequest.Callback cb = new WaitSetRequest.Callback();
                // wait shouldn't find anything yet
                IWaitSet ws = WaitSetMgr.lookup(waitSetId);
                errors = ws.doWait(cb, curSeqNo, null, null);
                Assert.assertEquals(0, errors.size());
                synchronized(cb) { Assert.assertEquals(false, cb.completed); }

                // creating a document in existing account should trigger waitset
                String subject = NAME_PREFIX + " testWaitSet document 1";
                TestUtil.createDocument(TestUtil.getZMailbox(USER_2_NAME),
                        ZFolder.ID_BRIEFCASE, subject, "text/plain", "Hello, world!".getBytes());
                try { Thread.sleep(500); } catch (Exception e) {}
                synchronized(cb) { Assert.assertEquals("document waitset", true, cb.completed); }
                curSeqNo = cb.seqNo;
            }

            { // part 2: waitset for "all" should pick up new account added
                WaitSetRequest.Callback cb = new WaitSetRequest.Callback();

                // wait shouldn't find anything yet
                IWaitSet ws = WaitSetMgr.lookup(waitSetId);
                errors = ws.doWait(cb, curSeqNo, null, null);
                Assert.assertEquals(0, errors.size());
                synchronized(cb) { Assert.assertEquals(false, cb.completed); }

                // create a new account, shouldn't trigger waitset
                TestUtil.createAccount(WS_USER_NAME);
                synchronized(cb) { Assert.assertEquals(false, cb.completed); }

                // adding a message to the new account SHOULD trigger waitset
                String sender = TestUtil.getAddress(WS_USER_NAME);
                String recipient = TestUtil.getAddress(WS_USER_NAME);
                String subject = NAME_PREFIX + " testWaitSet 2";
                TestUtil.addMessageLmtp(subject, recipient, sender);
                try { Thread.sleep(500); } catch (Exception e) {}
                synchronized(cb) { Assert.assertEquals(true, cb.completed); }
                curSeqNo = cb.seqNo;
            }
        } finally {
            WaitSetMgr.destroy(null, FAKE_ACCOUNT_ID, waitSetId);
        }
    }

    @After
    public void tearDown() throws Exception {
        cleanUp();
        Provisioning.getInstance().getLocalServer().setIndexManualCommit(originalLCSetting);
    }
}
