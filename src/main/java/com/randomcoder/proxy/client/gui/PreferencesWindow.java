package com.randomcoder.proxy.client.gui;

import java.awt.*;

import javax.swing.*;

public class PreferencesWindow extends JFrame
{
	public PreferencesWindow()
	{
		setIconImage(new ImageIcon(getClass().getResource("/icon-512x512.png")).getImage());
		
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
	}
	
	public void loadSettings()
	{
		// TODO stub
	}
	
	public void saveSettings()
	{
		// TODO stub
	}
	
	public static void main(String[] args)
	{
		PreferencesWindow prefs = new PreferencesWindow();
		prefs.setVisible(true);
	}
}