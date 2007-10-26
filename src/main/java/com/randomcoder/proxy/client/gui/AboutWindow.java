package com.randomcoder.proxy.client.gui;

import java.awt.*;

import javax.swing.*;

public class AboutWindow extends JFrame
{
	private static final long serialVersionUID = -7783337611538845870L;

	public AboutWindow()
	{
		super("About HTTP Proxy");
		ImageIcon icon = new ImageIcon(getClass().getResource("/icon-128x128.png"));		
		
		JLabel iconLabel = new JLabel(icon);
		
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		
		content.add(iconLabel, new GridBagConstraints(
			0, 0, 1, 4, 0.0, 0.0,
			GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
			new Insets(12, 12, 12, 12), 0, 0));
		setIconImage(icon.getImage());
		
		JLabel titleLabel = new JLabel("HTTP Proxy");
		titleLabel.setFont(titleLabel.getFont().deriveFont(24.0f).deriveFont(Font.BOLD));
		content.add(titleLabel, new GridBagConstraints(
			1, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
			new Insets(12, 0, 0, 12), 0, 0));
		
		JLabel versionLabel = new JLabel("1.0.0-SNAPSHOT");
		versionLabel.setFont(versionLabel.getFont().deriveFont(12.0f).deriveFont(Font.BOLD));
		content.add(versionLabel, new GridBagConstraints(
			1, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
			new Insets(0, 0, 12, 12), 0, 0));
		
		JLabel copyLabel1 = new JLabel(((char) 0xA9) + " 2007 Craig Condit.");
		copyLabel1.setFont(copyLabel1.getFont().deriveFont(12.0f).deriveFont(Font.PLAIN));
		content.add(copyLabel1, new GridBagConstraints(
				1, 2, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 12), 0, 0));

		JLabel copyLabel2 = new JLabel("All rights reserved.");
		copyLabel2.setFont(copyLabel2.getFont().deriveFont(12.0f).deriveFont(Font.PLAIN));
		content.add(copyLabel2, new GridBagConstraints(
				1, 3, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(0, 0, 12, 12), 0, 0));
		
		pack();
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
}