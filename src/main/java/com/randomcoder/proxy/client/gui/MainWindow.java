package com.randomcoder.proxy.client.gui;

import static javax.swing.ScrollPaneConstants.*;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;

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

	public MainWindow()
	{
		super("HTTP Proxy");
		
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
		
		JList connList = new JList(listModel);
		connList.setMinimumSize(new Dimension(250, 1));
		connList.setMinimumSize(new Dimension(250, 1));
		
		JScrollPane connListPane = new JScrollPane(connList, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_AS_NEEDED);
		content.add(connListPane, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(12, 12, 0, 11), 0, 0));
		
		ProxyTableModel model = new ProxyTableModel();
		
		JTable connTable = new JTable(model);
		connTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		connTable.getColumnModel().getColumn(0).setPreferredWidth(100);
		connTable.getColumnModel().getColumn(1).setPreferredWidth(100);
		connTable.getColumnModel().getColumn(2).setPreferredWidth(100);
		connTable.getColumnModel().getColumn(3).setPreferredWidth(100);
		connTable.getColumnModel().getColumn(4).setPreferredWidth(100);
		connTable.getColumnModel().getColumn(5).setPreferredWidth(100);
		connTable.getColumnModel().getColumn(6).setPreferredWidth(100);
		
		JScrollPane pane = new JScrollPane(connTable, VERTICAL_SCROLLBAR_ALWAYS, HORIZONTAL_SCROLLBAR_AS_NEEDED);
		pane.setPreferredSize(new Dimension((int) connTable.getPreferredScrollableViewportSize().getWidth(), 200));
		content.add(pane, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(12, 12, 0, 11), 0, 0));
		
		JPanel buttonBar = new JPanel(new GridBagLayout());
		
		JButton addButton = new JButton(new ImageIcon(getClass().getResource("/plus.png")));
		addButton.setMargin(new Insets(0,0,0,0));
		addButton.setFocusable(false);
		addButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				handleAdd();
			}
		});
		buttonBar.add(addButton, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
		
		JButton deleteButton = new JButton(new ImageIcon(getClass().getResource("/minus.png")));
		deleteButton.setMargin(new Insets(0,0,0,0));
		deleteButton.setFocusable(false);
		deleteButton.setEnabled(false);
		deleteButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				handleEdit();
			}
		});
		buttonBar.add(deleteButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
		
		content.add(buttonBar, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(11, 12, 11, 11), 0, 0));
		
		pack();
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
	
	protected void handleEdit()
	{
		System.err.println("edit");
	}
	
	protected void handleExit()
	{
		System.err.println("exit");
	}
	
	protected final class ProxyTableModel extends AbstractTableModel
	{
		private static final long serialVersionUID = -8408408044928354604L;

		@Override
		public String getColumnName(int column)
		{
			switch (column)
			{
				case 0:
					return "Proxy URL";
				case 1:
					return "Username";
				case 2:
					return "Password";
				case 3:
					return "Remote host";
				case 4:
					return "Remote port";
				case 5:
					return "Local port";
				case 6:
					return "Status";
				default:
					return null;
			}
		}

		public int getColumnCount()
		{
			// TODO Auto-generated method stub
			return 7;
		}

		public int getRowCount()
		{
			// TODO Auto-generated method stub
			return 3;
		}

		public Object getValueAt(int rowIndex, int columnIndex)
		{
			// TODO Auto-generated method stub
			return "test";
		}
	}
	
	protected final class ProxyListModel extends AbstractListModel
	{
		private static final long serialVersionUID = 1393206449025185349L;

		public Object getElementAt(int index)
		{
			return "Item #" + index;
		}

		public int getSize()
		{
			return 5;
		}
	}
}