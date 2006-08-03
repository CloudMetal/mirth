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
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.Ostermiller.Syntax.HighlightedDocument;
import com.webreach.mirth.client.ui.FunctionListHandler;
import com.webreach.mirth.client.ui.HL7XMLTreePanel;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.ReferenceTableHandler;
import com.webreach.mirth.client.ui.FunctionListBuilder;
import com.webreach.mirth.client.ui.FunctionListItem;
import com.webreach.mirth.client.ui.components.MirthTextPane;
import com.webreach.mirth.client.ui.util.SQLParserUtil;
import com.webreach.mirth.model.Channel;

public class TabbedReferencePanel extends JPanel {

	public TabbedReferencePanel() {
		initComponents();
		HL7TabbedPane.addTab("HL7 Tree", treeScrollPane);
		HL7TabbedPane.addTab("HL7 Message Template", pasteScrollPane);
	}

	public void update() {
		updateSQL();
	}

	private void updateSQL() {
		Object sqlStatement = PlatformUI.MIRTH_FRAME.channelEditPage
				.getSourceConnector().getProperties().get("query");
		if ((sqlStatement != null) && (!sqlStatement.equals(""))) {
			SQLParserUtil spu = new SQLParserUtil((String) sqlStatement);
			updateVariables(spu.Parse());
		} else {
			updateVariables(new String[] {});
		}
	}

	private void updateVariables(String[] variables) {
		dbVarPanel.remove(dbVarTable);
		dbVarTable = new VariableReferenceTable(variables);
		dbVarTable.setDragEnabled(true);
		dbVarTable.setTransferHandler(new ReferenceTableHandler());
		dbVarPanel.add(dbVarTable, BorderLayout.CENTER);
		varPanel = new JPanel();
		varPanel.setLayout(new BorderLayout());
		Channel channel = PlatformUI.MIRTH_FRAME.channelEditPage.currentChannel;
		if (channel.getDirection().equals(Channel.Direction.OUTBOUND)) {
			varPanel.add(globalVarPanel, BorderLayout.NORTH);
			varPanel.add(dbVarPanel, BorderLayout.CENTER);
		} else {
			varPanel.add(globalVarPanel, BorderLayout.CENTER);
		}
		varScrollPane.setViewportView(varPanel);
		repaint();
	}

	public void setDroppedTextPrefix(String prefix) {
		treePanel.setDroppedTextPrefix(prefix);
	}

	public String getHL7Message() {
		return pasteBox.getText();
	}

	public void setHL7Message(String msg) {
		pasteBox.setText(msg);
	}

