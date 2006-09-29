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
package org.apache.ode.test;

import org.apache.ode.bpel.deploy.DeploymentManager;
import org.apache.ode.bpel.deploy.DeploymentUnitImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;

/**
 * A non-persistent implementation of the
 * {@link org.apache.ode.bpel.deploy.DeploymentManager} interface.
 */

public class DeploymentManagerImpl implements DeploymentManager {

    private ArrayList<DeploymentUnitImpl> _knownDeployments = new ArrayList<DeploymentUnitImpl>();


    public DeploymentUnitImpl createDeploymentUnit(String location) {
        return createDeploymentUnit(new File(location));
    }

    public DeploymentUnitImpl createDeploymentUnit(File deploymentUnitDirectory) {
        DeploymentUnitImpl du = new DeploymentUnitImpl(deploymentUnitDirectory);
        _knownDeployments.add(du);
        return du;
    }

    public Collection<DeploymentUnitImpl> getDeploymentUnits() {
        return new ArrayList<DeploymentUnitImpl>(_knownDeployments);
    }

    public void remove(DeploymentUnitImpl du) {
        _knownDeployments.remove(du);

    }

    public Set<String> getDeploymentsList() {
        HashSet<String> depls = new HashSet<String>();
        for (DeploymentUnitImpl unit : _knownDeployments) {
            depls.add(unit.getDeployDir().getName());
        }
        return depls;
    }

}
