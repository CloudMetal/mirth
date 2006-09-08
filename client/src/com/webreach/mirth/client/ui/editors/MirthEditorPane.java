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


package com.webreach.mirth.client.ui.editors;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.PlatformUI;

public class MirthEditorPane extends JPanel {
	public boolean modified = false;
    public Frame parent = PlatformUI.MIRTH_FRAME;
    public JScrollPane referenceScrollPane;
	public JPanel refPanel;
	public TabbedReferencePanel tabPanel;
	
    public MirthEditorPane() {
    	super();
    	
		tabPanel = new TabbedReferencePanel();
		
		referenceScrollPane = new JScrollPane();
    	referenceScrollPane.setBorder( BorderFactory.createEmptyBorder() );
		referenceScrollPane.setViewportView( tabPanel );

    	refPanel = new JPanel();
    	refPanel.setBorder( BorderFactory.createEmptyBorder() );
		refPanel.setLayout( new BorderLayout() );
		refPanel.add( referenceScrollPane, BorderLayout.CENTER );
		
//		let the parent decide how big this should be
    	this.setPreferredSize( new Dimension( 0, 0 ) );
    }
    
    public void update() {
    	tabPanel.update();
	}
    
    public void setDroppedTextPrefix(String prefix){
    	tabPanel.setDroppedTextPrefix( prefix );
    }
}
