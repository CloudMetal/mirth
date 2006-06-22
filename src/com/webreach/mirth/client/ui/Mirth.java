package com.webreach.mirth.client.ui;

import com.webreach.mirth.client.core.Client;
import com.webreach.mirth.client.core.ClientException;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Dimension;
import org.jdesktop.swingx.JXLoginPanel;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.auth.JDBCLoginService;
import org.jdesktop.swingx.auth.LoginEvent;
import org.jdesktop.swingx.auth.LoginListener;
import org.jdesktop.swingx.auth.LoginService;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.jdesktop.swingx.plaf.windows.WindowsLookAndFeelAddons;

/**
 * <p>Title: Mirth Beta Prototype</p>
 *
 * <p>Description: Mirth Beta Prototype</p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: WebReach, Inc.</p>
 *
 * @author Brendan Haverlock and Jacob Brauer
 * @version 1.0
 */
public class Mirth 
{
    boolean packFrame = false;
    public Client client;
    /**
     * Construct and show the application.
     */
    public Mirth(Client m) 
    {
        Frame frame = new Frame(m);
        // Validate frames that have preset sizes
        // Pack frames that have useful preferred size info, e.g. from their layout
        if (packFrame) 
        {
            frame.pack();
        } 
        else 
        {
            frame.validate();
        }

        frame.setSize(800,600);
        frame.setLocationRelativeTo(null);
        //frame.setExtendedState(frame.MAXIMIZED_BOTH);
        frame.setVisible(true);
        frame.addComponentListener(new java.awt.event.ComponentAdapter() 
        {
            public void componentResized(ComponentEvent e) 
            {
               Frame tmp = (Frame)e.getSource();
               if (tmp.getWidth()<800 || tmp.getHeight()<600) 
               {
                 tmp.setSize(800, 600);
               }
            }
        });
    }
    
    /**
     * Application entry point.
     *
     * @param args String[]
     */
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                try
                {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                }
                catch (Exception exception)
                {
                    exception.printStackTrace();
                }
                
                final MirthLoginService svc = new MirthLoginService(); 
                JXLoginPanel panel = new JXLoginPanel(svc, null, null, null);
                
                panel.setBannerText("");
                Constants.BACKGROUND_IMAGE = new ImageIcon(panel.getUI().getBanner());
                
                panel.setBannerText("Login :: Mirth");
                panel.setOpaque(true);
                JPanel loginInfo = (JPanel)((JPanel)panel.getComponent(1)).getComponent(1);
                
                loginInfo.removeAll();
                
                String CLASS_NAME = JXLoginPanel.class.getCanonicalName(); 
                JLabel serverLabel = new JLabel("Server");
                javax.swing.JTextField serverName = new javax.swing.JTextField("", 30); 
                JLabel nameLabel = new JLabel(UIManager.getString(CLASS_NAME + ".nameString")); 
                JLabel passwordLabel = new JLabel(UIManager.getString(CLASS_NAME + ".passwordString"));  
                javax.swing.JTextField nameField = new javax.swing.JTextField("", 15); 
                JPasswordField passwordField = new JPasswordField("", 15);  
                               
                loginInfo.setLayout(new GridBagLayout());  
                
                GridBagConstraints gridBagConstraints = new GridBagConstraints();  
                gridBagConstraints.gridx = 0;  
                gridBagConstraints.gridy = 0;  
                gridBagConstraints.anchor = GridBagConstraints.WEST;  
                gridBagConstraints.insets = new Insets(0, 0, 5, 11);  
                loginInfo.add(serverLabel, gridBagConstraints);  

                gridBagConstraints = new GridBagConstraints();  
                gridBagConstraints.gridx = 1;  
                gridBagConstraints.gridy = 0;  
                gridBagConstraints.gridwidth = 1;  
                gridBagConstraints.anchor = GridBagConstraints.WEST;  
                gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;  
                gridBagConstraints.weightx = 1.0;  
                gridBagConstraints.insets = new Insets(0, 0, 5, 0);  
                loginInfo.add(serverName, gridBagConstraints);  
                
