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
package org.apache.ode.daohib.hobj;

import java.util.Date;

/**
 * Base class for Hibernate objects providing auto-generated key and
 * create timestamp.
 */
public class HObject {
	
  private Long _id;
  private Date _created;
  
  /** Constructor.	 */
	public HObject() {
		super();
	}

  /**
   * Auto-gnerated creation timestamp.
   * @hibernate.property
   *  column="INSERT_TIME"
   *  type="timestamp"
   */
  public Date getCreated() {
    return _created;
  }

  public void setCreated(Date created) {
    _created = created;
  }

  /**
   * Auto-generated primary key.
   * @hibernate.id
   *  generator-class="native"
   *  column="ID"
   */
  public Long getId() {
    return _id;
  }

  public void setId(Long id) {
    _id = id;
  }
}
