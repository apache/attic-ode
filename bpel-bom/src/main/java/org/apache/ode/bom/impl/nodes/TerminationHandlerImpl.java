/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.ode.bom.impl.nodes;

import org.apache.ode.bom.api.Activity;
import org.apache.ode.bom.api.TerminationHandler;
import org.apache.ode.utils.NSContext;

/**
 * Normalized representation of a BPEL termination handler block (a
 * <code>terminationHandler</code> element). 
 */
public class TerminationHandlerImpl extends BpelObjectImpl implements TerminationHandler {

  private static final long serialVersionUID = -1L;
  private ActivityImpl _activity;
	private ScopeImpl _declaredIn;

  public TerminationHandlerImpl(NSContext nsContext) {
    super(nsContext);
  }

 
  public Activity getActivity() {
    return _activity;
  }

  public void setActivity(Activity activity) {
    _activity = (ActivityImpl) activity;
  }
  
  void setDeclaredIn(ScopeImpl scopeLikeConstruct) {
    _declaredIn = scopeLikeConstruct;
  }

}
