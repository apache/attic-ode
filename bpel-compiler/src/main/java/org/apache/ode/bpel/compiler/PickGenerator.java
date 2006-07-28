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
import org.apache.ode.bpel.capi.CompilationException;
import org.apache.ode.bpel.o.OActivity;
import org.apache.ode.bpel.o.OPickReceive;

import java.util.Iterator;


/**
 * Generates code for <code>&lt;pick&gt;</code> activities.
 */
class PickGenerator extends PickReceiveGenerator {

  public OActivity newInstance(Activity src) {
    return new OPickReceive(_context.getOProcess());
  }

  public void compile(OActivity output, Activity src) {
    OPickReceive opick = (OPickReceive) output;
    PickActivity pickDef = (PickActivity) src;

    opick.createInstanceFlag = pickDef.isCreateInstance();
    for (Iterator<OnMessage> i = pickDef.getOnMessages().iterator(); i.hasNext(); ) {
      OnMessage sOnMessage = i.next();
      OPickReceive.OnMessage oOnMessage = compileOnMessage(sOnMessage.getVariable(),
              sOnMessage.getPartnerLink(),
              sOnMessage.getOperation(),
              sOnMessage.getMessageExchangeId(),
              sOnMessage.getPortType(),
              pickDef.isCreateInstance(),
              sOnMessage.getCorrelations());
      oOnMessage.activity = _context.compile(sOnMessage.getActivity());
      opick.onMessages.add(oOnMessage);
    }

    try {
      for(Iterator<OnAlarm> i = pickDef.getOnAlarms().iterator(); i.hasNext(); ){
      	OnAlarm onAlarmDef = i.next();
        OPickReceive.OnAlarm oalarm = new OPickReceive.OnAlarm(_context.getOProcess());
        oalarm.activity = _context.compile(onAlarmDef.getActivity());
        if (onAlarmDef.getFor() != null && onAlarmDef.getUntil() == null) {
          oalarm.forExpr = _context.compileExpr(onAlarmDef.getFor());
        } else if (onAlarmDef.getFor() == null && onAlarmDef.getUntil() != null) {
          oalarm.untilExpr = _context.compileExpr(onAlarmDef.getUntil());
        } else {
          throw new CompilationException(__cmsgs.errForOrUntilMustBeGiven().setSource(onAlarmDef));
        }

        if (pickDef.isCreateInstance())
          throw new CompilationException(__cmsgs.errOnAlarmWithCreateInstance().setSource(onAlarmDef));

        opick.onAlarms.add(oalarm);
      }
    } catch (CompilationException ce) {
      _context.recoveredFromError(pickDef, ce);
    }
  }
}
