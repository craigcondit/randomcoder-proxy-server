package com.randomcoder.proxy.client.gui;

import static javax.swing.ScrollPaneConstants.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import com.randomcoder.apple.eawt.*;

/**
 * Main window for HTTP proxy.
 * 
 * <pre>
 * Copyright (c) 2007, Craig Condit. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *     
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS &quot;AS IS&quot;
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * </pre>
 */
public class MainWindow extends JFrame
{
	private static final long serialVersionUID = -7201135008538343607L;

	private final JTextField connectionName;
	private final JTextField proxyUrl;
	private final JTextField username;
	private final JPasswordField password;
	private final JCheckBox savePassword;
	private final JTextField remoteHost;
	private final JTextField remotePort;
	private final JTextField localPort;
	private final JList connectionList;
	private final JButton addButton;
	private final JButton deleteButton;
	
	public MainWindow()
	{
		super("Preferences");
		
		boolean mac = Application.isSupported();
		
		JMenuBar menuBar = new JMenuBar();
		
		if (mac)
		{
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			Application.getApplication().addApplicationListener(new ApplicationAdapter()
			{

				@Override
				public void handleOpenApplication(ApplicationEvent event)
				{
					System.err.println("open");
				}

				@Override
				public void handleQuit(ApplicationEvent event)
				{
					handleExit();
					event.setHandled(true);
				}

				@Override
				public void handleReOpenApplication(ApplicationEvent event)
				{
					System.err.println("reopen");
				}
				
			});
		}
		else
		{
			JMenu fileMenu = new JMenu("File");
			fileMenu.setMnemonic(KeyEvent.VK_F);
			
			JMenuItem exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
			exitItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					handleExit();
				}
			});
			exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
			fileMenu.add(exitItem);
			
			menuBar.add(fileMenu);
		}
		
		setJMenuBar(menuBar);
		
		setIconImage(new ImageIcon(getClass().getResource("/socket.png")).getImage());
		
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		
		ProxyListModel listModel = new ProxyListModel();
		
		connectionList = new JList(listModel);
		connectionList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		ProxyListCellRenderer cellRenderer = new ProxyListCellRenderer();
		connectionList.setCellRenderer(cellRenderer);
		connectionList.setFocusable(false);
		
		connectionList.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				if (e.getValueIsAdjusting())
				{
					System.err.println("adjusting");
					return;
				}
				
				System.err.println("Stopped");
				
				deleteButton.setEnabled(connectionList.getSelectedIndex() >= 0);
			}
			
		});
		
		JScrollPane connListPane = new JScrollPane(
				connectionList, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_NEVER);

		connListPane.setMinimumSize(new Dimension(150, 1));
		connListPane.setPreferredSize(new Dimension(150, 1));
		connListPane.setFocusable(false);
		
		content.add(connListPane, new GridBagConstraints(
				0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
				new Insets(12, 12, 0, 11), 0, 0));

		JPanel prefPanel = new JPanel(new GridBagLayout());
		
		prefPanel.add(new JLabel("Connection name:"), new GridBagConstraints(
				0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 12, 18, 11), 0, 0));

		connectionName = new JTextField(20);
		connectionName.setEnabled(false);
		
		prefPanel.add(connectionName, new GridBagConstraints(
				1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 17, 0), 0, 0));

		prefPanel.add(new JLabel("Proxy URL:"), new GridBagConstraints(
				0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 12, 12, 11), 0, 0));

		proxyUrl = new JTextField(20);
		proxyUrl.setEnabled(false);
		
		prefPanel.add(proxyUrl, new GridBagConstraints(
				1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 11, 0), 0, 0));

		prefPanel.add(new JLabel("Username:"), new GridBagConstraints(
				0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 12, 12, 11), 0, 0));

		username = new JTextField(10);
		username.setEnabled(false);
		
		prefPanel.add(username, new GridBagConstraints(
				1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 11, 0), 0, 0));
		
		prefPanel.add(new JLabel("Password:"), new GridBagConstraints(
				0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 12, 12, 11), 0, 0));

		password = new JPasswordField(10);
		password.setEnabled(false);
		
		prefPanel.add(password, new GridBagConstraints(
				1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 11, 0), 0, 0));

		savePassword = new JCheckBox("Save password");
		savePassword.setEnabled(false);
		
		prefPanel.add(savePassword, new GridBagConstraints(
				1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 12, 0), 0, 0));

		prefPanel.add(new JLabel("Remote host:"), new GridBagConstraints(
				0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 12, 12, 11), 0, 0));

		remoteHost = new JTextField(15);
		remoteHost.setEnabled(false);
		
		prefPanel.add(remoteHost, new GridBagConstraints(
				1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 11, 0), 0, 0));
		
		prefPanel.add(new JLabel("Remote port:"), new GridBagConstraints(
				0, 6, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 12, 12, 11), 0, 0));

		remotePort = new JTextField(5);
		remotePort.setEnabled(false);

		prefPanel.add(remotePort, new GridBagConstraints(
				1, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 11, 0), 0, 0));
		
		prefPanel.add(new JLabel("Local port:"), new GridBagConstraints(
				0, 7, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(0, 12, 0, 11), 0, 0));

		localPort = new JTextField(5);
		localPort.setEnabled(false);
		
		prefPanel.add(localPort, new GridBagConstraints(
				1, 7, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 0), 0, 0));
		
		content.add(prefPanel, new GridBagConstraints(
				1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
				new Insets(12, 0, 0, 11), 0, 0));
		
		JPanel buttonBar = new JPanel(new GridBagLayout());
		
		addButton = new JButton(new ImageIcon(getClass().getResource("/plus.png")));
		addButton.setMargin(new Insets(2,2,2,2));
		addButton.setFocusable(false);
		addButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				handleAdd();
			}
		});
		buttonBar.add(addButton, new GridBagConstraints(
				0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 5), 0, 0));
		
		deleteButton = new JButton(new ImageIcon(getClass().getResource("/minus.png")));
		deleteButton.setMargin(new Insets(2,2,2,2));
		deleteButton.setFocusable(false);
		deleteButton.setEnabled(false);
		deleteButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				handleDelete();
			}
		});
		buttonBar.add(deleteButton, new GridBagConstraints(
				1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(0, 0, 0, 5), 0, 0));
		
		content.add(buttonBar, new GridBagConstraints(
				0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE,
				new Insets(11, 12, 11, 11), 0, 0));
		
		pack();
		
		connListPane.setMinimumSize(connListPane.getSize());		
		
		prefPanel.setMinimumSize(prefPanel.getSize());
		setMinimumSize(getSize());

		setResizable(false);
		
		// add a resizer
		addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentResized(ComponentEvent event)
			{
				
				JFrame src = (JFrame) event.getSource();				
				Dimension min = getMinimumSize();
				Dimension curr = src.getSize();
	
				double w = curr.getWidth();
				double h = curr.getHeight();
				
				if (curr.getWidth() < min.getWidth())
					w = min.getWidth();
				
				if (curr.getHeight() < min.getHeight())
					h = min.getHeight();
	
				final Dimension resized = new Dimension((int) w, (int) h);
				
				setSize(resized);
			}			
		});
		
		setLocationRelativeTo(null);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
	
	public static void main(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
			MainWindow window = new MainWindow();
			window.setVisible(true);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
	
	protected void handleAdd()
	{
		System.err.println("add");
	}
	
	protected void handleDelete()
	{
		int index = connectionList.getSelectedIndex();
		if (index < 0)
			return;
		
		System.err.println("delete");
		JOptionPane.showConfirmDialog(this, "Are you sure?");
	}
	
	protected void handleExit()
	{
		System.err.println("exit");
	}
	
	protected final class ProxyListModel extends AbstractListModel
	{
		private static final long serialVersionUID = 1393206449025185349L;

		public Object getElementAt(int index)
		{
			return "Really really long Item #" + index;
		}

		public int getSize()
		{
			return 5;
		}
	}
	
	protected final class ProxyListCellRenderer extends DefaultListCellRenderer
	{
		private static final long serialVersionUID = 5013801825421704387L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
		{
			JComponent comp = (JComponent) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (index % 2 == 0)
				comp.setFont(comp.getFont().deriveFont(Font.BOLD));

			comp.setToolTipText(value.toString());
			
			return comp;
		}		
	}
}