package com.randomcoder.proxy.client;

import static java.awt.GridBagConstraints.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class PasswordDialog extends JDialog
{
	private static final long serialVersionUID = -917090059644356701L;
	
	protected String username = null;
	protected String password = null;
	
	public PasswordDialog(JFrame parent, String proxyUrl)
	{
		super(parent, true);
		
		Container cp = getContentPane();
		
		cp.setLayout(new GridBagLayout());

		JLabel proxy = new JLabel(proxyUrl);
		proxy.setFont(proxy.getFont().deriveFont(Font.PLAIN));

		cp.add(new JLabel("Proxy URL:"), new GridBagConstraints(0, 0, 1, 1, 1, 1, EAST, NONE, new Insets(10,10,0,0), 0, 0));
		cp.add(proxy, new GridBagConstraints(1, 0, 2, 1, 1, 1, WEST, HORIZONTAL, new Insets(10,10,0,10), 0, 0));
		
		final JTextField userField = new JTextField();
		userField.setMaximumSize(new Dimension(100, (int) userField.getPreferredSize().getHeight()));
		userField.setPreferredSize(userField.getMaximumSize());
		userField.setMinimumSize(userField.getMaximumSize());
		userField.requestFocusInWindow();
		
		final JPasswordField passField = new JPasswordField();
		passField.setMaximumSize(new Dimension(100, (int) passField.getPreferredSize().getHeight()));
		passField.setPreferredSize(passField.getMaximumSize());
		passField.setMinimumSize(passField.getMaximumSize());
		
		cp.add(new JLabel("User name:"), new GridBagConstraints(0, 1, 1, 1, 1, 1, EAST, NONE, new Insets(10,10,0,0), 0, 0));
		cp.add(userField, new GridBagConstraints(1, 1, 2, 1, 1, 1, WEST, HORIZONTAL, new Insets(10,10,0,10), 0, 0));
		cp.add(new JLabel("Password:"), new GridBagConstraints(0, 2, 1, 1, 1, 1, EAST, NONE, new Insets(10,10,0,0), 0, 0));
		cp.add(passField, new GridBagConstraints(1, 2, 2, 1, 1, 1, WEST, HORIZONTAL, new Insets(10,10,0,10), 0, 0));
			
		JButton login = new JButton("Login");
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		getRootPane().setDefaultButton(login);
		
		getRootPane().registerKeyboardAction(new ActionListener() 
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		login.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				username = userField.getText();
				password = new String(passField.getPassword());
				dispose();
			}
		});
		cp.add(login, new GridBagConstraints(1, 3, 1, 1, 0, 0, WEST, NONE, new Insets(10,10,10,10), 0, 0));

		JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}		
		});
		cp.add(cancel, new GridBagConstraints(2, 3, 1, 1, 0, 0, WEST, NONE, new Insets(10,0,10,10), 0, 0));
		
		setTitle("Login required");
		pack();
		setResizable(false);
	}
	
	@Override
	public void setVisible(boolean visible)
	{
		// TODO Auto-generated method stub
		super.setVisible(visible);
		if (visible)
		{
			
		}
	}
	
	public String getUsername()
	{
		return username;
	}
	
	public String getPassword()
	{
		return password;
	}
}