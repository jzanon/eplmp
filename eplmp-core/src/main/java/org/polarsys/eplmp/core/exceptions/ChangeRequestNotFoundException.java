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

package org.polarsys.eplmp.core.exceptions;

import java.text.MessageFormat;
import java.util.Locale;

/**
 *
 * @author Florent Garin
 */
public class ChangeRequestNotFoundException extends EntityNotFoundException {
    private final int mChange;

    public ChangeRequestNotFoundException(String pMessage) {
        super(pMessage);
        mChange = -1;
    }

    public ChangeRequestNotFoundException(Locale pLocale, int pChange) {
        this(pLocale, pChange, null);
    }

    public ChangeRequestNotFoundException(Locale pLocale, int pChange, Throwable pCause) {
        super(pLocale, pCause);
        mChange =pChange;
    }

    @Override
    public String getLocalizedMessage() {
        String message = getBundleDefaultMessage();
        return MessageFormat.format(message, mChange);
    }
}
