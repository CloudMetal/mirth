/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.client.ui;

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.TransferHandler;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.webreach.mirth.client.ui.components.MirthTree;
import com.webreach.mirth.client.ui.components.MirthTreeNode;
import com.webreach.mirth.client.ui.components.MirthTree.FilterTreeModel;
import com.webreach.mirth.client.ui.editors.MessageTreePanel;
import com.webreach.mirth.client.ui.editors.transformer.TransformerPane;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.MessageObject.Protocol;
import com.webreach.mirth.model.converters.IXMLSerializer;
import com.webreach.mirth.model.converters.SerializerFactory;
import com.webreach.mirth.model.dicom.DICOMVocabulary;
import com.webreach.mirth.model.util.MessageVocabulary;
import com.webreach.mirth.model.util.MessageVocabularyFactory;
import com.webreach.mirth.util.StringUtil;

public class TreePanel extends javax.swing.JPanel {

    private static final String EMPTY = "[empty]";
    private String version = "";
    private String type = "";
    private Logger logger = Logger.getLogger(this.getClass());
    private String _dropPrefix;
    private String _dropSuffix;
    private String messageName;
    private MessageVocabulary vocabulary;
    private Timer timer;
    private MessageVocabularyFactory vocabFactory;
    private JPopupMenu popupMenu;

    /**
     * Creates new form TreePanel
     */
    public TreePanel() {
        setup();
    }

    public TreePanel(String prefix, String suffix) {
        _dropPrefix = prefix;
        _dropSuffix = suffix;

        setup();
    }

