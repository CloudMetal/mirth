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

package com.webreach.mirth.client.ui.editors.transformer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

import org.jdesktop.swingx.JXComboBox;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.action.ActionFactory;
import org.jdesktop.swingx.action.BoundAction;
import org.jdesktop.swingx.decorator.AlternateRowHighlighter;
import org.jdesktop.swingx.decorator.HighlighterPipeline;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Script;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.CenterCellRenderer;
import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.Mirth;
import com.webreach.mirth.client.ui.MirthFileFilter;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.client.ui.components.MirthComboBoxCellEditor;
import com.webreach.mirth.client.ui.editors.BasePanel;
import com.webreach.mirth.client.ui.editors.EditorTableCellEditor;
import com.webreach.mirth.client.ui.editors.ScriptPanel;
import com.webreach.mirth.client.ui.editors.MapperPanel;
import com.webreach.mirth.client.ui.editors.MessageBuilder;
import com.webreach.mirth.client.ui.editors.MirthEditorPane;
import com.webreach.mirth.client.ui.util.FileUtil;
import com.webreach.mirth.client.ui.util.VariableListUtil;
import com.webreach.mirth.model.Channel;
import com.webreach.mirth.model.Connector;
import com.webreach.mirth.model.ExtensionPoint;
import com.webreach.mirth.model.ExtensionPointDefinition;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.PluginMetaData;
import com.webreach.mirth.model.Rule;
import com.webreach.mirth.model.Step;
import com.webreach.mirth.model.Transformer;
import com.webreach.mirth.model.converters.ObjectXMLSerializer;
import com.webreach.mirth.model.util.ImportConverter;
import com.webreach.mirth.plugins.ClientPanelPlugin;
import com.webreach.mirth.plugins.TransformerStepPlugin;

public class TransformerPane extends MirthEditorPane implements DropTargetListener
{
    public static final String MAPPER = "Mapper";
    public static final String MESSAGE_BUILDER = "Message Builder";
    public static final String JAVASCRIPT = "Javascript";
    // used to load the pane
    private Transformer transformer;
    // fields
    private JXTable transformerTable;
    private DefaultTableModel transformerTableModel;
    private JScrollPane transformerTablePane;
    private JSplitPane vSplitPane;
    private JSplitPane hSplitPane;
    private JXTaskPaneContainer transformerTaskPaneContainer;
    private JXTaskPane transformerTasks;
    private JPopupMenu transformerPopupMenu;
    private JXTaskPane viewTasks;
    private Channel channel;
    private Connector connector;
    public boolean updating; // flow control
    public boolean invalidVar; // selection control
    // panels using CardLayout
    protected BasePanel stepPanel; // the card holder
    protected BasePanel blankPanel;
    
    public static final int NUMBER_OF_COLUMNS = 4;
    
    private Map<String,TransformerStepPlugin> loadedPlugins = new HashMap<String, TransformerStepPlugin>();
    
    private DropTarget dropTarget;
    
    //Extension point for ExtensionPoint.Type.CLIENT_TRANSFORMER_STEP
    @ExtensionPointDefinition(mode=ExtensionPoint.Mode.CLIENT, type=ExtensionPoint.Type.CLIENT_TRANSFORMER_STEP)
    public void loadPlugins()
    {
        loadedPlugins = new HashMap<String,TransformerStepPlugin>();

        Map<String, PluginMetaData> plugins = parent.getPluginMetaData();
        for (PluginMetaData metaData : plugins.values())
        {
            
            if (metaData.isEnabled())
            {
                for (ExtensionPoint extensionPoint : metaData.getExtensionPoints())
                {
                    try
                    {
                        if(extensionPoint.getMode() == ExtensionPoint.Mode.CLIENT && extensionPoint.getType() == ExtensionPoint.Type.CLIENT_TRANSFORMER_STEP && extensionPoint.getClassName() != null && extensionPoint.getClassName().length() > 0)
                        {
                            String pluginName = extensionPoint.getName();
                            TransformerStepPlugin stepPlugin = (TransformerStepPlugin) Class.forName(extensionPoint.getClassName()).getDeclaredConstructors()[0].newInstance(new Object[]{pluginName, this});
                            loadedPlugins.put(stepPlugin.getDisplayName(), stepPlugin);
                        }
                    }
                    catch (Exception e)
                    {
                        parent.alertException(e.getStackTrace(), e.getMessage());
                    }
                }
            }
        }

    }
    /**
     * CONSTRUCTOR
     */
    public TransformerPane()
    {
        prevSelRow = -1;
        modified = false;
        new DropTarget(this,this);
        initComponents();
        setBorder(BorderFactory.createEmptyBorder());
    }
    
