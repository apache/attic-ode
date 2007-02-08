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

package org.apache.ode.axis2.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Part;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.extensions.ElementExtensible;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPHeader;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultDetail;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ode.axis2.Messages;
import org.apache.ode.utils.DOMUtils;
import org.apache.ode.utils.stl.CollectionsX;
import org.w3c.dom.Element;

/**
 * SOAP/ODE Message converter. Uses WSDL binding information to convert the protocol-neutral ODE representation into a SOAP
 * representation and vice versa.
 * 
 * @author Maciej Szefler ( m s z e f l e r (at) g m a i l . c o m )
 * 
 */
public class SoapMessageConverter {

    private static final Messages __msgs = Messages.getMessages(Messages.class);

    private static final Log __log = LogFactory.getLog(SoapMessageConverter.class);

    /** Namespace (in the ODE <message>) for parts that are in the header but not in the payload message. */
    private static final String FOREIGN_HEADER_PART_NS = "urn:ode.apache.org/axis2-il/header";

    SOAPFactory _soapFactory;

    Definition _def;

    QName _serviceName;

    String _portName;

    Service _serviceDef;

    Binding _binding;

    Port _port;

    boolean _isRPC;

    private SOAPBinding _soapBinding;

    public SoapMessageConverter(SOAPFactory soapFactory, Definition def, QName serviceName, String portName,
            boolean replicateEmptyNS) throws AxisFault {
        if (soapFactory == null)
            throw new NullPointerException("Null soapFactory");
        if (def == null)
            throw new NullPointerException("Null wsdl def.");
        if (serviceName == null)
            throw new NullPointerException("Null serviceName");
        if (portName == null)
            throw new NullPointerException("Null portName");

        _soapFactory = soapFactory;
        _def = def;

        _serviceDef = _def.getService(serviceName);
        if (_serviceDef == null)
            throw new AxisFault(__msgs.msgServiceDefinitionNotFound(serviceName));
        _port = _serviceDef.getPort(portName);
        if (_port == null)
            throw new AxisFault(__msgs.msgPortDefinitionNotFound(serviceName, portName));
        _binding = _port.getBinding();
        if (_binding == null)
            throw new AxisFault(__msgs.msgBindingNotFound(serviceName, portName));


        Collection<SOAPBinding> soapBindings = CollectionsX.filter(_binding.getExtensibilityElements(), SOAPBinding.class);
        if (soapBindings.isEmpty())
            throw new AxisFault(__msgs.msgNoSOAPBindingForPort(_portName));
        else if (soapBindings.size() > 1) {
            throw new AxisFault(__msgs.msgMultipleSoapBindingsForPort(_portName));
        }

        _soapBinding = (SOAPBinding) soapBindings.iterator().next();
        String style = _soapBinding.getStyle();
        _isRPC = style != null && style.equals("rpc");
    }

    @SuppressWarnings("unchecked")
    public void createSoapRequest(SOAPEnvelope soapEnv, Element message, Operation op) throws AxisFault {
        if (op == null)
            throw new NullPointerException("Null operation");
        if (message == null)
            throw new NullPointerException("Null message.");
        if (soapEnv == null)
            throw new NullPointerException("Null soapEnv");

        BindingOperation bop = _binding.getBindingOperation(op.getName(), null, null);

        if (bop == null)
            throw new AxisFault(__msgs.msgBindingOperationNotFound(_serviceName, _portName, op.getName()));

        BindingInput bi = bop.getBindingInput();
        if (bi == null)
            throw new AxisFault(__msgs.msgBindingInputNotFound(_serviceName, _portName, op.getName()));

        List<SOAPHeader> soapHeaders = getSOAPHeaders(bi);
        for (SOAPHeader sh : soapHeaders)
            createSoapHeader(soapEnv, sh, op.getInput().getMessage(), message);

        SOAPBody soapBody = getSOAPBody(bi);
        if (soapBody != null) {
            org.apache.axiom.soap.SOAPBody sb = _soapFactory.createSOAPBody(soapEnv);
            createSoapBody(sb, soapBody, op.getInput().getMessage(), message, op.getName());
        }
        
    }