    public void setup() {
        initComponents();

        filterTextBox.addKeyListener(new KeyAdapter() {

            public void keyPressed(KeyEvent arg0) {
            }

            public void keyReleased(KeyEvent e) {
                filterActionPerformed();
            }

            public void keyTyped(KeyEvent e) {
            }
        });

        exact.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                filterActionPerformed();
            }
        });
    }

    private void recursivelyExpandChildren(MirthTreeNode tn) {
        tree.expandPath(new TreePath(tn.getPath()));
        Enumeration<TreeNode> children = tn.children();
        while (children.hasMoreElements()) {
            MirthTreeNode child = (MirthTreeNode) children.nextElement();
            if (child.getChildCount() > 0) {
                recursivelyExpandChildren(child);
            }
            tree.expandPath(new TreePath(child.getPath()));
        }
    }

    private void recursivelyCollapseChildren(MirthTreeNode tn) {
        Enumeration<TreeNode> children = tn.children();
        while (children.hasMoreElements()) {
            MirthTreeNode child = (MirthTreeNode) children.nextElement();
            if (child.getChildCount() > 0) {
                recursivelyCollapseChildren(child);
            }
            tree.collapsePath(new TreePath(child.getPath()));
        }
    }

    public void setPrefix(String prefix) {
        _dropPrefix = prefix;
    }

    public void setSuffix(String suffix) {
        _dropSuffix = suffix;
    }

    public void setupPopupMenu() {
        popupMenu = new JPopupMenu();
        JMenuItem expandAll = new JMenuItem("Expand");
        expandAll.setIcon(new ImageIcon(this.getClass().getResource("images/add.png")));
        expandAll.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MirthTreeNode tn;
                if (tree.getSelectionPath() != null) {
                    TreePath tp = tree.getSelectionPath();
                    tn = (MirthTreeNode) tp.getLastPathComponent();
                } else {
                    tn = (MirthTreeNode) tree.getModel().getRoot();
                }
                if (!tn.isLeaf()) {
                    recursivelyExpandChildren(tn);
                    tree.expandPath(new TreePath(tn.getPath()));
                }

            }
        });
        popupMenu.add(expandAll);

        JMenuItem collapseAll = new JMenuItem("Collapse");
        collapseAll.setIcon(new ImageIcon(this.getClass().getResource("images/delete.png")));
        collapseAll.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                MirthTreeNode tn;
                if (tree.getSelectionPath() != null) {
                    TreePath tp = tree.getSelectionPath();
                    tn = (MirthTreeNode) tp.getLastPathComponent();
                } else {
                    tn = (MirthTreeNode) tree.getModel().getRoot();
                }
                if (!tn.isLeaf()) {
                    recursivelyCollapseChildren(tn);
                    tree.collapsePath(new TreePath(tn.getPath()));
                }
            }
        });
        popupMenu.add(collapseAll);

        popupMenu.addSeparator();

        if (_dropPrefix.equals(MessageTreePanel.MAPPER_PREFIX)) {
            JMenuItem mapNode = new JMenuItem("Map to Variable");
            mapNode.setIcon(new ImageIcon(this.getClass().getResource("images/book_previous.png")));
            mapNode.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    TreePath path = tree.getSelectionPath();
                    if (path == null) {
                        return;
                    }
                    TreeNode tp = (TreeNode) path.getLastPathComponent();
                    if (tp == null) {
                        return;
                    }

                    String variable = MirthTree.constructVariable(tp);
                    PlatformUI.MIRTH_FRAME.channelEditPanel.transformerPane.addNewStep(variable, variable, MirthTree.constructPath(tp, tree.getPrefix(), tree.getSuffix()).toString(), TransformerPane.MAPPER);
                }
            });
            popupMenu.add(mapNode);
        } else if (_dropPrefix.equals(MessageTreePanel.MESSAGE_BUILDER_PREFIX)) {
            JMenuItem mapNode = new JMenuItem("Map Segment");
            mapNode.setIcon(new ImageIcon(this.getClass().getResource("images/book_previous.png")));
            mapNode.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    TreePath path = tree.getSelectionPath();
                    if (path == null) {
                        return;
                    }
                    TreeNode tp = (TreeNode) path.getLastPathComponent();
                    if (tp == null) {
                        return;
                    }

                    PlatformUI.MIRTH_FRAME.channelEditPanel.transformerPane.addNewStep(MirthTree.constructMessageBuilderStepName(null, tp), MirthTree.constructPath(tp, tree.getPrefix(), tree.getSuffix()).toString(), "", TransformerPane.MESSAGE_BUILDER);
                }
            });
            popupMenu.add(mapNode);
        }

        JMenuItem ruleNode = new JMenuItem("Filter Segment");
        ruleNode.setIcon(new ImageIcon(this.getClass().getResource("images/book_previous.png")));
        ruleNode.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                TreePath path = tree.getSelectionPath();
                if (path == null) {
                    return;
                }
                TreeNode tp = (TreeNode) path.getLastPathComponent();
                if (tp == null) {
                    return;
                }

                PlatformUI.MIRTH_FRAME.channelEditPanel.filterPane.addNewRule(MirthTree.constructNodeDescription(tp), MirthTree.constructPath(tp, tree.getPrefix(), tree.getSuffix()).toString());
            }
        });
        popupMenu.add(ruleNode);
    }

    public void setFilterView() {
        popupMenu.getComponent(3).setVisible(false);
        popupMenu.getComponent(4).setVisible(true);
    }

    public void setTransformerView() {
        popupMenu.getComponent(3).setVisible(true);
        popupMenu.getComponent(4).setVisible(false);
    }

    public void setBorderText(String text) {
    }

    public void filterActionPerformed() {

        class FilterTimer extends TimerTask {

            @Override
            public void run() {
                filter();
            }
        }

        if (timer == null) {
            timer = new Timer();
            timer.schedule(new FilterTimer(), 1000);
        } else {
            timer.cancel();
            PlatformUI.MIRTH_FRAME.setWorking("", false);
            timer = new Timer();
            timer.schedule(new FilterTimer(), 1000);
        }
    }

    public void filter() {
        PlatformUI.MIRTH_FRAME.setWorking("Filtering...", true);
        FilterTreeModel model = (FilterTreeModel) tree.getModel();

        if (filterTextBox.getText().length() > 0) {
            model.setFiltered(true);
        } else {
            model.setFiltered(false);
        }

        model.performFilter(model.getRoot(), filterTextBox.getText(), exact.isSelected(), false);
        model.updateTreeStructure();
        if (filterTextBox.getText().length() > 0) {
            tree.expandAll();
        }

        PlatformUI.MIRTH_FRAME.setWorking("", false);
    }

    public void setMessage(Properties protocolProperties, String messageType, String source, String ignoreText, Properties dataProperties) {

        Document xmlDoc = null;
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder;

        messageName = "";
        version = "";
        type = "";
        String messageDescription = "";
        Protocol protocol = null;
        if (source.length() > 0 && !source.equals(ignoreText)) {
            IXMLSerializer<String> serializer;
            if (PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.HL7V2).equals(messageType)) {
                protocol = Protocol.HL7V2;
                //The \n to \r conversion is ONLY valid for HL7
                boolean convertLFtoCR = true;
                if (protocolProperties != null && protocolProperties.get("convertLFtoCR") != null) {
                    convertLFtoCR = Boolean.parseBoolean((String) protocolProperties.get("convertLFtoCR"));
                }
                if (convertLFtoCR) {
                    source = StringUtil.convertLFtoCR(source).trim();
                }
            } else if (PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.NCPDP).equals(messageType)) {
                protocol = Protocol.NCPDP;
            } else if (PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.DICOM).equals(messageType)) {
                protocol = Protocol.DICOM;
            } else if (PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.HL7V3).equals(messageType)) {
                protocol = Protocol.HL7V3;
            } else if (PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.X12).equals(messageType)) {
                protocol = Protocol.X12;
            } else if (PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.XML).equals(messageType)) {
                protocol = Protocol.XML;
            } else if (PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.EDI).equals(messageType)) {
                protocol = Protocol.EDI;
            } else if (PlatformUI.MIRTH_FRAME.protocols.get(MessageObject.Protocol.DELIMITED).equals(messageType)) {
                protocol = Protocol.DELIMITED;
            } else {
                logger.error("Invalid protocol");
                return;
            }

            try {
                serializer = SerializerFactory.getSerializer(protocol, protocolProperties);
                docBuilder = docFactory.newDocumentBuilder();

                String message;
                if (protocol.equals(Protocol.DICOM)) {
                    message = source;
                } else {
                    message = serializer.toXML(source);
                }
                xmlDoc = docBuilder.parse(new InputSource(new StringReader(message)));

                if (xmlDoc != null) {
                    Map<String, String> metadata = serializer.getMetadataFromDocument(xmlDoc);
                    version = metadata.get("version").trim();
                    type = metadata.get("type").trim();
                    messageName = type + " (" + version + ")";
                    vocabulary = vocabFactory.getInstance(PlatformUI.MIRTH_FRAME.mirthClient).getVocabulary(protocol, version, type);
                    messageDescription = vocabulary.getDescription(type.replaceAll("-", ""));

                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

            }

            if (xmlDoc != null) {
                createTree(protocol, xmlDoc, messageName, messageDescription);
                filter();
            } else {
                setInvalidMessage(messageType);
            }
        } else {
            clearMessage();
        }
    }

    /**
     * Shows the trigger-button popup menu.
     */
    private void showTreePopupMenu(java.awt.event.MouseEvent evt) {
        if (evt.isPopupTrigger()) {
            int row = tree.getRowForLocation(evt.getX(), evt.getY());
            tree.setSelectionRow(row);

            popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }

    /**
     * Updates the panel with a new Message.
     */
    private void createTree(Protocol protocol, Document xmlDoc, String messageName, String messageDescription) {
        Element el = xmlDoc.getDocumentElement();
        MirthTreeNode top;
        if (messageDescription.length() > 0) {
            top = new MirthTreeNode(messageName + " (" + messageDescription + ")");
        } else {
            top = new MirthTreeNode(messageName);
        }

        NodeList children = el.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            processElement(protocol, children.item(i), top);
        }
        // processElement(xmlDoc.getDocumentElement(), top);
        // addChildren(message, top);

        tree = new MirthTree(top, _dropPrefix, _dropSuffix);

        tree.setDragEnabled(true);
        tree.setTransferHandler(new TreeTransferHandler());
        tree.addMouseMotionListener(new MouseMotionAdapter() {

            public void mouseDragged(MouseEvent evt) {
                if (tree.getSelectionPath() != null) {
                    TreePath tp = tree.getSelectionPath();
                    TreeNode tn = (TreeNode) tp.getLastPathComponent();
                    if (tn.isLeaf()) {
                        refTableMouseDragged(evt);
                    }
                }
            }

            public void mouseMoved(MouseEvent evt) {
                refTableMouseMoved(evt);
            }
        });
        tree.addMouseListener(new MouseListener() {

            public void mouseClicked(MouseEvent e) {
                // TODO Auto-generated method stub
            }

            public void mouseEntered(MouseEvent e) {
                // TODO Auto-generated method stub
            }

            public void mouseExited(MouseEvent e) {
                refTableMouseExited(e);

            }

            public void mousePressed(MouseEvent e) {
                showTreePopupMenu(e);

            }

            public void mouseReleased(MouseEvent e) {
                showTreePopupMenu(e);
            }
        });
        try {
            tree.setScrollsOnExpand(true);
            treePane.setViewportView(tree);
            tree.revalidate();
        } catch (Exception e) {
            logger.error(e);
        }
        PlatformUI.MIRTH_FRAME.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    private void refTableMouseExited(MouseEvent evt) {
        if (!popupMenu.isShowing()) {
            tree.clearSelection();
        }
    }

    private void refTableMouseDragged(MouseEvent evt) {
    }

    private void refTableMouseMoved(MouseEvent evt) {
        int row = tree.getRowForLocation(evt.getPoint().x, evt.getPoint().y);

        if (!popupMenu.isShowing() && row >= 0 && row < tree.getRowCount()) {
            tree.setSelectionRow(row);
        }
    }

    private void processElement(Protocol protocol, Object elo, MirthTreeNode mtn) {
        if (elo instanceof Element) {
            Element el = (Element) elo;
            String description;
            if (vocabulary instanceof DICOMVocabulary) {
                description = vocabulary.getDescription(el.getAttribute("tag"));
                if (description.equals("?")) {
                    description = "";
                }
            } else {
                description = vocabulary.getDescription(el.getNodeName());
            }
            MirthTreeNode currentNode;
            if (description != null && description.length() > 0) {
                if (vocabulary instanceof DICOMVocabulary) {
                    //currentNode = new MirthTreeNode(vocabulary.getDescription(el.getAttribute("tag").replaceAll(" ", "")) + " (" + description + ")");
                    currentNode = new MirthTreeNode("tag" + el.getAttribute("tag") + " (" + description + ")");
                } else {
                    currentNode = new MirthTreeNode(el.getNodeName() + " (" + description + ")");
                }
            } else {
                currentNode = new MirthTreeNode(el.getNodeName());
            }

            String text = "";
            if (el.hasChildNodes()) {
                text = el.getFirstChild().getNodeValue();
                if ((text == null) || (text.equals("") || text.trim().length() == 0)) {
                    currentNode.add(new MirthTreeNode(el.getNodeName()));
                } else {
                    currentNode.add(new MirthTreeNode(text));
                }
            } else {
                // Check if we are in the format SEG.1.1
                if (protocol.equals(Protocol.HL7V3) || protocol.equals(Protocol.XML) || el.getNodeName().matches(".*\\..*\\..") || protocol.equals(Protocol.DICOM)) {
                    // We already at the last possible child segment, so just
                    // add empty node
                    currentNode.add(new MirthTreeNode(EMPTY));
                } else if (protocol.equals(Protocol.DELIMITED)) {
                    // We have empty column node
                    currentNode.add(new MirthTreeNode(EMPTY));
                } else {
                    // We have empty node and possibly empty children
                    // Add the sub-node handler (SEG.1)
                    currentNode.add(new MirthTreeNode(el.getNodeName()));
                    // Add a sub node (SEG.1.1)
                    String newNodeName = el.getNodeName() + ".1";
                    description = vocabulary.getDescription(newNodeName);
                    MirthTreeNode parentNode;
                    if (description != null && description.length() > 0) {
                        parentNode = new MirthTreeNode(newNodeName + " (" + description + ")");
                    } else {
                        parentNode = new MirthTreeNode(newNodeName);
                    }
                    parentNode.add(new MirthTreeNode(EMPTY));
                    currentNode.add(parentNode);
                }

            }

            processAttributes(el, currentNode);

            NodeList children = el.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                processElement(protocol, children.item(i), currentNode);
            }
            mtn.add(currentNode);
        }
    }

    private void processAttributes(Element el, MirthTreeNode dmtn) {
        NamedNodeMap atts = el.getAttributes();
        for (int i = 0; i < atts.getLength(); i++) {
            Attr att = (Attr) atts.item(i);
            MirthTreeNode attNode = new MirthTreeNode("@" + att.getName());
            attNode.add(new MirthTreeNode(att.getValue()));
            dmtn.add(attNode);
        }
    }

    public class TreeTransferHandler extends TransferHandler {

        protected Transferable createTransferable(JComponent c) {
            if (c != null) {
                try {
                    TreePath path = ((MirthTree) c).getSelectionPath();
                    if (path == null) {
                        return null;
                    }
                    TreeNode tp = (TreeNode) path.getLastPathComponent();
                    if (tp == null) {
                        return null;
                    }
                    if (!tp.isLeaf()) {
                        return null;
                    }

                    if (_dropPrefix.equals(MessageTreePanel.MAPPER_PREFIX)) {
                        return new TreeTransferable(tp, _dropPrefix, _dropSuffix, TreeTransferable.MAPPER_DATA_FLAVOR);
                    } else {
                        return new TreeTransferable(tp, _dropPrefix, _dropSuffix, TreeTransferable.MESSAGE_BUILDER_DATA_FLAVOR);
                    }
                } catch (ClassCastException cce) {
                    return null;
                }
            } else {
                return null;
            }
        }

        public int getSourceActions(JComponent c) {
            return COPY;
        }

        public boolean canImport(JComponent c, DataFlavor[] df) {
            return false;
        }
    }

    public void clearMessage() {
        MirthTreeNode top = new MirthTreeNode("Please provide a message template.");
        MirthTree tree = new MirthTree(top, _dropPrefix, _dropSuffix);
        treePane.setViewportView(tree);
        revalidate();
    }

    public void setInvalidMessage(String messageType) {
        MirthTreeNode top = new MirthTreeNode("The message template is not valid " + messageType + ".");
        MirthTree tree = new MirthTree(top, _dropPrefix, _dropSuffix);
        treePane.setViewportView(tree);
        revalidate();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        filterTextBox = new javax.swing.JTextField();
        treePane = new javax.swing.JScrollPane();
        tree = new com.webreach.mirth.client.ui.components.MirthTree();
        exact = new javax.swing.JCheckBox();

        setBackground(new java.awt.Color(255, 255, 255));
        setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(5, 1, 1, 1), "Message Tree", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 11))); // NOI18N

        jLabel1.setText("Filter:");

        treePane.setViewportView(tree);

        exact.setBackground(new java.awt.Color(255, 255, 255));
        exact.setText("Match Exact");
        exact.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        exact.setMargin(new java.awt.Insets(0, 0, 0, 0));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(treePane, javax.swing.GroupLayout.DEFAULT_SIZE, 163, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(filterTextBox, javax.swing.GroupLayout.DEFAULT_SIZE, 48, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(exact)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(filterTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(exact))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(treePane, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox exact;
    private javax.swing.JTextField filterTextBox;
    private javax.swing.JLabel jLabel1;
    private com.webreach.mirth.client.ui.components.MirthTree tree;
    private javax.swing.JScrollPane treePane;
    // End of variables declaration//GEN-END:variables
}
