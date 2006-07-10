package com.webreach.mirth.client.ui.editors;

import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;

public class EditorConstants {

//	the default font for all editors
	public final static Font DEFAULT_FONT = new Font("Monospaced", Font.PLAIN, 12);
	public final static Font DEFAULT_FONT_BOLD = new Font("Monospaced", Font.BOLD, 12);
	
//	the colors for the line number margin
	public final static Color LINENUMBER_BACKGROUND = (new JLabel()).getBackground();
	public final static Color LINENUMBER_FOREGROUND = new Color( 119, 136, 153 );
	
}
