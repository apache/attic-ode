package org.apache.ode.axis2.util;

import javax.transaction.TransactionManager;

import org.objectweb.jotm.Jotm;

/**
 * @author mriou
 */
public class JotmFactory {

    public TransactionManager getTransactionManager() {
        try {
            Jotm jotm = new Jotm(true, false);
            jotm.getTransactionManager().setTransactionTimeout(30);
            return jotm.getTransactionManager();
        } catch (Exception e) {
            throw new RuntimeException("Couldn't initialize a proper transaction manager!", e);
        }
    }
}