    /**
     * load( Transformer t ) now that the components have been initialized...
     */
    public boolean load(Connector c, Transformer t, boolean channelHasBeenChanged)
    {
        if (loadedPlugins.values().size() == 0)
        {
            parent.alertError("No transformer step plugins loaded.\r\nPlease install plugins and try again.");
            return false;
        }
        prevSelRow = -1;
        connector = c;
        transformer = t;
        channel = PlatformUI.MIRTH_FRAME.channelEditPanel.currentChannel;
        
        makeTransformerTable();
        
        // add any existing steps to the model
        List<Step> list = transformer.getSteps();
        ListIterator<Step> li = list.listIterator();
        while (li.hasNext())
        {
            Step s = li.next();
            if (!loadedPlugins.containsKey(s.getType()))
            {
                parent.alertError("Unable to load transformer step plugin \"" + s.getType() + "\"\r\nPlease install plugin and try again.");
                return false;
            }
            int row = s.getSequenceNumber();
            setRowData(s, row);
        }
        
        parent.setCurrentContentPage((JPanel) this);
        
        tabTemplatePanel.setDefaultComponent();
        tabTemplatePanel.tabPanel.add("Outgoing Data", tabTemplatePanel.outgoingTab);
        // select the first row if there is one
        int rowCount = transformerTableModel.getRowCount();
        if (rowCount > 0)
        {
            transformerTable.setRowSelectionInterval(0, 0);
            prevSelRow = 0;
        }
        else
        {
            stepPanel.showCard(BLANK_TYPE);
            for(TransformerStepPlugin plugin : loadedPlugins.values())
            {
                plugin.getPanel().setData(null);
            }
            loadData(-1);
        }
        
        if (connector.getMode() == Connector.Mode.SOURCE)
        {
            tabTemplatePanel.setIncomingDataType(PlatformUI.MIRTH_FRAME.channelEditPanel.getSourceDatatype());
        }
        else if (connector.getMode() == Connector.Mode.DESTINATION)
        {
            if (channel.getSourceConnector().getTransformer().getOutboundProtocol() != null)
                tabTemplatePanel.setIncomingDataType((String) PlatformUI.MIRTH_FRAME.protocols.get(channel.getSourceConnector().getTransformer().getOutboundProtocol()));
            else
                tabTemplatePanel.setIncomingDataType(PlatformUI.MIRTH_FRAME.channelEditPanel.getSourceDatatype());
        }
        
        if (transformer.getOutboundProtocol() != null)
        {
            tabTemplatePanel.setOutgoingDataType(((String) PlatformUI.MIRTH_FRAME.protocols.get(transformer.getOutboundProtocol())));
        }
        else
        {
            tabTemplatePanel.setOutgoingDataType(tabTemplatePanel.getIncomingDataType());
        }
        
        tabTemplatePanel.setIncomingDataProperties(transformer.getInboundProperties());
        tabTemplatePanel.setOutgoingDataProperties(transformer.getOutboundProperties());
        
        tabTemplatePanel.setIncomingMessage(transformer.getInboundTemplate());
        tabTemplatePanel.setOutgoingMessage(transformer.getOutboundTemplate());
        
        transformerTable.setBorder(BorderFactory.createEmptyBorder());
        transformerTaskPaneContainer.add(parent.getOtherPane());
        
        parent.setCurrentTaskPaneContainer(transformerTaskPaneContainer);
        
        updateStepNumbers();
        if (transformerTableModel.getRowCount() >0)
        {
            updateTaskPane((String)transformerTableModel.getValueAt(0, STEP_TYPE_COL));
        }
        
        if (channelHasBeenChanged)
            modified = true;
        else
            modified = false;
        
        return true;
    }
    
    
    public void dragEnter(DropTargetDragEvent dtde)
    {
        try
        {
            Transferable tr = dtde.getTransferable();
            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
            {
                
                dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
                
                List fileList = (List) tr.getTransferData(DataFlavor.javaFileListFlavor);
                Iterator iterator = fileList.iterator();
                if (iterator.hasNext() && fileList.size() == 1)
                {
                    String fileName = ((File)iterator.next()).getName();
                    if(!fileName.substring(fileName.lastIndexOf(".")).equalsIgnoreCase(".xml"))
                        dtde.rejectDrag();
                }
                else
                    dtde.rejectDrag();
            }
            else
                dtde.rejectDrag();
        }
        catch (Exception e)
        {
            dtde.rejectDrag();
        }
    }
    
    public void dragOver(DropTargetDragEvent dtde)
    {
    }
    
    public void dropActionChanged(DropTargetDragEvent dtde)
    {
    }
    
    public void dragExit(DropTargetEvent dte)
    {
    }
    