	private void initComponents() {
		HL7TabbedPane = new JTabbedPane();
		pasteTab = new JPanel();
		pasteScrollPane = new JScrollPane();
		treeScrollPane = new JScrollPane();
		treePanel = new HL7XMLTreePanel();
		/*
		String[] referenceData = new String[7];
		String[] tooltip = new String[7];
		referenceData[0] = "localMap";
		referenceData[1] = "globalMap";
		referenceData[2] = "incomingMessage";
		referenceData[3] = "debug(\"message\");";
		referenceData[4] = "sendEmail(\"to\", \"cc\", \"from\", \"subject\", \"body\");";
		referenceData[5] = "queryDatabase(\"driver\", \"address\", \"query\");";
		referenceData[6] = "updateDatabase(\"driver\", \"address\", \"query\");";
		tooltip[0] = "The local variable map that will be sent to the connector.";
		tooltip[1] = "The global variable map that persists values between channels.";
		tooltip[2] = "The original incoming ER7 or XML string.";
		tooltip[3] = "Outputs the message to the system debug log.";
		tooltip[4] = "Sends an alert email using the alert SMTP properties.";
		tooltip[5] = "Performs a database query and returns the resultset.";
		tooltip[6] = "Performs a database update.";
		*/
		ArrayList<FunctionListItem> functionListItems = new FunctionListBuilder().getVariableListItems();
		globalVarTable = new VariableReferenceTable(functionListItems);
		globalVarPanel = new JPanel();
		globalVarPanel.setBorder(BorderFactory.createTitledBorder("Variables & Functions"));
		globalVarPanel.setBackground(EditorConstants.PANEL_BACKGROUND);
		globalVarPanel.setLayout(new BorderLayout());
		globalVarPanel.add(globalVarTable, BorderLayout.CENTER);
		globalVarTable.setDragEnabled(true);
		globalVarTable.setTransferHandler(new FunctionListHandler(functionListItems));
		
		dbVarTable = new VariableReferenceTable();
		dbVarPanel = new JPanel();
		dbVarPanel.setBorder(BorderFactory
				.createTitledBorder("Database Variables"));
		dbVarPanel.setBackground(EditorConstants.PANEL_BACKGROUND);
		dbVarPanel.setLayout(new BorderLayout());
		dbVarPanel.add(dbVarTable, BorderLayout.CENTER);
		dbVarTable.setDragEnabled(true);
		dbVarTable.setTransferHandler(new ReferenceTableHandler());

		varPanel = new JPanel();
		varPanel.setLayout(new BorderLayout());
		varPanel.add(globalVarPanel, BorderLayout.NORTH);
		varPanel.add(dbVarPanel, BorderLayout.CENTER);

		varScrollPane = new JScrollPane();
		varScrollPane.setViewportView(varPanel);
		varScrollPane.addComponentListener(new ComponentListener() {
			public void componentResized(ComponentEvent arg0) {
			}

			public void componentMoved(ComponentEvent arg0) {
			}

			public void componentShown(ComponentEvent arg0) {
				// chrisl 7/11/206
				updateSQL();
			}

			public void componentHidden(ComponentEvent arg0) {

			}

		});

		// we need to create an HL7 Lexer...
		HL7Doc = new HighlightedDocument();
		HL7Doc.setHighlightStyle(HighlightedDocument.C_STYLE);
		pasteBox = new MirthTextPane();
		pasteBox.setDocument(HL7Doc);
		pasteBox.setFont(EditorConstants.DEFAULT_FONT);

		// this is a tricky way to have "no line-wrap" in a JTextPane;
		// not using JTextArea for compliance with our current syntax
		// highlighting package, and for use of MirthTextPane, which
		// provides right-click edit functionality
		JPanel pasteBoxPanel = new JPanel();
		pasteBoxPanel.setLayout(new BorderLayout());
		pasteBoxPanel.add(pasteBox, BorderLayout.CENTER);
		pasteScrollPane.setViewportView(pasteBoxPanel);
		treeScrollPane.setViewportView(treePanel);

		

		treeScrollPane.addComponentListener(new ComponentListener() {

			public void componentResized(ComponentEvent arg0) {
			}

			public void componentMoved(ComponentEvent arg0) {
			}

			public void componentShown(ComponentEvent arg0) {
				String message = pasteBox.getText();
				if (message != null || !message.equals(""))
					treePanel.setMessage(message.replaceAll("//n", "/r/n"));
				else
					treePanel.clearMessage();
				treePanel.revalidate();
				treePanel.repaint();
			}

			public void componentHidden(ComponentEvent arg0) {
				treePanel.clearMessage();
			}

		});

		org.jdesktop.layout.GroupLayout pasteTabLayout = new org.jdesktop.layout.GroupLayout(
				pasteTab);
		pasteTab.setLayout(pasteTabLayout);
		pasteTabLayout.setHorizontalGroup(pasteTabLayout.createParallelGroup(
				org.jdesktop.layout.GroupLayout.LEADING).add(
				pasteTabLayout.createSequentialGroup().add(pasteScrollPane,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 207,
						Short.MAX_VALUE)));
		pasteTabLayout.setVerticalGroup(pasteTabLayout.createParallelGroup(
				org.jdesktop.layout.GroupLayout.LEADING).add(
				pasteTabLayout.createSequentialGroup().add(pasteScrollPane,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 350,
						Short.MAX_VALUE)));
		HL7TabbedPane.addTab("Variables", varScrollPane);

		org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(
				this);
		this.setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(
				org.jdesktop.layout.GroupLayout.LEADING).add(
				org.jdesktop.layout.GroupLayout.TRAILING,
				layout.createSequentialGroup().add(HL7TabbedPane,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 232,
						Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createParallelGroup(
				org.jdesktop.layout.GroupLayout.LEADING).add(
				org.jdesktop.layout.GroupLayout.TRAILING,
				layout.createSequentialGroup().add(HL7TabbedPane,
						org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 400,
						Short.MAX_VALUE)));
	}

	public void BuildVarPanel() {
		
	}

	private JTabbedPane HL7TabbedPane;

	private JPanel pasteTab;

	private JScrollPane pasteScrollPane;

	private MirthTextPane pasteBox;

	private static HighlightedDocument HL7Doc;

	private JScrollPane treeScrollPane;

	private HL7XMLTreePanel treePanel;

	private VariableReferenceTable globalVarTable;

	private VariableReferenceTable dbVarTable;

	private JPanel globalVarPanel;

	private JPanel dbVarPanel;

	private JScrollPane varScrollPane;

	private JPanel varPanel;
}
