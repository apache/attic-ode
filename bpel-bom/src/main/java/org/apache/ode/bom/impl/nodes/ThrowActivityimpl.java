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
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.ThrowActivity;
import org.apache.ode.utils.NSContext;

import javax.xml.namespace.QName;


/**
 * ThrowActivityimpl
 *
 * @author jguinney
 */
public class ThrowActivityimpl extends ActivityImpl implements ThrowActivity {

  private static final long serialVersionUID = -1L;

  /**
   * Constructor.
   *
   * @param nsContext namespace context
   */
  public ThrowActivityimpl(NSContext nsContext) {
    super(nsContext);
  }

  private QName _faultName;
  private String _faultVariable;

  public ThrowActivityimpl() {
    super();
  }

  public void setFaultName(QName faultName) {
    _faultName = faultName;
  }

  public QName getFaultName() {
    return _faultName;
  }

  public void setFaultVariable(String faultVariable) {
    _faultVariable = faultVariable;
  }

  public String getFaultVariable() {
    return _faultVariable;
  }

  /**
   * @see ActivityImpl#getType()
   */
  public String getType() {
    return "throw";
  }
}