    public void drop(DropTargetDropEvent dtde)
    {
        try
        {
            Transferable tr = dtde.getTransferable();
            if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
            {
                
                dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
                
                List fileList = (List) tr.getTransferData(DataFlavor.javaFileListFlavor);
                Iterator iterator = fileList.iterator();
                
                if(fileList.size() == 1)
                {
                    File file = (File)iterator.next();
                    importTransformer(file);
                }
            }
        }
        catch (Exception e)
        {
            dtde.rejectDrop();
        }
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     */
    public void initComponents()
    {
        
        // the available panels (cards)
        stepPanel = new BasePanel();
        blankPanel = new BasePanel();
        loadPlugins();
        
        // establish the cards to use in the Transformer
        stepPanel.addCard(blankPanel, BLANK_TYPE);
        for(TransformerStepPlugin plugin : loadedPlugins.values())
        {
            stepPanel.addCard(plugin.getPanel(), plugin.getDisplayName());
        }
        transformerTablePane = new JScrollPane();
        transformerTablePane.setBorder(BorderFactory.createEmptyBorder());
        // make and place the task pane in the parent Frame
        transformerTaskPaneContainer = new JXTaskPaneContainer();
        
        viewTasks = new JXTaskPane();
        viewTasks.setTitle("Mirth Views");
        viewTasks.setFocusable(false);
        viewTasks.add(initActionCallback("accept", ActionFactory.createBoundAction("accept", "Back to Channels", "B"), new ImageIcon(Frame.class.getResource("images/resultset_previous.png"))));
        parent.setNonFocusable(viewTasks);
        transformerTaskPaneContainer.add(viewTasks);
        transformerTasks = new JXTaskPane();
        transformerTasks.setTitle("Transformer Tasks");
        transformerTasks.setFocusable(false);
        
        transformerPopupMenu = new JPopupMenu();
        
        // add new step task
        transformerTasks.add(initActionCallback("addNewStep", ActionFactory.createBoundAction("addNewStep", "Add New Step", "N"), new ImageIcon(Frame.class.getResource("images/add.png"))));
        JMenuItem addNewStep = new JMenuItem("Add New Step");
        addNewStep.setIcon(new ImageIcon(Frame.class.getResource("images/add.png")));
        addNewStep.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                addNewStep();
            }
        });
        transformerPopupMenu.add(addNewStep);
        
        // delete step task
        transformerTasks.add(initActionCallback("deleteStep", ActionFactory.createBoundAction("deleteStep", "Delete Step", "X"), new ImageIcon(Frame.class.getResource("images/delete.png"))));
        JMenuItem deleteStep = new JMenuItem("Delete Step");
        deleteStep.setIcon(new ImageIcon(Frame.class.getResource("images/delete.png")));
        deleteStep.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                deleteStep();
            }
        });
        transformerPopupMenu.add(deleteStep);
        
        transformerTasks.add(initActionCallback("doImport", ActionFactory.createBoundAction("doImport", "Import Transformer", "I"), new ImageIcon(Frame.class.getResource("images/import.png"))));
        JMenuItem importTransformer = new JMenuItem("Import Transformer");
        importTransformer.setIcon(new ImageIcon(Frame.class.getResource("images/import.png")));
        importTransformer.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doImport();
            }
        });
        transformerPopupMenu.add(importTransformer);
        
        transformerTasks.add(initActionCallback("doExport", ActionFactory.createBoundAction("doExport", "Export Transformer", "E"), new ImageIcon(Frame.class.getResource("images/export.png"))));
        JMenuItem exportTransformer = new JMenuItem("Export Transformer");
        exportTransformer.setIcon(new ImageIcon(Frame.class.getResource("images/export.png")));
        exportTransformer.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doExport();
            }
        });
        transformerPopupMenu.add(exportTransformer);
        
        transformerTasks.add(initActionCallback("doValidate", ActionFactory.createBoundAction("doValidate", "Validate Script", "V"), new ImageIcon(Frame.class.getResource("images/accept.png"))));
        JMenuItem validateStep = new JMenuItem("Validate Script");
        validateStep.setIcon(new ImageIcon(Frame.class.getResource("images/accept.png")));
        validateStep.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                doValidate();
            }
        });
        transformerPopupMenu.add(validateStep);
        
        // move step up task
        transformerTasks.add(initActionCallback("moveStepUp", ActionFactory.createBoundAction("moveStepUp", "Move Step Up", "P"), new ImageIcon(Frame.class.getResource("images/arrow_up.png"))));
        JMenuItem moveStepUp = new JMenuItem("Move Step Up");
        moveStepUp.setIcon(new ImageIcon(Frame.class.getResource("images/arrow_up.png")));
        moveStepUp.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                moveStepUp();
            }
        });
        transformerPopupMenu.add(moveStepUp);
        
        // move step down task
        transformerTasks.add(initActionCallback("moveStepDown", ActionFactory.createBoundAction("moveStepDown", "Move Step Down", "D"), new ImageIcon(Frame.class.getResource("images/arrow_down.png"))));
        JMenuItem moveStepDown = new JMenuItem("Move Step Down");
        moveStepDown.setIcon(new ImageIcon(Frame.class.getResource("images/arrow_down.png")));
        moveStepDown.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                moveStepDown();
            }
        });
        transformerPopupMenu.add(moveStepDown);
        
        // add the tasks to the taskpane, and the taskpane to the mirth client
        parent.setNonFocusable(transformerTasks);
        transformerTaskPaneContainer.add(transformerTasks);
        
        makeTransformerTable();
        
        // BGN LAYOUT
        transformerTable.setBorder(BorderFactory.createEmptyBorder());
        transformerTablePane.setBorder(BorderFactory.createEmptyBorder());
        transformerTablePane.setMinimumSize(new Dimension(0, 40));
        stepPanel.setBorder(BorderFactory.createEmptyBorder());
        
        hSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, transformerTablePane, stepPanel);
        hSplitPane.setContinuousLayout(true);
        //hSplitPane.setDividerSize(6);
        hSplitPane.setOneTouchExpandable(true);
        vSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, hSplitPane, refPanel);
        //vSplitPane.setDividerSize(6);
        vSplitPane.setOneTouchExpandable(true);
        vSplitPane.setContinuousLayout(true);
        resizePanes();
        
        hSplitPane.setBorder(BorderFactory.createEmptyBorder());
        vSplitPane.setBorder(BorderFactory.createEmptyBorder());
        this.setLayout(new BorderLayout());
        this.add(vSplitPane, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder());
        // END LAYOUT
        
    } // END initComponents()
    
    public void makeTransformerTable()
    {
        transformerTable = new JXTable();
        transformerTable.setBorder(BorderFactory.createEmptyBorder());
        transformerTable.setModel(new DefaultTableModel(new String[] { "#", "Name", "Type", "Data" }, 0)
        { // Data column is hidden
            public boolean isCellEditable(int rowIndex, int columnIndex)
            {
                boolean[] canEdit;
                TransformerStepPlugin plugin;
                try
                {
                    plugin = getPlugin((String) transformerTableModel.getValueAt(rowIndex, STEP_TYPE_COL));
                    canEdit = new boolean[] { false, plugin.isStepNameEditable(), true, true };
                }
                catch (Exception e)
                {
                    canEdit = new boolean[] { false, false, true, true };
                }
                return canEdit[columnIndex];
            }
        });
        
        transformerTableModel = (DefaultTableModel) transformerTable.getModel();
        
        transformerTable.getColumnModel().getColumn(STEP_NAME_COL).setCellEditor(new EditorTableCellEditor(this));
        
        // Set the combobox editor on the type column, and add action listener
        String[] defaultComboBoxValues = new String[loadedPlugins.size()];
        TransformerStepPlugin[] pluginArray = loadedPlugins.values().toArray(new TransformerStepPlugin[0]);
        for (int i = 0; i < pluginArray.length; i++)
        {
            defaultComboBoxValues[i] = pluginArray[i].getDisplayName();
        }
        MirthComboBoxCellEditor comboBox = new MirthComboBoxCellEditor(defaultComboBoxValues, this);
        
        ((JXComboBox) comboBox.getComponent()).addItemListener(new ItemListener()
        {
            public void itemStateChanged(ItemEvent evt)
            {
                if (evt.getStateChange() == evt.SELECTED)
                {
                    String type = evt.getItem().toString();
                    int row = getSelectedRow();
                    
                    if (type.equalsIgnoreCase((String) transformerTable.getValueAt(row, STEP_TYPE_COL)))
                        return;
                    
                    modified = true;
                    TransformerStepPlugin plugin;
                    try
                    {
                        plugin = getPlugin(type);
                        plugin.initData();
                        transformerTableModel.setValueAt(plugin.getNewStepName(), row, STEP_NAME_COL);
                        stepPanel.showCard(type);
                        updateTaskPane(type);
                    }
                    catch (Exception e)
                    {
                        // TODO Auto-generated catch block
                        parent.alertException(e.getStackTrace(), e.getMessage());
                    }
                    
                    
                }
            }
        });
        
        transformerTable.setSelectionMode(0); // only select one row at a time
        
        transformerTable.getColumnExt(STEP_NUMBER_COL).setMaxWidth(UIConstants.MAX_WIDTH);
        transformerTable.getColumnExt(STEP_TYPE_COL).setMaxWidth(UIConstants.MAX_WIDTH);
        transformerTable.getColumnExt(STEP_TYPE_COL).setMinWidth(120);
        
        transformerTable.getColumnExt(STEP_NUMBER_COL).setPreferredWidth(30);
        transformerTable.getColumnExt(STEP_TYPE_COL).setPreferredWidth(120);
        
        transformerTable.getColumnExt(STEP_NUMBER_COL).setCellRenderer(new CenterCellRenderer());
        transformerTable.getColumnExt(STEP_TYPE_COL).setCellEditor(comboBox);
        // transformerTable.getColumnExt(STEP_TYPE_COL).setCellRenderer(new
        // MyComboBoxRenderer(comboBoxValues));
        
        transformerTable.getColumnExt(STEP_DATA_COL).setVisible(false);
        transformerTable.getColumnExt(STEP_NUMBER_COL).setHeaderRenderer(PlatformUI.CENTER_COLUMN_HEADER_RENDERER);
        transformerTable.getColumnExt(STEP_TYPE_COL).setHeaderRenderer(PlatformUI.CENTER_COLUMN_HEADER_RENDERER);
        
        transformerTable.setRowHeight(UIConstants.ROW_HEIGHT);
        transformerTable.packTable(UIConstants.COL_MARGIN);
        transformerTable.setSortable(false);
        transformerTable.setOpaque(true);
        transformerTable.setRowSelectionAllowed(true);
        transformerTable.getTableHeader().setReorderingAllowed(false);
        
        if (Preferences.systemNodeForPackage(Mirth.class).getBoolean("highlightRows", true))
        {
            HighlighterPipeline highlighter = new HighlighterPipeline();
            highlighter.addHighlighter(new AlternateRowHighlighter(UIConstants.HIGHLIGHTER_COLOR, UIConstants.BACKGROUND_COLOR, UIConstants.TITLE_TEXT_COLOR));
            transformerTable.setHighlighters(highlighter);
        }
        
        transformerTable.setDropTarget(dropTarget);
        transformerTablePane.setDropTarget(dropTarget);
        
        transformerTable.setBorder(BorderFactory.createEmptyBorder());
        transformerTablePane.setBorder(BorderFactory.createEmptyBorder());
        transformerTablePane.setViewportView(transformerTable);
        
        // listen for mouse clicks on the actual table
        transformerTable.addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent evt)
            {
                showTransformerPopupMenu(evt, true);
            }
            
            public void mouseReleased(MouseEvent evt)
            {
                showTransformerPopupMenu(evt, true);
            }
        });
        
        // listen for mouse clicks on the empty part of the pane
        transformerTablePane.addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent evt)
            {
                showTransformerPopupMenu(evt, false);
            }
            
            public void mouseReleased(MouseEvent evt)
            {
                showTransformerPopupMenu(evt, false);
            }
        });
        
        transformerTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent evt)
            {
                if (!updating && !evt.getValueIsAdjusting())
                {
                    TransformerListSelected(evt);
                }
            }
        });
        transformerTable.addKeyListener(new KeyListener()
        {
            
            public void keyPressed(KeyEvent e)
            {
                // TODO Auto-generated method stub
                if (e.getKeyCode() == KeyEvent.VK_S && e.isControlDown())
                {
                    PlatformUI.MIRTH_FRAME.doContextSensitiveSave();
                }
            }
            
            public void keyReleased(KeyEvent e)
            {
                // TODO Auto-generated method stub
                
            }
            
            public void keyTyped(KeyEvent e)
            {
                // TODO Auto-generated method stub
                
            }
            
        });
    }
    
    private void showTransformerPopupMenu(MouseEvent evt, boolean onTable)
    {
        if (evt.isPopupTrigger())
        {
            if (onTable)
            {
                int row = transformerTable.rowAtPoint(new Point(evt.getX(), evt.getY()));
                transformerTable.setRowSelectionInterval(row, row);
            }
            
            transformerPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }
    }
    
    // for the task pane
    public BoundAction initActionCallback(String callbackMethod, BoundAction boundAction, ImageIcon icon)
    {
        
        if (icon != null)
            boundAction.putValue(Action.SMALL_ICON, icon);
        boundAction.registerCallback(this, callbackMethod);
        return boundAction;
    }
    
    // called when a table row is (re)selected
    private void TransformerListSelected(ListSelectionEvent evt)
    {
        updating = true;
        
        int row = transformerTable.getSelectedRow();
        int last = evt.getLastIndex();
        
        if (row != prevSelRow)
        {
            saveData(prevSelRow);
            
            if (invalidVar)
            {
                row = prevSelRow;
                invalidVar = false;
            }
            
            if (isValid(row))
                loadData(row);
            else if (isValid(last))
            {
                loadData(last);
                row = last;
            }
            
            String type = (String) transformerTable.getValueAt(row, STEP_TYPE_COL);
            stepPanel.showCard(type);
            transformerTable.setRowSelectionInterval(row, row);
            
            prevSelRow = row;
            updateTaskPane(type);
        }
        
        updating = false;
    }
    
    // returns true if the row is a valid index in the existing model
    private boolean isValid(int row)
    {
        return (row >= 0 && row < transformerTableModel.getRowCount());
    }
    
    // returns true if the variable name is unique
    // if an integer is provided, don't check against
    // the var in that row
    public boolean isUnique(String var, boolean dontCheckCurrentRow)
    {
        return isUnique(var, -1, dontCheckCurrentRow);
    }
    
    public boolean isUnique(String var, int curRow, boolean dontCheckCurrentRow)
    {
        boolean unique = true;
        
        for (int i = 0; i < transformerTableModel.getRowCount(); i++)
        {
            if (!dontCheckCurrentRow || dontCheckCurrentRow && i != getSelectedRow())
            {
                String temp = "";
                Map<Object, Object> data = (Map<Object, Object>) transformerTableModel.getValueAt(i, STEP_DATA_COL);
                
                if (data != null)
                    temp = (String) data.get("Variable");
                
                if (var != null && curRow != i)
                    if (var.equalsIgnoreCase(temp))
                        unique = false;
            }
        }
        
        return unique;
    }
    
    // sets the data from the previously used panel into the
    // previously selected Step object
    private void saveData(int row)
    {
        if (transformerTable.isEditing())
            transformerTable.getCellEditor(transformerTable.getEditingRow(), transformerTable.getEditingColumn()).stopCellEditing();
        
        if (isValid(row))
        {
            String type = (String) transformerTable.getValueAt(row, STEP_TYPE_COL);
            Map<Object, Object> data;
            try
            {
                data = getPlugin(type).getData(row);
                transformerTableModel.setValueAt(data, row, STEP_DATA_COL);
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                parent.alertException(e.getStackTrace(), e.getMessage());
            }
            
        }
    }
    
    /**
     * loadData() loads the data object into the correct panel
     */
    private void loadData(int row)
    {
        if (isValid(row))
        {
            String type = (String) transformerTableModel.getValueAt(row, STEP_TYPE_COL);
            Map<Object, Object> data = (Map<Object, Object>) transformerTableModel.getValueAt(row, STEP_DATA_COL);
            
            setPanelData(type, data);
        }
        
        if (connector.getMode() == Connector.Mode.SOURCE)
            tabTemplatePanel.updateVariables(connector.getFilter().getRules(), buildStepList(new ArrayList<Step>(), row));
        else
            tabTemplatePanel.updateVariables(getGlobalRuleVariables(), buildStepList(getGlobalStepVariables(), row));
    }
    
    private void setPanelData(String type, Map<Object, Object> data)
    {
        TransformerStepPlugin plugin;
        try
        {
            plugin = getPlugin(type);
            plugin.setData(data);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            parent.alertException(e.getStackTrace(), e.getMessage());
        }
    }
    private TransformerStepPlugin getPlugin(String name) throws Exception
    {
        TransformerStepPlugin plugin = loadedPlugins.get(name);
        if (plugin == null)
        {
            String message = "Unable to find Transformer Step Plugin: " + name;
            Exception e = new Exception(message);
            parent.alertError(message);
            throw new Exception(e);
        }
        else
        {
            return plugin;
        }
    }
    /**
     * prepData( int row ) works to move the data in a panel for moves or
     * deletes
     */
    private void prepData(int row)
    {
        Map<Object, Object> d = (Map<Object, Object>) transformerTableModel.getValueAt(row, STEP_DATA_COL);
        String type = (String) transformerTableModel.getValueAt(row, STEP_TYPE_COL);
        setPanelData(type, d);
    }
    
    private void setRowData(Step step, int row)
    {
        //TODO: Check the logic of this with plugins
        Object[] tableData = new Object[NUMBER_OF_COLUMNS];
        
        tableData[STEP_NUMBER_COL] = step.getSequenceNumber();
        TransformerStepPlugin plugin;
        try
        {
            plugin = getPlugin(step.getType());
            String stepName = step.getName();
            if (stepName == null || stepName.equals(""))
            {
                stepName = plugin.getStepName();
            }
            tableData[STEP_NAME_COL] = stepName;
            tableData[STEP_TYPE_COL] = step.getType();
            tableData[STEP_DATA_COL] = step.getData();
            
            updating = true;
            transformerTableModel.addRow(tableData);
            transformerTable.setRowSelectionInterval(row, row);
            updating = false;
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            parent.alertException(e.getStackTrace(), e.getMessage());
        }
        
    }
    
    /**
     * void addNewStep() add a new step to the end of the list
     */
    public void addNewStep()
    {
        saveData(transformerTable.getSelectedRow());
        
        if (!invalidVar || transformerTable.getRowCount() == 0)
        {
            int rowCount = transformerTable.getRowCount();
            modified = true;
            Step step = new Step();
            step.setSequenceNumber(rowCount);
            step.setScript("");
            step.setName("");
            
            for (TransformerStepPlugin plugin : loadedPlugins.values())
            {
                plugin.clearData();
            }
            if (tabTemplatePanel.tabPanel.getSelectedComponent() == tabTemplatePanel.outgoingTab)
            {
                if (loadedPlugins.containsKey(MESSAGE_BUILDER))
                {
                    step.setType(MESSAGE_BUILDER);// hl7 message type by default, outbound
                    
                    loadedPlugins.get(MESSAGE_BUILDER).initData();
                }
                else
                {
                    step.setType(loadedPlugins.keySet().iterator().next());
                }
            }
            else
            {
                if (loadedPlugins.containsKey(MAPPER))
                {
                    step.setType(MAPPER); // mapper type by default, inbound
                    loadedPlugins.get(MAPPER).initData();
                }
                else
                {
                    System.out.println("Mapper Plugin not found");
                    step.setType(loadedPlugins.keySet().iterator().next());
                }
            }
            Map<Object, Object> data = new HashMap<Object, Object>();
            data.put("Mapping", "");
            data.put("Variable", step.getName());
            
            step.setData(data);
            setRowData(step, rowCount);
            prevSelRow = rowCount;
            updateStepNumbers();
            transformerTable.setRowSelectionInterval(rowCount, rowCount);
            transformerTable.setVisibleRowCount(rowCount);
            transformerTablePane.getViewport().setViewPosition(new Point(0, transformerTable.getRowHeight() * rowCount));
        }
    }
    
    /**
     * void deleteStep(MouseEvent evt) delete all selected rows
     */
    public void deleteStep()
    {
        modified = true;
        if (transformerTable.isEditing())
            transformerTable.getCellEditor(transformerTable.getEditingRow(), transformerTable.getEditingColumn()).stopCellEditing();
        
        updating = true;
        
        int row = transformerTable.getSelectedRow();
        if (isValid(row + 1))
            prepData(row + 1);
        
        if (isValid(row))
            transformerTableModel.removeRow(row);
        
        updating = false;
        
        if (isValid(row))
            transformerTable.setRowSelectionInterval(row, row);
        else if (isValid(row - 1))
            transformerTable.setRowSelectionInterval(row - 1, row - 1);
        else
        {
            stepPanel.showCard(BLANK_TYPE);
            for (TransformerStepPlugin plugin : loadedPlugins.values())
            {
                plugin.clearData();
            }
        }
        updateStepNumbers();
    }
    
    /*
     * Import the transfomer
     */
    public void doImport()
    {
        JFileChooser importFileChooser = new JFileChooser();
        importFileChooser.setFileFilter(new MirthFileFilter("XML"));
        
        File currentDir = new File(Preferences.systemNodeForPackage(Mirth.class).get("currentDirectory", ""));
        if (currentDir.exists())
            importFileChooser.setCurrentDirectory(currentDir);
        
        int returnVal = importFileChooser.showOpenDialog(this);
        File importFile = null;
        
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            Preferences.systemNodeForPackage(Mirth.class).put("currentDirectory", importFileChooser.getCurrentDirectory().getPath());
            importFile = importFileChooser.getSelectedFile();
            importTransformer(importFile);
        }
    }
    
    private void importTransformer(File importFile)
    {
        String transformerXML = "";
        
        MessageObject.Protocol incomingProtocol = null, outgoingProtocol = null;
        
        for (MessageObject.Protocol protocol : MessageObject.Protocol.values())
        {
            if (PlatformUI.MIRTH_FRAME.protocols.get(protocol).equals(tabTemplatePanel.getIncomingDataType()))
            {
                incomingProtocol = protocol;
            }
            if (PlatformUI.MIRTH_FRAME.protocols.get(protocol).equals(tabTemplatePanel.getOutgoingDataType()))
            {
                outgoingProtocol = protocol;
            }
        }
        Transformer previousTransformer = connector.getTransformer();
        ObjectXMLSerializer serializer = new ObjectXMLSerializer();
        try
        {
            transformerXML = ImportConverter.convertTransformer(importFile, incomingProtocol, outgoingProtocol);
            Transformer importTransformer = (Transformer) serializer.fromXML(transformerXML);
            prevSelRow = -1;
            modified = true;
            connector.setTransformer(importTransformer);
            if (!load(connector, importTransformer, modified))
            {
                connector.setTransformer(previousTransformer);
                load(connector, previousTransformer, modified);
            }
        }
        catch (Exception e)
        {
            parent.alertError("Invalid transformer file.");
        }
    }
    
    /*
     * Export the transfomer
     */
    public void doExport()
    {
        accept(false);
        JFileChooser exportFileChooser = new JFileChooser();
        exportFileChooser.setFileFilter(new MirthFileFilter("XML"));
        
        File currentDir = new File(Preferences.systemNodeForPackage(Mirth.class).get("currentDirectory", ""));
        if (currentDir.exists())
            exportFileChooser.setCurrentDirectory(currentDir);
        
        int returnVal = exportFileChooser.showSaveDialog(this);
        File exportFile = null;
        
        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            Preferences.systemNodeForPackage(Mirth.class).put("currentDirectory", exportFileChooser.getCurrentDirectory().getPath());
            ObjectXMLSerializer serializer = new ObjectXMLSerializer();
            String transformerXML = serializer.toXML(transformer);
            exportFile = exportFileChooser.getSelectedFile();
            
            int length = exportFile.getName().length();
            
            if (length < 4 || !exportFile.getName().substring(length - 4, length).equals(".xml"))
                exportFile = new File(exportFile.getAbsolutePath() + ".xml");
            
            if (exportFile.exists())
                if (!parent.alertOption("This file already exists.  Would you like to overwrite it?"))
                    return;
            
            try
            {
                FileUtil.write(exportFile, transformerXML, false);
                parent.alertInformation("Transformer was written to " + exportFile.getPath() + ".");
            }
            catch (IOException ex)
            {
                parent.alertError("File could not be written.");
            }
        }
    }
    
    /*
     * Validate the current step if it has JavaScript
     */
    public void doValidate()
    {
        String type = (String) transformerTable.getValueAt(transformerTable.getSelectedRow(), STEP_TYPE_COL);
        try
        {
            getPlugin(type).doValidate();
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            parent.alertException(e.getStackTrace(), e.getMessage());
        }
        
    }
    
    /**
     * void moveStep( int i ) move the selected row i places
     */
    public void moveStepUp()
    {
        moveStep(-1);
    }
    
    public void moveStepDown()
    {
        moveStep(1);
    }
    
    public void moveStep(int i)
    {
        modified = true;
        int selRow = transformerTable.getSelectedRow();
        int moveTo = selRow + i;
        
        // we can't move past the first or last row
        if (isValid(moveTo))
        {
            saveData(selRow);
            loadData(moveTo);
            transformerTableModel.moveRow(selRow, selRow, moveTo);
            transformerTable.setRowSelectionInterval(moveTo, moveTo);
        }
        
        updateStepNumbers();
        parent.enableSave();
    }
    
    public List<Step> buildStepList(List<Step> list, int endingRow)
    {
        for (int i = 0; i < endingRow; i++)
        {
            Step step = new Step();
            step.setSequenceNumber(Integer.parseInt(transformerTable.getValueAt(i, STEP_NUMBER_COL).toString()));
            step.setName((String) transformerTableModel.getValueAt(i, STEP_NAME_COL));
            step.setType((String) transformerTableModel.getValueAt(i, STEP_TYPE_COL));
            step.setData((Map) transformerTableModel.getValueAt(i, STEP_DATA_COL));
            
            HashMap map = (HashMap) step.getData();
            try
            {
                step.setScript(getPlugin(step.getType()).getScript(map));
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                parent.alertException(e.getStackTrace(), e.getMessage());
            }
            list.add(step);
        }
        return list;
    }
    
    private List<Rule> getGlobalRuleVariables()
    {
        ArrayList<Rule> concatenatedRules = new ArrayList<Rule>();
        VariableListUtil.getRuleGlobalVariables(concatenatedRules, channel.getSourceConnector());
        
        List<Connector> destinationConnectors = channel.getDestinationConnectors();
        Iterator<Connector> it = destinationConnectors.iterator();
        boolean seenCurrent = false;
        while (it.hasNext())
        {
            Connector destination = it.next();
            if (connector == destination)
            {
                VariableListUtil.getRuleGlobalVariables(concatenatedRules, destination);
                seenCurrent = true;
            }
            else if (!seenCurrent)
            {
                VariableListUtil.getRuleGlobalVariables(concatenatedRules, destination);
            }
        }
        return concatenatedRules;
    }
    
    private List<Step> getGlobalStepVariables()
    {
        ArrayList<Step> concatenatedSteps = new ArrayList<Step>();
        VariableListUtil.getStepGlobalVariables(concatenatedSteps, channel.getSourceConnector());
        
        List<Connector> destinationConnectors = channel.getDestinationConnectors();
        Iterator<Connector> it = destinationConnectors.iterator();
        boolean seenCurrent = false;
        while (it.hasNext())
        {
            Connector destination = it.next();
            if (connector == destination)
            {
                seenCurrent = true;
            }
            else if (!seenCurrent)
            {
                VariableListUtil.getStepGlobalVariables(concatenatedSteps, destination);
            }
        }
        return concatenatedSteps;
    }
    
    
    
    /**
     * void accept(MouseEvent evt) returns a vector of vectors to the caller of
     * this.
     */
    public void accept()
    {
        accept(true);
    }
    
    public void accept(boolean returning)
    {
        saveData(transformerTable.getSelectedRow());
        
        if (!invalidVar)
        {
            List<Step> list = buildStepList(new ArrayList<Step>(), transformerTable.getRowCount());
            
            transformer.setSteps(list);
            
            for (MessageObject.Protocol protocol : MessageObject.Protocol.values())
            {
                if (PlatformUI.MIRTH_FRAME.protocols.get(protocol).equals(tabTemplatePanel.getIncomingDataType()))
                {
                    transformer.setInboundProtocol(protocol);
                }
                if (PlatformUI.MIRTH_FRAME.protocols.get(protocol).equals(tabTemplatePanel.getOutgoingDataType()))
                {
                    transformer.setOutboundProtocol(protocol);
                }
            }
            
            transformer.setInboundTemplate(tabTemplatePanel.getIncomingMessage());
            transformer.setOutboundTemplate(tabTemplatePanel.getOutgoingMessage());
            
            transformer.setInboundProperties(tabTemplatePanel.getIncomingDataProperties());
            transformer.setOutboundProperties(tabTemplatePanel.getOutgoingDataProperties());
            
            // reset the task pane and content to channel edit page
            if (returning)
            {
                parent.channelEditPanel.setDestinationVariableList();
                parent.taskPaneContainer.add(parent.getOtherPane());
                parent.setCurrentContentPage(parent.channelEditPanel);
                parent.setCurrentTaskPaneContainer(parent.taskPaneContainer);
                parent.setPanelName("Edit Channel - " + parent.channelEditPanel.currentChannel.getName());
                if (modified)
                    parent.enableSave();
                
                parent.channelEditPanel.updateComponentShown();
                modified = false;
            }
            
            invalidVar = false;
        }
    }
    public void setRowSelectionInterval(int index0, int index1)
    {
        //if (transformerTable.getRowCount() > index0 && transformerTable.getRowCount() >= index1){
        transformerTable.setRowSelectionInterval(index0, index1);
        //}
    }
    public Frame getParentFrame()
    {
        return parent;
    }
    /**
     * void updateStepNumbers() traverses the table and updates all data
     * numbers, both in the model and the view, after any change to the table
     */
    private void updateStepNumbers()
    {
        updating = true;
        
        int rowCount = transformerTableModel.getRowCount();
        int selRow = transformerTable.getSelectedRow();
        String type = new String();
        for (int i = 0; i < rowCount; i++)
            transformerTableModel.setValueAt(i, i, STEP_NUMBER_COL);
        
        if (isValid(selRow))
        {
            transformerTable.setRowSelectionInterval(selRow, selRow);
            loadData(selRow);
            type = transformerTableModel.getValueAt(selRow, STEP_TYPE_COL).toString();
            stepPanel.showCard(type);
        }
        else if (rowCount > 0)
        {
            transformerTable.setRowSelectionInterval(0, 0);
            loadData(0);
            type = transformerTableModel.getValueAt(0, STEP_TYPE_COL).toString();
            stepPanel.showCard(type);
            
        }
        
        updateTaskPane(type);
        updating = false;
    }
    
    /**
     * updateTaskPane() configure the task pane so that it shows only relevant
     * tasks
     */
    public void updateTaskPane(String newType)
    {
        int rowCount = transformerTableModel.getRowCount();
        if (rowCount <= 0)
            parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 1, -1, false);
        else if (rowCount == 1)
        {
            parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 0, -1, true);
            parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 4, -1, false);
        }
        else
        {
            parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 0, -1, true);
            
            int selRow = transformerTable.getSelectedRow();
            if (selRow == 0) // hide move up
                parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 5, 5, false);
            else if (selRow == rowCount - 1) // hide move down
                parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 6, 6, false);
        }
        parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 2, 3, true);
        
        try
        {
            if (newType != null && !newType.equals(""))
                parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 4, 4, getPlugin(newType).showValidateTask());
            else
                parent.setVisibleTasks(transformerTasks, transformerPopupMenu, 4, 4, false);
        }
        catch (Exception e)
        {
            parent.alertException(e.getStackTrace(), e.getMessage());
        }
    }
    
    public int getSelectedRow()
    {
        return transformerTable.getSelectedRow();
    }
    
    public DefaultTableModel getTableModel()
    {
        return transformerTableModel;
    }
    
    public void resizePanes()
    {
        hSplitPane.setDividerLocation((int) (PlatformUI.MIRTH_FRAME.currentContentPage.getHeight() / 2 - PlatformUI.MIRTH_FRAME.currentContentPage.getHeight() / 3.5));
        vSplitPane.setDividerLocation((int) (PlatformUI.MIRTH_FRAME.currentContentPage.getWidth() / 2 + PlatformUI.MIRTH_FRAME.currentContentPage.getWidth() / 5.5));
        tabTemplatePanel.resizePanes();
    }
    
    public void setHighlighters()
    {
        for (TransformerStepPlugin plugin : loadedPlugins.values())
        {
            plugin.setHighlighters();
        }
    }
    
    public void unsetHighlighters()
    {
        for (TransformerStepPlugin plugin : loadedPlugins.values())
        {
            plugin.unsetHighlighters();
        }
    }
    public boolean isInvalidVar()
    {
        return invalidVar;
    }
    public void setInvalidVar(boolean invalidVar)
    {
        this.invalidVar = invalidVar;
    }
}
