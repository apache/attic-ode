/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ode.daohib.bpel;

import java.util.Date;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;

import org.apache.ode.bpel.dao.MessageDAO;
import org.apache.ode.bpel.dao.MessageExchangeDAO;
import org.apache.ode.bpel.dao.PartnerLinkDAO;
import org.apache.ode.bpel.dao.ProcessDAO;
import org.apache.ode.bpel.dao.ProcessInstanceDAO;
import org.apache.ode.daohib.SessionManager;
import org.apache.ode.daohib.bpel.hobj.HMessage;
import org.apache.ode.daohib.bpel.hobj.HMessageExchange;
import org.apache.ode.daohib.bpel.hobj.HProcess;
import org.apache.ode.daohib.bpel.hobj.HProcessInstance;
import org.apache.ode.daohib.hobj.HLargeData;
import org.apache.ode.utils.DOMUtils;

public class MessageExchangeDaoImpl extends HibernateDao implements MessageExchangeDAO {

    private HMessageExchange _hself;

    public MessageExchangeDaoImpl(SessionManager sm, HMessageExchange mex) {
        super(sm, mex);
        _hself = mex;
    }

    public String getMessageExchangeId() {
        return _hself.getId().toString();
    }

    public MessageDAO getResponse() {
        return _hself.getResponse() == null ? null : new MessageDaoImpl(_sm, _hself.getResponse());
    }

    public Date getCreateTime() {
        return _hself.getInsertTime();
    }

    public MessageDAO getRequest() {
        return _hself.getRequest() == null ? null : new MessageDaoImpl(_sm, _hself.getRequest());
    }

    public String getOperation() {
        return _hself.getOperationName();
    }

    public QName getPortType() {
        return _hself.getPortType() == null ? null : QName.valueOf(_hself.getPortType());
    }

    public void setPortType(QName porttype) {
        _hself.setPortType(porttype == null ? null : porttype.toString());
    }

    public void setStatus(String status) {
        _hself.setState(status);
    }

    public String getStatus() {
        return _hself.getState();
    }

    public MessageDAO createMessage(QName type) {
        HMessage message = new HMessage();
        message.setType(type == null ? null : type.toString());
        message.setCreated(new Date());
        message.setMessageExchange(_hself);
        getSession().save(message);
        return new MessageDaoImpl(_sm, message);

    }

    public void setRequest(MessageDAO msg) {
        _hself.setRequest(msg == null ? null : (HMessage) ((MessageDaoImpl) msg).getHibernateObj());

    }

    public void setResponse(MessageDAO msg) {
        _hself.setResponse(msg == null ? null : (HMessage) ((MessageDaoImpl) msg).getHibernateObj());
    }

    public int getPartnerLinkModelId() {
        return _hself.getPartnerLinkModelId();
    }

    public void setPartnerLinkModelId(int modelId) {
        _hself.setPartnerLinkModelId(modelId);
    }

    /**
     * @see org.apache.ode.sfwk.bapi.dao.MessageExchangeDAO#getCorrelationId()
     */
    public String getCorrelationId() {
        return _hself.getClientKey();
    }

    /**
     * @see org.apache.ode.sfwk.bapi.dao.MessageExchangeDAO#setCorrelationId(byte[])
     */
    public void setCorrelationId(String clientKey) {
        _hself.setClientKey(clientKey);
        update();
    }

    public void setPattern(String pattern) {
        _hself.setPattern(pattern);
        update();

    }

    public void setOperation(String opname) {
        _hself.setOperationName(opname);
        update();
    }

    public void setEPR(Element source) {
        if (source == null)
            _hself.setEndpoint(null);
        else {
            HLargeData ld = new HLargeData(DOMUtils.domToString(source));
            getSession().save(ld);
            _hself.setEndpoint(ld);
        }

        getSession().saveOrUpdate(_hself);

    }

    public Element getEPR() {
        HLargeData ld = _hself.getEndpoint();
        if (ld == null)
            return null;
        try {
            return DOMUtils.stringToDOM(ld.getText());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setCallbackEPR(Element source) {
        if (source == null)
            _hself.setCallbackEndpoint(null);
        else {
            HLargeData ld = new HLargeData(DOMUtils.domToString(source));
            getSession().save(ld);
            _hself.setCallbackEndpoint(ld);
        }

        getSession().saveOrUpdate(_hself);

    }

    public Element getCallbackEPR() {
        HLargeData ld = _hself.getCallbackEndpoint();
        if (ld == null)
            return null;
        try {
            return DOMUtils.stringToDOM(ld.getText());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getPattern() {
        return _hself.getPattern();
    }

    public String getChannel() {
        return _hself.getChannelName();
    }

    public void setChannel(String channel) {
        _hself.setChannelName(channel);
        update();
    }

    public boolean getPropagateTransactionFlag() {
        // TODO Auto-generated method stub
        return false;
    }

    public String getFault() {
        return _hself.getFault();
    }

    public void setFault(String faultType) {
        _hself.setFault(faultType);
        update();
    }

    public void setCorrelationStatus(String cstatus) {
        _hself.setCorrelationStatus(cstatus);
        update();
    }

    public String getCorrelationStatus() {
        return _hself.getCorrelationStatus();
    }

    public ProcessDAO getProcess() {
        return _hself.getProcess() == null ? null : new ProcessDaoImpl(_sm, _hself.getProcess());
    }

    public void setProcess(ProcessDAO process) {
        _hself.setProcess(process == null ? null : (HProcess) ((ProcessDaoImpl) process).getHibernateObj());
        update();
    }

    public void setInstance(ProcessInstanceDAO instance) {
        _hself.setInstance(instance == null ? null : (HProcessInstance) ((ProcessInstanceDaoImpl) instance)
                .getHibernateObj());
        update();
    }

    public ProcessInstanceDAO getInstance() {
        return _hself.getInstance() == null ? null : new ProcessInstanceDaoImpl(_sm, _hself.getInstance());
    }

    public char getDirection() {
        return _hself.getDirection();
    }

    public QName getCallee() {
        String callee = _hself.getCallee();
        return callee == null ? null : QName.valueOf(callee);
    }

    public void setCallee(QName callee) {
        _hself.setCallee(callee == null ? null : callee.toString());
        update();
    }

    public String getProperty(String key) {
        return _hself.getProperties().get(key);
    }

    public void setProperty(String key, String value) {
        _hself.getProperties().put(key, value);
        //getSession().update(_hself.getProperties()); // Does not appear to work ?
        update();
    }

    public void setPartnerLink(PartnerLinkDAO plinkDAO) {
        _hself.setPartnerLink(((PartnerLinkDAOImpl) plinkDAO)._self);
        update();
    }

    public PartnerLinkDAO getPartnerLink() {
        return new PartnerLinkDAOImpl(_sm, _hself.getPartnerLink());
    }

}
