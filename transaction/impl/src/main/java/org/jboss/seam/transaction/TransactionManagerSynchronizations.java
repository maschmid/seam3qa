/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.seam.transaction;

import org.jboss.solder.logging.Logger;
import org.jboss.solder.bean.defaultbean.DefaultBean;

import javax.enterprise.context.ApplicationScoped;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 * Synchronizations implementation that registers synchronizations with a JTA {@link TransactionManager}
 */
@ApplicationScoped
@DefaultBean(Synchronizations.class)
public class TransactionManagerSynchronizations implements Synchronization, Synchronizations {
    private static final Logger log = Logger.getLogger(TransactionManagerSynchronizations.class);

    private final String[] JNDI_LOCATIONS = { "java:jboss/TransactionManager", "java:/TransactionManager",
            "java:appserver/TransactionManager", "java:comp/TransactionManager", "java:pm/TransactionManager" };

    /**
     * The location that the TM was found under JNDI. This is static, as it will not change between deployed apps on the same
     * JVM
     */
    private static volatile String foundJndiLocation;

    protected ThreadLocalStack<SynchronizationRegistry> synchronizations = new ThreadLocalStack<SynchronizationRegistry>();

    protected ThreadLocalStack<Transaction> transactions = new ThreadLocalStack<Transaction>();

    @Override
    public void beforeCompletion() {
        log.debug("beforeCompletion");
        SynchronizationRegistry sync = synchronizations.peek();
        sync.beforeTransactionCompletion();
    }

    @Override
    public void afterCompletion(final int status) {
        transactions.pop();
        log.debug("afterCompletion");
        synchronizations.pop().afterTransactionCompletion((Status.STATUS_COMMITTED & status) == 0);
    }

    @Override
    public boolean isAwareOfContainerTransactions() {
        return true;
    }

    @Override
    public void registerSynchronization(final Synchronization sync) {
        try {
            TransactionManager manager = getTransactionManager();
            Transaction transaction = manager.getTransaction();
            if (transactions.isEmpty() || transactions.peek().equals(transaction)) {
                transactions.push(transaction);
                synchronizations.push(new SynchronizationRegistry());
                transaction.registerSynchronization(this);
            }
            synchronizations.peek().registerSynchronization(sync);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public TransactionManager getTransactionManager() {
        if (foundJndiLocation != null) {
            try {
                return (TransactionManager) new InitialContext().lookup(foundJndiLocation);
            } catch (NamingException e) {
                log.trace("Could not find transaction manager under" + foundJndiLocation);
            }
        }
        for (String location : JNDI_LOCATIONS) {
            try {
                TransactionManager manager = (TransactionManager) new InitialContext().lookup(location);
                foundJndiLocation = location;
                return manager;
            } catch (NamingException e) {
                log.trace("Could not find transaction manager under" + location);
            }
        }
        throw new RuntimeException("Could not find TransactionManager in JNDI");
    }

    @Override
    public void afterTransactionBegin() {

    }

    @Override
    public void afterTransactionCompletion(final boolean success) {

    }

    @Override
    public void beforeTransactionCommit() {

    }
}
