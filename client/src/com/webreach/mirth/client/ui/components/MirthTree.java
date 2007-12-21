/*
 * MirthTree.java
 *
 * Created on July 5, 2007, 2:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.client.ui.components;

import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.jdesktop.swingx.JXTree;

import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.MapperDropData;
import com.webreach.mirth.client.ui.MessageBuilderDropData;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.TreeTransferable;
import com.webreach.mirth.client.ui.editors.MessageTreePanel;

/**
 *
 * @author brendanh
 */
public class MirthTree extends JXTree implements DropTargetListener
{
    private Frame parent;
    private MyFilter mf;
    private FilterTreeModel ftm;
    private String prefix;
    private String suffix;
    private DropTarget dropTarget;
    private DataFlavor supportedDropFlavor;
    
    /** Creates a new instance of MirthTree */
    public MirthTree()
    {
        mf = new MyFilter();
        ftm = new FilterTreeModel(null, mf);
        this.setModel(ftm);
        dropTarget = new DropTarget(this, this);
    }  
    
    public MirthTree(MirthTreeNode root, String prefix, String suffix)
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        mf = new MyFilter();
        ftm = new FilterTreeModel(root, mf);
        this.setModel(ftm);
        dropTarget = new DropTarget(this, this);
        this.prefix = prefix;
        this.suffix = suffix;
        