    public void createSoapResponse(SOAPEnvelope soapEnv, Element message, Operation op) throws AxisFault {
        if (op == null)
            throw new NullPointerException("Null operation");
        if (message == null)
            throw new NullPointerException("Null message.");
        if (soapEnv == null)
            throw new NullPointerException("Null soapEnv");

        BindingOperation bop = _binding.getBindingOperation(op.getName(),null,null);

        if (bop == null)
            throw new AxisFault(__msgs.msgBindingOperationNotFound(_serviceName, _portName, op.getName()));

        BindingOutput bo = bop.getBindingOutput();
        if (bo == null)
            throw new AxisFault(__msgs.msgBindingOutputNotFound(_serviceName, _portName, op.getName()));

        List<SOAPHeader> soapHeaders = getSOAPHeaders(bo);
        for (SOAPHeader sh : soapHeaders)
            createSoapHeader(soapEnv, sh, op.getInput().getMessage(), message);

        SOAPBody soapBody = getSOAPBody(bo);
        if (soapBody != null) {
            org.apache.axiom.soap.SOAPBody sb = _soapFactory.createSOAPBody(soapEnv);
            createSoapBody(sb, soapBody, op.getInput().getMessage(), message, op.getName() + "Response");
        }
        
        
    }

    @SuppressWarnings("unchecked")
    public void createSoapHeader(SOAPEnvelope soapEnv, SOAPHeader headerdef, Message msgdef, Element message) throws AxisFault {
        boolean payloadMessageHeader = headerdef.getMessage() == null || headerdef.getMessage().equals(msgdef.getQName());

        if (headerdef.getPart() == null)
            return;
        
        if (payloadMessageHeader && msgdef.getPart(headerdef.getPart()) == null)
            throw new AxisFault(__msgs.msgSoapHeaderReferencesUnkownPart(headerdef.getPart()));

        Element srcPartEl = DOMUtils.findChildByName(message, new QName(payloadMessageHeader 
                ? null : FOREIGN_HEADER_PART_NS, headerdef.getPart()));

        // We don't complain about missing header data unless they are part of the message payload. This is
        // because AXIS may be providing these headers.
        if (srcPartEl == null && payloadMessageHeader)
            throw new AxisFault(__msgs.msgOdeMessageMissingRequiredPart(headerdef.getPart()));
        
        if (srcPartEl == null)
            return;
        
        org.apache.axiom.soap.SOAPHeader soaphdr = soapEnv.getHeader();
        if (soaphdr == null) {
            soaphdr = _soapFactory.createSOAPHeader(soapEnv);
        }
        
        OMElement omPart = OMUtils.toOM(srcPartEl, _soapFactory);
        for (Iterator<OMNode> i = omPart.getChildren(); i.hasNext();)
            soaphdr.addChild(i.next());

    }

    public OMElement createSoapFault(Element message, QName faultName, Operation op) throws AxisFault {
        if (faultName.getNamespaceURI() == null || !faultName.getNamespaceURI().equals(_def.getTargetNamespace()))
            throw new AxisFault(__msgs.msgUndefinedFault(_serviceName, _portName, op.getName(), faultName));
        Fault f = op.getFault(faultName.getLocalPart());
        if (f == null)
            throw new AxisFault(__msgs.msgUndefinedFault(_serviceName, _portName, op.getName(), faultName));
        
        // For faults, there will be exactly one part. 
        Part p = (Part)f.getMessage().getParts().values().iterator().next();
        Element partEl= DOMUtils.getFirstChildElement(DOMUtils.findChildByName(message,new QName(null,p.getName())));
        if (partEl == null)
            throw new AxisFault(__msgs.msgOdeMessageMissingRequiredPart(p.getName()));
        if (p == null)
            throw new AxisFault(new IllegalStateException("fault part is non-element" + p.getName()));
        Element detail = DOMUtils.findChildByName(partEl, p.getElementName());
        if (detail == null)
            throw new AxisFault(__msgs.msgOdeMessagePartMissingRequiredElement(_serviceName, _portName, op.getName(), p.getElementName()));
        
        return OMUtils.toOM(detail, _soapFactory);
   }

    public void parseSoapRequest(Element odeMessage, SOAPEnvelope envelope, Operation op) throws AxisFault {
        BindingOperation bop = _binding.getBindingOperation(op.getName(), null, null);

        if (bop == null)
            throw new AxisFault(__msgs.msgBindingOperationNotFound(_serviceName, _portName, op.getName()));
        
        BindingInput bi = bop.getBindingInput();
        if (bi == null)
            throw new AxisFault(__msgs.msgBindingInputNotFound(_serviceName, _portName, op.getName()));

        SOAPBody soapBody = getSOAPBody(bi);
        if (soapBody != null)
            extractSoapBodyParts(odeMessage, envelope.getBody(), soapBody, op.getInput().getMessage(), op.getName());

        List<SOAPHeader> soapHeaders = getSOAPHeaders(bi);
        for (SOAPHeader sh : soapHeaders)
            extractSoapHeaderPart(odeMessage, envelope.getHeader(), sh, op.getInput().getMessage());

    }

