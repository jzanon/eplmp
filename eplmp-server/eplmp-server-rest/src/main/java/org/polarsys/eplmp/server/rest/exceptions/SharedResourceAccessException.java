/*******************************************************************************
  * Copyright (c) 2017 DocDoku.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    DocDoku - initial API and implementation
  *******************************************************************************/
package org.polarsys.eplmp.server.rest.exceptions;

/**
 * @author Taylor LABEJOF
 */
public class SharedResourceAccessException extends RestApiException {

    public SharedResourceAccessException() {
        super();
    }

    @Override
    public String getMessage() {
        return "This resource can not be found. The link may have expired or is protected by password";
    }
}
