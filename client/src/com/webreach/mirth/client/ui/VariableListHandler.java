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


package com.webreach.mirth.client.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.jdesktop.swingx.JXList;

import com.webreach.mirth.client.ui.components.MirthVariableList;
import com.webreach.mirth.client.ui.editors.ReferenceTable;

public class VariableListHandler extends TransferHandler {
	
	protected Transferable createTransferable( JComponent c ) {
		try {
			JXList list = ((JXList)( c ));
			if ( list == null ) return null;
			String text = (String)list.getSelectedValue();
			if (text != null){
				return new VariableTransferable( text, "${", "}" );
			}
			return null;
		}
		catch ( ClassCastException cce ) {
			return null;
		}
	}
	
	public int getSourceActions( JComponent c ) {
		return COPY;
	}
	
	public boolean canImport( JComponent c, DataFlavor[] df ) {
		return false;
	}
}
