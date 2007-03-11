/*
 * MirthTable.java
 *
 * Created on October 18, 2006, 10:42 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.client.ui.components;

import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import org.jdesktop.swingx.JXTable;

import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.UIConstants;

/**
 * 
 * @author brendanh
 */
public class MirthTable extends JXTable
{

    /** Creates a new instance of MirthTable */
    public MirthTable()
    {
        super();
        this.setDragEnabled(true);
        this.addKeyListener(new KeyListener()
        {

            public void keyPressed(KeyEvent e)
            {
                // TODO Auto-generated method stub
                if (e.getKeyCode() == KeyEvent.VK_S && e.isControlDown())
                {
                    PlatformUI.MIRTH_FRAME.doSaveChannel();
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
        this.addMouseWheelListener(new MouseWheelListener(){

			public void mouseWheelMoved(MouseWheelEvent e) {
				Rectangle rO = getVisibleRect();
				Rectangle r = new Rectangle((int)rO.getX(), (int)rO.getY() + (e.getWheelRotation() * e.getScrollAmount()), (int)rO.getWidth(), (int)rO.getHeight());
		        scrollRectToVisible(r);
			}
        	
        });
    }


    public Class getColumnClass(int column)
    {
        if (getValueAt(0, column) != null && getRowCount() > 0 && column > 0 && column < getColumnCount())
            return getValueAt(0, column).getClass();
        else
            return Object.class;
    }

    /**
     * Deselects all rows and sets the correct tasks visible.
     */
    public void deselectRows()
    {
        this.clearSelection();
    }

    /**
     * Gets the index of column with title 'name'.
     */
    public int getColumnNumber(String name)
    {
        for (int i = 0; i < this.getColumnCount(); i++)
        {
            if (this.getColumnName(i).equalsIgnoreCase(name))
                return i;
        }
        return UIConstants.ERROR_CONSTANT;
    }
}
