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
package org.apache.ode.bpel.compiler;

import org.apache.ode.bom.api.*;
import org.apache.ode.bom.wsdl.WSDLFactory4BPEL;
import org.apache.ode.bom.wsdl.WSDLFactoryBPEL11;
import org.apache.ode.bpel.elang.xpath10.compiler.XPath10ExpressionCompilerBPEL11;

/**
 * BPEL v1.1 compiler.
 */
public class BpelCompiler11 extends BpelCompiler {

  /** URI for the XPath 1.0 expression language. */
  public static final String EXPLANG_XPATH = "http://www.w3.org/TR/1999/REC-xpath-19991116";

  public BpelCompiler11() {
    super((WSDLFactory4BPEL) WSDLFactoryBPEL11.newInstance());

    registerActivityCompiler(EmptyActivity.class, new EmptyGenerator());
    registerActivityCompiler(CompensateActivity.class, new CompensateGenerator());
    registerActivityCompiler(FlowActivity.class, new FlowGenerator());
    registerActivityCompiler(SequenceActivity.class, new SequenceGenerator());
    registerActivityCompiler(AssignActivity.class, new AssignGenerator());
    registerActivityCompiler(ThrowActivity.class, new ThrowGenerator());
    registerActivityCompiler(WhileActivity.class, new WhileGenerator());
    registerActivityCompiler(SwitchActivity.class, new SwitchGenerator());
    registerActivityCompiler(PickActivity.class, new PickGenerator());
    registerActivityCompiler(ReplyActivity.class, new ReplyGenerator());
    registerActivityCompiler(ReceiveActivity.class, new ReceiveGenerator());
    registerActivityCompiler(InvokeActivity.class, new InvokeGenerator());
    registerActivityCompiler(WaitActivity.class, new WaitGenerator());
    registerActivityCompiler(TerminateActivity.class, new TerminateGenerator());

    registerExpressionLanguage(EXPLANG_XPATH, new XPath10ExpressionCompilerBPEL11());
  }

  protected String getBpwsNamespace() {
    return Constants.NS_BPEL4WS_2003_03;
  }

  protected String getDefaultExpressionLanguage() {
    return EXPLANG_XPATH;
  }

}
