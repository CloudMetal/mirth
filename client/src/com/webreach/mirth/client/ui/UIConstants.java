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

import java.awt.Color;
import java.awt.Font;

import javax.swing.ImageIcon;

/**
 * A constants class for the Mirth UI
 */
public class UIConstants
{
    // for EOL stuff
    public static final String EOL_JAVA = "\n";
    public static final String EOL_UNIX = "\n";
    public static final String EOL_WIN32 = "\r\n";
    public static final String EOL_MAC = "\r";

    // for Frame
    public static final int TASK_PANE_WIDTH = 170;
    public static final String TITLE_TEXT = "Mirth Connect Administrator";
    public static final int MIRTH_WIDTH = 950;
    public static final int MIRTH_HEIGHT = 650;
    public static final ImageIcon MIRTHCORP_LOGO = new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/mirthcorp_24h.png"));
    public static final ImageIcon MIRTHCONNECT_LOGO_GRAY = new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/mirthconnect_gray_24h.png"));
    public static final String MIRTHCORP_TOOLTIP = "Mirth Corporation";
    public static final String MIRTHCONNECT_TOOLTIP = "Mirth Connect";
    public static final String MIRTHCORP_URL = "http://www.mirthcorp.com";
    public static final String MIRTHCONNECT_URL = "http://www.mirthcorp.com/products/mirth-connect";
    public static final String EDIT_FILTER = "Edit Filter";
    public static final String EDIT_TRANSFORMER = "Edit Transformer";
    public static final int EDIT_FILTER_TASK_NUMBER = 9;
    public static final int EDIT_TRANSFORMER_TASK_NUMBER = 10;

    // for error checking
    public static final int ERROR_CONSTANT = -1;

    // for JXTables
    public static final int ROW_HEIGHT = 20;
    public static final int COL_MARGIN = 10;
    public static final Color GRID_COLOR = new Color(224, 224, 224);
    public static final int MIN_WIDTH = 75;
    public static final int MAX_WIDTH = 200;
    public static final int WIDTH_SHORT_MIN = 20;
    public static final int WIDTH_SHORT_MAX = 50;
            
    public static final Color HIGHLIGHTER_COLOR = new Color(240, 240, 240);

    // background colors
    public static final Color BACKGROUND_COLOR = new Color(255, 255, 255);
    public static final Color TITLE_TEXT_COLOR = new Color(0, 0, 0);
    public static final Color HEADER_TITLE_TEXT_COLOR = new Color(255, 255, 255);
    public static final Color COMBO_BOX_BACKGROUND = new Color(220, 220, 220);
    public static final Color JX_CONTAINER_BACKGROUND_COLOR = new Color(0x9EB1C9);

    // for JSplitPane
    public static final int DIVIDER_SIZE = 12;

    // fonts
    public static final Font TEXTFIELD_PLAIN_FONT = new Font("Tahoma", Font.PLAIN, 11);
    public static final Font TEXTFIELD_BOLD_FONT = new Font("Tahoma", Font.BOLD, 11);
    public static final Font BANNER_FONT = new Font("Arial", Font.BOLD, 36);
    public static final Font DIALOG_FONT = new Font("Dialog", Font.PLAIN, 12);

    // issue link
    public static final String ISSUE_TRACKER_LOCATION = "http://www.mirthcorp.com/community/issues/";
    
    // help links
    public static final String ALERTS_HELP_LOCATION = "Alerts";
    public static final String CHANNEL_HELP_LOCATION = "Edit+Channel";
    public static final String TRANFORMER_HELP_LOCATION = "Transformers";
    public static final String FILTER_HELP_LOCATION = "Filters";
    public static final String CHANNELS_HELP_LOCATION = "Channels";
    public static final String DASHBOARD_HELP_LOCATION = "Dashboard";
    public static final String MESSAGE_BROWSER_HELP_LOCATION = "Message+Browser";
    public static final String SYSTEM_EVENT_HELP_LOCATION = "Events";
    public static final String SETTINGS_HELP_LOCATION = "Settings";
    public static final String USERS_HELP_LOCATION = "Users";
    public static final String PLUGINS_HELP_LOCATION = "Plugins";
    // for JTrees
    public static final ImageIcon LEAF_ICON = new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_green.png"));
    public static final ImageIcon OPEN_ICON = new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_yellow.png"));
    public static final ImageIcon CLOSED_ICON = new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_yellow.png"));

    // for Forms
    public static final String DOES_NOT_EXISTS_OPTION = "3";
    public static final String EXISTS_OPTION = "2";
    public static final String YES_OPTION = "1";
    public static final String NO_OPTION = "0";
    public static final Color INVALID_COLOR = Color.PINK;

    // ast: encodings
    public static final String DEFAULT_ENCODING_OPTION = "DEFAULT_ENCODING";
    public static final String UTF8_OPTION = "UTF-8";
    public static final String UTF16LE_OPTION = "UTF-16LE";
    public static final String UTF16BE_OPTION = "UTF-16BE";
    public static final String UTF16BOM_OPTION = "UTF-16";
    public static final String LATIN1_OPTION = "ISO-8859-1";
    public static final String USASCII_OPTION = "US-ASCII";

    // for incoming / outgoing
    public static final String INCOMING_DATA = "Incoming";
    public static final String OUTGOING_DATA = "Outgoing";
    
    // variable constants
    public static final String IS_GLOBAL = "isGlobal";
    public static final String IS_GLOBAL_CHANNEL = "channel";
    public static final String IS_GLOBAL_CONNECTOR = "connector";
    public static final String IS_GLOBAL_GLOBAL = "global";
    public static final String IS_GLOBAL_RESPONSE = "response";
    
    // for tables
    public static final String ENABLED_STATUS = "Enabled";
    public static final String DISABLED_STATUS = "Disabled";
    
    // for message template
    public static final ImageIcon FILE_PICKER_ICON = new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/folder_explore.png"));

    // for dashboard connector monitoring and Mirth server logs
    public static final ImageIcon PAUSE_LOG_ICON = new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/control_pause_blue.png"));
    public static final ImageIcon RESUME_LOG_ICON = new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/control_play_blue.png"));
    public static final ImageIcon CLEAR_LOG_ICON = new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/cross.png"));
    public static final ImageIcon CHANGE_LOGSIZE_ICON = new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/tick.png"));

    // for dashboard statistics
    public static final ImageIcon GREEN_BULLET = new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_green.png"));
    public static final ImageIcon YELLOW_BULLET = new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_yellow.png"));
    public static final ImageIcon RED_BULLET = new ImageIcon(com.webreach.mirth.client.ui.Frame.class.getResource("images/bullet_red.png"));

    // for privacy
    public static final String PRIVACY_URL = "http://www.mirthcorp.com/company/about/privacy#mirthconnect";
    public static final String PRIVACY_TOOLTIP = "Privacy Information";
}
