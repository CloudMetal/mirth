/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.util;

public class EncryptionException extends Exception {
	private static final long serialVersionUID = 1L;

	public EncryptionException(Throwable cause) {
		super(cause);
	}
	
	public EncryptionException(String message) {
		super(message);
	}

	public EncryptionException(String message, Throwable cause) {
		super(message, cause);
	}
}
