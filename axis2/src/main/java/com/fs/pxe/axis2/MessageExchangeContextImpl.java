package com.fs.pxe.axis2;

import com.fs.pxe.bpel.iapi.BpelEngineException;
import com.fs.pxe.bpel.iapi.ContextException;
import com.fs.pxe.bpel.iapi.MessageExchangeContext;
import com.fs.pxe.bpel.iapi.MyRoleMessageExchange;
import com.fs.pxe.bpel.iapi.PartnerRoleMessageExchange;
import com.fs.pxe.bpel.iapi.EndpointReference;
import com.fs.pxe.bpel.epr.WSDL11Endpoint;
import com.fs.pxe.bpel.epr.EndpointFactory;
import com.fs.pxe.bpel.epr.WSAEndpoint;
import com.fs.utils.Namespaces;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;

/**
 * Implementation of the PXE {@link com.fs.pxe.bpel.iapi.MessageExchangeContext}
 * interface. This class is used by the PXE engine to make invocation of external
 * services using Axis.
 */
public class MessageExchangeContextImpl implements MessageExchangeContext {

  private static final Log __log = LogFactory.getLog(MessageExchangeContextImpl.class);

  private PXEServer _server;

  public MessageExchangeContextImpl(PXEServer server) {
    _server = server;
  }

  public void invokePartner(PartnerRoleMessageExchange partnerRoleMessageExchange) throws ContextException {
    if (__log.isDebugEnabled())
      __log.debug("Invoking a partner operation: " + partnerRoleMessageExchange.getOperationName());

    EndpointReference epr = partnerRoleMessageExchange.getEndpointReference();
    // We only invoke with WSA endpoints, that makes our life easier
    if (!(epr instanceof WSAEndpoint))
      epr = EndpointFactory.convert(new QName(Namespaces.WS_ADDRESSING_NS, "EndpointReference"),
              epr.toXML().getDocumentElement());
    // It's now safe to cast
    QName serviceName = ((WSAEndpoint)epr).getServiceName();
    String portName = ((WSAEndpoint)epr).getPortName();
    if (__log.isDebugEnabled())
      __log.debug("The service to invoke is the external service " + serviceName);
    ExternalService service = _server.getExternalService(serviceName, portName);
    service.invoke(partnerRoleMessageExchange);
  }

  public void onAsyncReply(MyRoleMessageExchange myRoleMessageExchange) throws BpelEngineException {
    if (__log.isDebugEnabled())
      __log.debug("Processing an async reply from service " + myRoleMessageExchange.getServiceName());

    // TODO Add a port in MessageExchange (for now there's only service) to be able to find the
    // TODO right service. For now we'll just lookup by service+portType but if we have severalt ports
    // TODO for the same portType that will not work.
    PXEService service = _server.getService(myRoleMessageExchange.getServiceName(),
            myRoleMessageExchange.getPortType().getQName());
    service.notifyResponse(myRoleMessageExchange);
  }
}
