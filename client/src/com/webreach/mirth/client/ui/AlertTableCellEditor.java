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

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import com.webreach.mirth.model.Alert;
import com.webreach.mirth.client.ui.components.MirthFieldConstraints;

/**
 * A table cell editor for the destination table.
 */
public class AlertTableCellEditor extends AbstractCellEditor implements TableCellEditor
{
    // This is the component that will handle the editing of the cell value
    JComponent component = new JTextField();
    Frame parent;
    Object originalValue;

    public AlertTableCellEditor()
    {
        super();
        this.parent = PlatformUI.MIRTH_FRAME;
        ((JTextField) component).setDocument(new MirthFieldConstraints(40));    // set the character limit to 40.
    }

    /**
     * This method is called just before the cell value is saved. If the value
     * is not valid, false should be returned.
     */
    public boolean stopCellEditing()
    {
        String s = (String) getCellEditorValue();

        if (!valueChanged(s))
            super.cancelCellEditing();
        return super.stopCellEditing();
    }

    /**
     * This method is called when editing is completed. It must return the new
     * value to be stored in the cell.
     */
    public Object getCellEditorValue()
    {
        return ((JTextField) component).getText();
    }

    /**
     * This method is called when a cell value is edited by the user.
     */
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
    {
        // 'value' is value contained in the cell located at (rowIndex,
        // vColIndex)
        originalValue = value;

        if (isSelected)
        {
            // cell (and perhaps other cells) are selected
        }

        // Configure the component with the specified value
        ((JTextField) component).setText((String) value);

        // Return the configured component
        return component;
    }

    /**
     * Checks whether or not the value change is valid.
     */
    private boolean valueChanged(String s)
    {
        List<Alert> alerts = parent.alerts;

        // make sure the name doesn't already exist
        for (int i = 0; i < alerts.size(); i++)
        {
            if (alerts.get(i).getName().equalsIgnoreCase(s))
                return false;
        }

        parent.enableSave();
        // set the name to the new name.
        for (int i = 0; i < alerts.size(); i++)
        {
            if (alerts.get(i).getName().equalsIgnoreCase((String) originalValue))
                alerts.get(i).setName(s);
        }

        return true;
    }

    /**
     * Enables the editor only for double-clicks.
     */
    public boolean isCellEditable(EventObject evt)
    {
        if (evt instanceof MouseEvent)
        {
            return ((MouseEvent) evt).getClickCount() >= 2;
        }
        return true;
    }

}
