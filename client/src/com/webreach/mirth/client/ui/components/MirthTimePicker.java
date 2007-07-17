/*
 * NewClass.java
 *
 * Created on June 28, 2007, 12:37 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.webreach.mirth.client.ui.components;

import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.UIConstants;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;

public class MirthTimePicker extends JSpinner
{
    private DateFormatter formatter;
    private final JSpinner spinner;
    private Frame parent;
    
    public MirthTimePicker()
    {
        init("hh:mm aa", Calendar.MINUTE);
        spinner = this;
    }
    
    public MirthTimePicker(String format, int accuracy)
    {
        init(format, accuracy);
        spinner = this;
    }
    
    public void init(String format, int accuracy)
    {
        this.parent = PlatformUI.MIRTH_FRAME;
        
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        GregorianCalendar calendar = new GregorianCalendar();
        Date now = calendar.getTime();
        SpinnerDateModel dateModel = new SpinnerDateModel(now, null, null, accuracy);
        getEditor().setFont(UIConstants.TEXTFIELD_PLAIN_FONT);
        setModel(dateModel);
        JFormattedTextField tf = ((JSpinner.DefaultEditor)getEditor()).getTextField();
        
        tf.addKeyListener(new KeyListener(){
            public void keyTyped(KeyEvent e)
            {
            }

            public void keyPressed(KeyEvent e)
            {
                 parent.enableSave();  
            }

            public void keyReleased(KeyEvent e)
            {
            }
            
        });
        
        DefaultFormatterFactory factory = (DefaultFormatterFactory)tf.getFormatterFactory();
        formatter = (DateFormatter)factory.getDefaultFormatter();
        formatter.setFormat(dateFormat);
        fireStateChanged();
        
        this.addChangeListener(new ChangeListener()
        {
            public void stateChanged(ChangeEvent event)
            {
                parent.enableSave();           
            }
        });
    }
    
    public void setDate(String date)
    {
        
        try
        {
            this.setValue(formatter.stringToValue(date));
            parent.disableSave();  
        }
        catch (ParseException e)
        {
        }
    }
    
    public String getDate()
    {
        return ((JSpinner.DefaultEditor)spinner.getEditor()).getTextField().getText();
    }
}