        if(prefix.equals(MessageTreePanel.MAPPER_PREFIX))
            this.supportedDropFlavor = TreeTransferable.MESSAGE_BUILDER_DATA_FLAVOR;
        else if(prefix.equals(MessageTreePanel.MESSAGE_BUILDER_PREFIX))
            this.supportedDropFlavor = TreeTransferable.MAPPER_DATA_FLAVOR;
    }
    
    public class FilterTreeModel extends DefaultTreeModel
    {
        Filter filter;
        
        public TreeNode getRoot()
        {
            return root;
        }
        
        public FilterTreeModel(TreeNode root, Filter filter)
        {
            super(root);
            this.filter = filter;
        }
        
        public void setFiltered(boolean pass)
        {
            this.filter.setFiltered(!pass);
        }
        public void updateTreeStructure(){
        	TreeNode tn = this.getRoot();
        	int count = tn.getChildCount();
            Object[] path = { tn };
            int[] childIndices = new int[count];
            Object[] children = new Object[count];
        	this.fireTreeStructureChanged(tn, path, childIndices, children);
        }
        public boolean performFilter(TreeNode tn, String text, boolean exact, boolean ignoreChildren)
        {
            if(tn == null)
                return false;
            
            int realCount = super.getChildCount(tn);
            
            boolean passed = filter.pass(tn, text, exact) || ignoreChildren;
            boolean originalPassed = passed;
            
            for (int i = 0; i < realCount; i++)
            {
                boolean childPassed = performFilter(tn.getChildAt(i), text, exact, originalPassed);
                passed = passed || childPassed;
            }
            
            ((MirthTreeNode)tn).setFiltered(!passed);
            
            int count = tn.getChildCount();
            Object[] path = { tn };
            int[] childIndices = new int[count];
            Object[] children = new Object[count];
            
            return passed;
        }
        
        public int getChildCount(Object parent)
        {
            int realCount = super.getChildCount(parent);
            int visibleCount = 0;
            for (int i = 0; i < realCount; i++)
            {
                MirthTreeNode mtn = (MirthTreeNode) super.getChild(parent, i);
                if (!mtn.isFiltered())
                {
                    visibleCount++;
                }
            }
            
            return visibleCount;
        }
        
        public Object getChild(Object parent, int index)
        {
            int cnt = -1;
            for (int i = 0; i < super.getChildCount(parent); i++)
            {
                MirthTreeNode child = (MirthTreeNode) super.getChild(parent, i);
                if (!child.isFiltered())
                {
                    cnt++;
                }
                
                if (cnt == index)
                    return child;
            }
            return null;
        }
    }
    
    interface Filter
    {
        public boolean pass(Object obj, String text, boolean exact);
        
        public void setFiltered(boolean pass);
        
        public boolean isFiltered();
    }
    
    class MyFilter implements Filter
    {
        boolean pass = true;
        
        public boolean pass(Object obj, String text, boolean exact)
        {
            if (pass)
                return true;
            
            String s = obj.toString();
            if(exact)
                return s.equals(text);
            else
                return s.toLowerCase().indexOf(text.toLowerCase()) != -1;                
        }
        
        public void setFiltered(boolean pass)
        {
            this.pass = pass;
        }
        
        public boolean isFiltered()
        {
            return pass;
        }
    }
    
    public void dragEnter(DropTargetDragEvent dtde)
    {
        try
        {
            Transferable tr = dtde.getTransferable();
            
            if (parent.currentContentPage == parent.channelEditPanel.transformerPane && tr.isDataFlavorSupported(supportedDropFlavor))
            {
                dtde.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
            }
            else
                dtde.rejectDrag();
        }
        catch (Exception e)
        {
            dtde.rejectDrag();
        }
    }

    public void dragExit(DropTargetEvent dte)
    {
        // TODO Auto-generated method stub
        
    }

    public void dragOver(DropTargetDragEvent dtde)
    {
        if(!dtde.isDataFlavorSupported(supportedDropFlavor))
            return;
        
        Point cursorLocationBis = dtde.getLocation();
        TreePath destinationPath = getPathForLocation(cursorLocationBis.x, cursorLocationBis.y);
        if(destinationPath != null)
        {
            if(((MirthTreeNode)destinationPath.getLastPathComponent()).isLeaf())
            {
                this.setSelectionPath(destinationPath);
            }
        }
    }

    public void drop(DropTargetDropEvent dtde)
    {
        if(parent.currentContentPage != parent.channelEditPanel.transformerPane)
            return;

        try
        {
            TreeNode selectedNode = (TreeNode) this.getLastSelectedPathComponent();
            
            if(selectedNode == null)
                return;
            
            dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
            Transferable tr = dtde.getTransferable();
            
            if (supportedDropFlavor == TreeTransferable.MAPPER_DATA_FLAVOR)
            {
                Object transferData = tr.getTransferData(TreeTransferable.MAPPER_DATA_FLAVOR);
                MapperDropData data = (MapperDropData) transferData;
                
                parent.channelEditPanel.transformerPane.addMessageBuilder(constructPath(selectedNode, prefix, suffix).toString(), data.getMapping());
            }
            else if (supportedDropFlavor == TreeTransferable.MESSAGE_BUILDER_DATA_FLAVOR)
            {
                Object transferData = tr.getTransferData(TreeTransferable.MESSAGE_BUILDER_DATA_FLAVOR);
                MessageBuilderDropData data = (MessageBuilderDropData) transferData;
                parent.channelEditPanel.transformerPane.addMessageBuilder(data.getMessageSegment(), constructPath(selectedNode, prefix, suffix).toString());
            }
            else
            {
                dtde.rejectDrop();
            }
        }
        catch (Exception e)
        {
            dtde.rejectDrop();
        }
        
    }

    public void dropActionChanged(DropTargetDragEvent dtde)
    {
        // TODO Auto-generated method stub
        
    }
    
    /**
     * Get the index of a node in relation to other nodes with the same name.
     * Returns -1 if there isn't an index.
     * 
     * @param node
     * @return
     */
    public static int getIndexOfNode(TreeNode node)
    {
    	String nodeName = node.toString();
        TreeNode parent = node.getParent();
        
        // The parent will be null for the root node
        if (parent != null)
        {
        	Enumeration children = parent.children();
            int indexCounter = 0;
            int foundIndex = -1;
            
            // Look through all of the children of the parent to see if there
            // are multiple children with the same name.
            while (children.hasMoreElements())
            {
            	TreeNode child = (TreeNode)children.nextElement();
            	if (nodeName.equals(child.toString()))
            	{
            		if (child != node)
            		{
            			indexCounter++;
            		}
            		else
            		{
            			foundIndex = indexCounter;
            			indexCounter++;
            		}
            	}
            }
            
            // If there were multiple children, add the index to the nodeQ.
            if (indexCounter > 1)
            	return foundIndex;
        }
        
        return -1;
    }
    
    /**
     * Construct a path for a specific node.
     * 
     * @param parent
     * @param prefix
     * @param suffix
     * @return
     */
    public static StringBuilder constructPath(TreeNode parent, String prefix, String suffix)
    {
        StringBuilder sb = new StringBuilder();
        sb.insert(0, prefix);
        
        // Get the parent if the leaf was actually passed in instead of the parent.
        if(parent.isLeaf())
            parent = parent.getParent();
                      
        LinkedList<String> nodeQ = new LinkedList<String>();
        while (parent != null)
        {
            nodeQ.add("'" + parent.toString().replaceAll(" \\(.*\\)", "") + "'");
            
            int parentIndexValue = getIndexOfNode(parent);
            if (parentIndexValue != -1)
            	nodeQ.add(nodeQ.size()-1, parentIndexValue + "");
            
            parent = parent.getParent();
        }
        
        if (!nodeQ.isEmpty())
            nodeQ.removeLast();

        while (!nodeQ.isEmpty())
        {
            sb.append("[" + nodeQ.removeLast() + "]");
        }
   
        sb.append(suffix);
        
        return sb;
    }
    
    /**
     * Construct a variable for a specfic node.
     * 
     * @param parent
     * @return
     */
    public static String constructVariable(TreeNode parent)
    {
    	String variable = "";
    	
    	// Get the parent if the leaf was actually passed in instead of the parent.
        if(parent.isLeaf())
            parent = parent.getParent();
    	
    	// Stop the loop as soon as the parent or grandparent is null,
    	// because we don't want to include the root node.
    	while (parent != null && parent.getParent() != null)
        {
    		String parentName = parent.toString();
            Pattern pattern = Pattern.compile(" (\\(.*\\))");
            Matcher matcher = pattern.matcher(parentName.toString());
            
            // Get the index of the parent about to be added.
            String parentIndex = "";
            int parentIndexValue = MirthTree.getIndexOfNode(parent);
            if (parentIndexValue != -1)
            	parentIndex += parentIndexValue;
            
            // If something has already been added, then prepend it with an "_"
            if (variable.length() != 0)
            {
            	variable = "_" + variable;
            }
            
            // Add either the vocab (if there is one) or the name.
            if (matcher.find())
            {
                variable = removeInvalidVariableCharacters(matcher.group(1)) + parentIndex + variable;
            }
            else
            {
                variable = removeInvalidVariableCharacters(parent.toString().replaceAll(" \\(.*\\)", "")) + parentIndex + variable;
            }
            
            parent = parent.getParent();
        }
    	
    	return variable;
    }
    
    /**
     * Remove invalid characters for variables and fix capitalization.
     * 
     * @param source
     * @return
     */
    private static String removeInvalidVariableCharacters(String source)
    {
        source = source.toLowerCase();
        source = source.replaceAll("\\/", " or ");
        source = source.replaceAll(" - ", "_");
        source = source.replaceAll("&", " and ");
        source = source.replace("@", "att ");
        source = source.replaceAll("\\'|\\�|\\(|\\)", "");
        
        while (source.indexOf(' ') != -1 || source.indexOf('.') != -1)
        {
        	int index = source.indexOf(' ');
        	int index2 = source.indexOf('.');
        	if (index2 != -1 && index2 < index)
        		index = index2;
        	source = source.replaceFirst(" |\\.", "");
        	source = source.substring(0, index) + source.substring(index, index+1).toUpperCase() + source.substring(index+1);
        }
        
//        if (source.equalsIgnoreCase("value"))
//        	source = "";
        
        return source;
    }
    
    public String getPrefix()
    {
    	return this.prefix;
    }
    
    public String getSuffix()
    {
    	return this.suffix;
    }
}