    public void parseSoapResponse(Element odeMessage, SOAPEnvelope envelope, Operation op) throws AxisFault {
        BindingOperation bop = _binding.getBindingOperation(op.getName(), null, null);

        if (bop == null)
            throw new AxisFault(__msgs.msgBindingOperationNotFound(_serviceName, _portName, op.getName()));

        BindingOutput bo = bop.getBindingOutput();
        if (bo == null)
            throw new AxisFault(__msgs.msgBindingInputNotFound(_serviceName, _portName, op.getName()));

        SOAPBody soapBody = getSOAPBody(bo);
        if (soapBody != null)
            extractSoapBodyParts(odeMessage, envelope.getBody(), soapBody, op.getOutput().getMessage(), op.getName() + "Response");
    }

    @SuppressWarnings("unchecked")
    public void createSoapBody(org.apache.axiom.soap.SOAPBody sb, 
            SOAPBody soapBody, 
            Message msgDef, 
            Element message,
            String rpcWrapper) throws AxisFault {
        
        OMElement partHolder;
        if (_isRPC) {
            partHolder = _soapFactory.createOMElement(new QName(soapBody.getNamespaceURI(),rpcWrapper), sb);
        } else
            partHolder = sb; 

        List<Part> parts = msgDef.getOrderedParts(soapBody.getParts());

        for (Part part : parts) {
            Element srcPartEl = DOMUtils.findChildByName(message, new QName(null, part.getName()));
            if (srcPartEl == null)
                throw new AxisFault(__msgs.msgOdeMessageMissingRequiredPart(part.getName()));

            OMElement omPart = OMUtils.toOM(srcPartEl, _soapFactory);
            if (_isRPC) {
                partHolder.addChild(omPart);
            } else {
                for (Iterator<OMNode> i = omPart.getChildren(); i.hasNext();)
                    partHolder.addChild(i.next());
            }
        }

    }

    // public Element createODEMessage(SOAPEnvelope soapEnv,Operation op) throws AxisFault {
    // }
    @SuppressWarnings("unchecked")
    public void extractSoapBodyParts(Element message, org.apache.axiom.soap.SOAPBody soapBody, SOAPBody bodyDef, Message msg,String rpcWrapper)
            throws AxisFault {

        List<Part> bodyParts = msg.getOrderedParts(bodyDef.getParts());

        if (_isRPC) {
            QName rpcWrapQName = new QName(bodyDef.getNamespaceURI(), rpcWrapper);
            OMElement partWrapper = soapBody.getFirstChildWithName(rpcWrapQName);
            if (partWrapper == null)
                throw new AxisFault(__msgs.msgSoapBodyDoesNotContainExpectedPartWrapper(_serviceName,_portName,rpcWrapQName));
            // In RPC the body element is the operation name, wrapping parts. Order doesn't really matter as far as
            // we're concerned. All we need to do is copy the soap:body children, since doc-lit rpc looks the same
            // in ode and soap.
            for (Part pdef : bodyParts) {
                OMElement srcPart = partWrapper.getFirstChildWithName(new QName(null, pdef.getName()));
                if (srcPart == null)
                    throw new AxisFault(__msgs.msgSOAPBodyDoesNotContainRequiredPart(pdef.getName()));
                message.appendChild(message.getOwnerDocument().importNode(OMUtils.toDOM(srcPart), true));
            }

        } else {
            // In doc-literal style, we expect the elements in the body to correspond (in order) to the
            // parts defined in the binding. All the parts should be element-typed, otherwise it is a mess.
            Iterator<OMElement> srcParts = soapBody.getChildElements();
            for (Part partDef : bodyParts) {
                if (!srcParts.hasNext())
                    throw new AxisFault(__msgs.msgSOAPBodyDoesNotContainRequiredPart(partDef.getName()));

                OMElement srcPart = srcParts.next();
                if (partDef.getElementName() == null)
                    throw new AxisFault(__msgs.msgBindingDefinesNonElementDocListParts());
                if (!srcPart.getQName().equals(partDef.getElementName()))
                    throw new AxisFault(__msgs.msgUnexpectedElementInSOAPBody(srcPart.getQName(), partDef.getElementName()));
                Element destPart = message.getOwnerDocument().createElementNS(null, partDef.getName());
                message.appendChild(destPart);
                destPart.appendChild(message.getOwnerDocument().importNode(OMUtils.toDOM(srcPart), true));
            }
        }
    }

