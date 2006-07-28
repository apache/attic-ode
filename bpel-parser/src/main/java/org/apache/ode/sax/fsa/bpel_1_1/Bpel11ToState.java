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
package org.apache.ode.sax.fsa.bpel_1_1;

import org.apache.ode.bom.api.To;
import org.apache.ode.bom.impl.nodes.ExpressionImpl;
import org.apache.ode.bom.impl.nodes.PropertyValImpl;
import org.apache.ode.bom.impl.nodes.VariableValImpl;
import org.apache.ode.bom.impl.nodes.PartnerLinkValImpl;
import org.apache.ode.sax.fsa.*;
import org.apache.ode.sax.evt.StartElement;
import org.apache.ode.sax.evt.XmlAttributes;
import org.apache.ode.sax.evt.attspec.FilterSpec;
import org.apache.ode.sax.evt.attspec.XmlAttributeSpec;

class Bpel11ToState extends BaseBpelState {

  private static final StateFactory _factory = new Factory();
  private To _t;
  
  private XmlAttributeSpec VAR = new FilterSpec(
      new String[] {"variable"},new String[] {"part","query"});
  private XmlAttributeSpec PLINK = new FilterSpec(
      new String[] {"partnerLink"},new String[] {});
  private XmlAttributeSpec VAR_PROP = new FilterSpec(
      new String[] {"variable","property"},new String[] {});

  
  Bpel11ToState(StartElement se, ParseContext pc) throws ParseException {
    super(pc);
    XmlAttributes atts = se.getAttributes();
    if (VAR.matches(atts)) {
      VariableValImpl vvi = new VariableValImpl();
      vvi.setNamespaceContext(se.getNamespaceContext());
      vvi.setLineNo(se.getLocation().getLineNumber());
      vvi.setVariable(atts.getValue("variable"));
      if(atts.hasAtt("part")){
        vvi.setPart(atts.getValue("part"));
      }
      if(atts.hasAtt("query")){
        ExpressionImpl expr = new ExpressionImpl();
        expr.setXPathString(atts.getValue("query"));
        expr.setLineNo(se.getLocation().getLineNumber());
        expr.setNamespaceContext(se.getNamespaceContext());
        vvi.setLocation(expr);
      }
      _t = vvi;
    } else if (VAR_PROP.matches(atts)) {
      PropertyValImpl pvi = new PropertyValImpl(se.getNamespaceContext());
      pvi.setLineNo(se.getLocation().getLineNumber());
      pvi.setVariable(atts.getValue("variable"));
      pvi.setProperty(se.getNamespaceContext().derefQName(atts.getValue("property")));
      _t = pvi;
    } else if (PLINK.matches(atts)) {
      PartnerLinkValImpl pvi = new PartnerLinkValImpl();
      pvi.setLineNo(se.getLocation().getLineNumber());
      pvi.setPartnerLink(atts.getValue("partnerLink"));
      _t = pvi;
    } else {
      pc.parseError(ParseError.ERROR,se,"PARSER_ERROR",
          "Unsupported attribute combination " + atts.toString());
      // TODO: Get real error key here.
    }
  }
  
  
  public To getTo() {
    return _t;
  }
  
  /**
   * @see org.apache.ode.sax.fsa.State#getFactory()
   */
  public StateFactory getFactory() {
    return _factory;
  }

  /**
   * @see org.apache.ode.sax.fsa.State#getType()
   */
  public int getType() {
    return BPEL11_TO;
  }
  
  static class Factory implements StateFactory {
    
    public State newInstance(StartElement se, ParseContext pc) throws ParseException {
      return new Bpel11ToState(se,pc);
    }
  }
}
