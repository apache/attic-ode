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
package org.apache.ode.bpel.obj;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonCreator;

public class OConstantExpression extends OExpression  implements Serializable{
	public static final long serialVersionUID = -1L;

    private static String VAL = "_val";

    @JsonCreator 
    public OConstantExpression(){}
    public OConstantExpression(OProcess owner, Object val) {
        super(owner);
        setVal(val);
    }

    public Object getVal() {
        return fieldContainer.get(VAL);
    }

    public void setVal(Object val) {
        if (val == null)
          throw new IllegalArgumentException("OConstatExpression cannot be null.");

         fieldContainer.put(VAL, val);
    }

    public String toString() {
    	return "{OConstantExpression " + getVal()  + "}";
    }
}