    public void extractSoapHeaderPart(Element odeMessage, org.apache.axiom.soap.SOAPHeader header, SOAPHeader headerdef,
            Message msgType) throws AxisFault {
        // Is this header part of the "payload" messsage?
        boolean payloadMessageHeader = headerdef.getMessage() == null || headerdef.getMessage().equals(msgType.getQName());
        boolean requiredHeader = payloadMessageHeader || (headerdef.getRequired() != null && headerdef.getRequired() == true);

        if (requiredHeader && header == null)
            throw new AxisFault(__msgs.msgSoapHeaderMissingRequiredElement(headerdef.getElementType()));

        if (header == null)
            return;

        Message hdrMsg = _def.getMessage(headerdef.getMessage());
        if (hdrMsg == null)
            return;
        Part p = hdrMsg.getPart(headerdef.getPart());
        if (p == null || p.getElementName() == null)
            return;
        
        OMElement headerEl = header.getFirstChildWithName(p.getElementName());
        if (requiredHeader && headerEl == null)
            throw new AxisFault(__msgs.msgSoapHeaderMissingRequiredElement(headerdef.getElementType()));

        if (headerEl == null)
            return;

        Element destPart = odeMessage.getOwnerDocument().createElementNS(
                payloadMessageHeader ? null : FOREIGN_HEADER_PART_NS, headerdef.getPart());
        odeMessage.appendChild(destPart);
        destPart.appendChild(odeMessage.getOwnerDocument().importNode(OMUtils.toDOM(headerEl), true));

    }

    public static SOAPBody getSOAPBody(ElementExtensible ee) {
        return getFirstExtensibilityElement(ee, SOAPBody.class);
    }

    @SuppressWarnings("unchecked")
    public static List<SOAPHeader> getSOAPHeaders(ElementExtensible eee) {
        return CollectionsX.filter(new ArrayList<SOAPHeader>(), (Collection<Object>) eee.getExtensibilityElements(),
                SOAPHeader.class);
    }

    public static <T> T getFirstExtensibilityElement(ElementExtensible parent, Class<T> cls) {
        Collection<T> ee = CollectionsX.filter(parent.getExtensibilityElements(), cls);

        return ee.isEmpty() ? null : ee.iterator().next();

    }

    /**
     * Attempts to extract the SOAP Action is defined in the WSDL document.
     * 
     * @param def
     * @param service
     * @param port
     * @param operation
     * @return
     */
    public String getSoapAction(String operation) {
        BindingOperation bop = _binding.getBindingOperation(operation, null, null);
        if (bop == null)
            return "";

        for (SOAPOperation soapOp : CollectionsX.filter(bop.getExtensibilityElements(), SOAPOperation.class))
            return soapOp.getSoapActionURI();

        return "";
    }

    public QName parseSoapFault(Element odeMsgEl, SOAPEnvelope envelope, Operation operation) throws AxisFault {
        SOAPFault flt = envelope.getBody().getFault();
        SOAPFaultDetail detail = flt.getDetail();
        Fault fdef = inferFault(operation, flt);
        if (fdef == null)
            return null;
        
        Part pdef = (Part)fdef.getMessage().getParts().values().iterator().next();
        Element partel = odeMsgEl.getOwnerDocument().createElementNS(null,pdef.getName());
        odeMsgEl.appendChild(partel);
        
        partel.appendChild(odeMsgEl.getOwnerDocument().importNode(OMUtils.toDOM(detail),true));
        return new QName(_def.getTargetNamespace(), fdef.getName());
    }

    @SuppressWarnings("unchecked")
    private Fault inferFault(Operation operation, SOAPFault flt) {
        if (flt.getDetail() == null)
            return null;
        
        QName elName = flt.getDetail().getQName();
        for (Fault f : (Collection<Fault>)operation.getFaults().values()) {
            if (f.getMessage() == null)
                continue;  // should have checked in ctor
            
            Collection<Part> parts = f.getMessage().getParts().values();
            if (parts.isEmpty())
                continue;  // should check this in ctor
            Part p = parts.iterator().next();
            if (p.getElementName() == null)
                continue;  // should check this is ctor
            
            if (p.getElementName().equals(elName))
                return f;
                
        }
        
        return null;
    }



}