                gridBagConstraints = new GridBagConstraints();  
                gridBagConstraints.gridx = 0;  
                gridBagConstraints.gridy = 1;  
                gridBagConstraints.anchor = GridBagConstraints.WEST;  
                gridBagConstraints.insets = new Insets(0, 0, 5, 11);  
                loginInfo.add(nameLabel, gridBagConstraints);  

                gridBagConstraints = new GridBagConstraints();  
                gridBagConstraints.gridx = 1;  
                gridBagConstraints.gridy = 1;  
                gridBagConstraints.gridwidth = 1;  
                gridBagConstraints.anchor = GridBagConstraints.WEST;  
                gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;  
                gridBagConstraints.weightx = 1.0;  
                gridBagConstraints.insets = new Insets(0, 0, 5, 0);  
                loginInfo.add(nameField, gridBagConstraints);  

                gridBagConstraints = new GridBagConstraints();  
                gridBagConstraints.gridx = 0;  
                gridBagConstraints.gridy = 2;  
                gridBagConstraints.anchor = GridBagConstraints.WEST;  
                gridBagConstraints.insets = new Insets(0, 0, 11, 11);  
                loginInfo.add(passwordLabel, gridBagConstraints);  

                gridBagConstraints = new GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy = 2;  
                gridBagConstraints.gridwidth = 1;  
                gridBagConstraints.anchor = GridBagConstraints.WEST;  
                gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;  
                gridBagConstraints.weightx = 1.0;
                gridBagConstraints.insets = new Insets(0, 0, 11, 0);  
                loginInfo.add(passwordField, gridBagConstraints);  
                
                loginInfo.getComponent(5).setFont(loginInfo.getComponent(1).getFont());
                svc.setPanel(loginInfo);
                svc.addLoginListener(new MirthLoginListener());
                /*JXLoginPanel.Status status = JXLoginPanel.showLoginDialog(null, panel);
                if(status == JXLoginPanel.Status.SUCCEEDED)
                    new Mirth(svc.getClient());
                */
                
                final JXLoginPanel.JXLoginFrame frm = JXLoginPanel.showLoginFrame(panel);

                frm.addWindowListener(new WindowAdapter()
                {
                    public void windowClosed(WindowEvent e)
                    {
                        JXLoginPanel.Status status = frm.getStatus();
                        if (status == JXLoginPanel.Status.SUCCEEDED)
                        {
                            new Mirth(svc.getClient());
                        } 
                        else
                        {
                            System.out.println("Login Failed: " + status);
                        }
                    }
                });

                frm.setVisible(true);
            }
        });
    }
}

class MirthLoginListener implements LoginListener 
{
        public MirthLoginListener() 
        {
        }
        
        public void loginSucceeded(LoginEvent source) 
        {
        }
        public void loginStarted(LoginEvent source) 
        {
        }
        public void loginFailed(LoginEvent source) 
        {
        }
        public void loginCanceled(LoginEvent source) 
        {
            System.exit(0);
        }
}

class MirthLoginService extends LoginService 
{
        Client c;
        JPanel p;
        
        public MirthLoginService() 
        {
        }
        
        public void setPanel(JPanel p)
        {
            this.p = p;
        }
        
        public Client getClient()
        {
            return c;
        }
        
        public boolean authenticate(final String username, char[] pass, String server) throws Exception 
        {
            //String username = String.valueOf(frm.getPanel().getUserName());
            //String password =  String.valueOf(frm.getPanel().getPassword());  
            String user = "admin";
            String pw = "abc12345";
            String svr = "http://34.34.34.69:8080";
            String mirthServer = ((javax.swing.JTextField)p.getComponent(1)).getText();
            //c = new Client(mirthServer);
            c = new Client(svr);
            try
            {
                //if(c.login(username,new String(pass)))
                if(c.login(user,pw))
                {
                    return true;
                }
            }
            catch (ClientException ex)
            {
                System.out.println("Could not connect to server...");
            }      
            return false;
        }
}
