/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.client.ui.components;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * Document that can be set with certain field constraints.
 */
public class MirthFieldConstraints extends PlainDocument
{
	private int limit;

	// optional uppercase conversion
	private boolean toUppercase = false;

	private boolean numbersOnly = false;

	/**
	 * Constructor that sets a character number limit. Set limit to 0 for no
	 * limit.
	 */
	public MirthFieldConstraints(int limit)
	{
		super();
		this.limit = limit;
	}

	/**
	 * Constructor that sets a character number limit, uppercase conversion, and
	 * numbers only. Set limit to 0 for no limit.
	 */
	public MirthFieldConstraints(int limit, boolean toUppercase, boolean numbersOnly)
	{
		super();
		this.limit = limit;
		this.toUppercase = toUppercase;
		this.numbersOnly = numbersOnly;
	}

	/**
	 * Overwritten insertString method to check if the string should actually be
	 * inserted based on the constraints.
	 */
	public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException
	{
		if (str == null)
			return;
		if ((getLength() + str.length()) <= limit || limit == 0)
		{
			if (toUppercase)
				str = str.toUpperCase();
			if (numbersOnly)
			{
				try
				{
					if (Double.isNaN(Double.parseDouble(str)))
						return;
				}
				catch (Exception e)
				{
					return;
				}
			}
			super.insertString(offset, str, attr);
		}
	}
}
