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

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;

import org.jboss.solder.logging.Logger;
import org.jboss.solder.core.Veto;

/**
 * Wraps JTA transaction management in a Seam UserTransaction interface.
 *
 * @author Mike Youngstrom
 * @author Gavin King
 */
@Veto
public class UTTransaction extends AbstractUserTransaction {
    private static final Logger log = Logger.getLogger(UTTransaction.class);

    private final javax.transaction.UserTransaction delegate;

    UTTransaction(javax.transaction.UserTransaction delegate, Synchronizations sync) {
        this.setSynchronizations(sync);
        this.delegate = delegate;
        if (delegate == null) {
            throw new IllegalArgumentException("null UserTransaction");
        }
    }

    public void begin() throws NotSupportedException, SystemException {
        log.debug("beginning JTA transaction");
        delegate.begin();
        getSynchronizations().afterTransactionBegin();
    }

    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        log.debug("committing JTA transaction");
        boolean success = false;
        Synchronizations synchronizations = getSynchronizations();
        synchronizations.beforeTransactionCommit();
        try {
            delegate.commit();
            success = true;
        } finally {
            synchronizations.afterTransactionCompletion(success);
        }
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        log.debug("rolling back JTA transaction");
        try {
            delegate.rollback();
        } finally {
            getSynchronizations().afterTransactionCompletion(false);
        }
    }

    public int getStatus() throws SystemException {
        return delegate.getStatus();
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        delegate.setRollbackOnly();
    }

    public void setTransactionTimeout(int timeout) throws SystemException {
        delegate.setTransactionTimeout(timeout);
    }

    @Override
    public void registerSynchronization(Synchronization sync) {
        getSynchronizations().registerSynchronization(sync);
    }

}
