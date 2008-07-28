package com.webreach.mirth.plugins.dashboardstatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.table.TableCellRenderer;

import com.webreach.mirth.client.core.ClientException;
import com.webreach.mirth.client.ui.CellData;
import com.webreach.mirth.client.ui.DashboardPanel;
import com.webreach.mirth.client.ui.Frame;
import com.webreach.mirth.client.ui.ImageCellRenderer;
import com.webreach.mirth.client.ui.PlatformUI;
import com.webreach.mirth.client.ui.UIConstants;
import com.webreach.mirth.model.ChannelStatus;
import com.webreach.mirth.plugins.DashboardColumnPlugin;

public class DashboardConnectorStatusColumn extends DashboardColumnPlugin {
	private static final String _SOURCE_CONNECTOR = "_source_connector";
	private static final String GET_STATES = "getStates";
	private static final String SERVER_PLUGIN_NAME = "Dashboard Status Column Server";
	private HashMap<String, String[]> currentStates;
	private ImageIcon greenBullet;
    private ImageIcon yellowBullet;
    private ImageIcon redBullet;
    private ImageIcon blackBullet;
    private Map<String, ImageIcon>  iconMap = new HashMap<String, ImageIcon>();
    
    public DashboardConnectorStatusColumn(String name) {
        super(name);
    }
    
    public DashboardConnectorStatusColumn(String name, DashboardPanel parent) {
		super(name, parent);
		greenBullet = new ImageIcon(Frame.class.getResource("images/bullet_green.png"));
        yellowBullet = new ImageIcon(Frame.class.getResource("images/bullet_yellow.png"));
        redBullet = new ImageIcon(Frame.class.getResource("images/bullet_red.png"));
        blackBullet = new ImageIcon(Frame.class.getResource("images/bullet_black.png"));
        iconMap.put("green", greenBullet);
        iconMap.put("yellow", yellowBullet);
        iconMap.put("red", redBullet);
        iconMap.put("black", blackBullet);
	}

	@Override
	public TableCellRenderer getCellRenderer() {
		return new ImageCellRenderer();
	}

	@Override
	public String getColumnHeader() {
		return "Connection";
	}

	@Override
	public int getMaxWidth() {
		return UIConstants.MAX_WIDTH;
	}

	@Override
	public int getMinWidth() {
		return 100;
	}

	@Override
	public Object getTableData(ChannelStatus status) {
		String connectorName = status.getChannelId() + _SOURCE_CONNECTOR;
		if (currentStates != null && currentStates.containsKey(connectorName)){
			String[] stateData = currentStates.get(connectorName);
			return new CellData(iconMap.get(stateData[0]), stateData[1]);
		}else{
			return new CellData(blackBullet, "Unknown");
		}
	}

	@Override
	public boolean showBeforeStatusColumn() {
		return false;
	}
	
	
	@Override
	public void tableUpdate(List<ChannelStatus> status) {
		//get states from server
		try {
			currentStates = (HashMap<String, String[]>) PlatformUI.MIRTH_FRAME.mirthClient.invokePluginMethod(SERVER_PLUGIN_NAME, GET_STATES, null);
		} catch (ClientException e) {
			PlatformUI.MIRTH_FRAME.alertException(PlatformUI.MIRTH_FRAME, e.getStackTrace(), e.getMessage());
           // we can safely ignore this
		   //e.printStackTrace();
		}
	}

}
