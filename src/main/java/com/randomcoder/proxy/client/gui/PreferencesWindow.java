package com.randomcoder.proxy.client.gui;

import static javax.swing.ScrollPaneConstants.*;

import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.event.*;

import org.apache.commons.lang.StringUtils;

import com.randomcoder.proxy.client.config.ProxyConfiguration;
import com.randomcoder.proxy.client.validation.ValidationResult;

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
public class PreferencesWindow extends JFrame
{
	private static final long serialVersionUID = 3758601335874262188L;
	
	private final JTextField connectionName;
	private final JTextField proxyUrl;
	private final JTextField username;
	private final JPasswordField password;
	private final JTextField remoteHost;
	private final JTextField remotePort;
	private final JTextField localPort;
	private final JList connectionList;
	private final JButton addButton;
	private final JButton deleteButton;
	private final ProxyListModel listModel;
	
	private boolean dirty = false;
	private ProxyConfiguration current;
	private ProxyConfiguration original;
	private int currentIndex = -1;
	
	public PreferencesWindow()
	{
		super("HTTP Proxy Preferences");
		
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
				
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				if (!validateForm(true))
					return;
								
				setVisible(false);
			}			
		});
		
		// set the window's icon
		setIconImage(new ImageIcon(getClass().getResource("/icon-512x512.png")).getImage());
		
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		
		listModel = new ProxyListModel();
		
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
					return;
				
				deleteButton.setEnabled(connectionList.getSelectedIndex() >= 0);
				
				// change the currently active item
				handleEdit(false);
			}
		});

		JScrollPane connListPane = new JScrollPane(
			connectionList, VERTICAL_SCROLLBAR_ALWAYS,
			HORIZONTAL_SCROLLBAR_NEVER);

		connListPane.setMinimumSize(new Dimension(150, 1));
		connListPane.setPreferredSize(new Dimension(150, 1));
		connListPane.setFocusable(false);
		
		content.add(connListPane, new GridBagConstraints(
			0, 0, 1, 1, 1.0, 1.0,
			GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH,
			new Insets(12, 12, 0, 11), 0, 0));

		JPanel prefPanel = new JPanel(new GridBagLayout());
		
		prefPanel.add(new JLabel("Connection name:"), new GridBagConstraints(
			0, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.EAST, GridBagConstraints.NONE,
			new Insets(0, 12, 18, 11), 0, 0));

		connectionName = new JTextField(25);
		connectionName.setEnabled(false);
		connectionName.getDocument().addDocumentListener(new ProxyDocumentListener(connectionName));
		connectionName.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				if (current != null)
					connectionName.setText(current.getName());
			}
		});
		
		prefPanel.add(connectionName, new GridBagConstraints(
			1, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(0, 0, 17, 0), 0, 0));

		prefPanel.add(new JLabel("Proxy URL:"), new GridBagConstraints(
			0, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.EAST, GridBagConstraints.NONE,
			new Insets(0, 12, 12, 11), 0, 0));

		proxyUrl = new JTextField(25);
		proxyUrl.setEnabled(false);
		proxyUrl.getDocument().addDocumentListener(new ProxyDocumentListener(proxyUrl));
		proxyUrl.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				if (current != null)
					proxyUrl.setText(current.getProxyUrl());
			}
		});
		
		prefPanel.add(proxyUrl, new GridBagConstraints(
			1, 1, 1, 1, 0.0, 0.0,
			GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(0, 0, 11, 0), 0, 0));

		prefPanel.add(new JLabel("Username:"), new GridBagConstraints(
			0, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.EAST, GridBagConstraints.NONE,
			new Insets(0, 12, 12, 11), 0, 0));

		username = new JTextField(15);
		username.setEnabled(false);
		username.getDocument().addDocumentListener(new ProxyDocumentListener(username));
		
		prefPanel.add(username, new GridBagConstraints(
			1, 2, 1, 1, 0.0, 0.0,
			GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(0, 0, 11, 0), 0, 0));
		
		prefPanel.add(new JLabel("Password:"), new GridBagConstraints(
			0, 3, 1, 1, 0.0, 0.0,
			GridBagConstraints.EAST, GridBagConstraints.NONE,
			new Insets(0, 12, 12, 11), 0, 0));

		password = new JPasswordField(15);
		password.setEnabled(false);
		password.getDocument().addDocumentListener(new ProxyDocumentListener(password));
		
		prefPanel.add(password, new GridBagConstraints(
			1, 3, 1, 1, 0.0, 0.0,
			GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(0, 0, 11, 0), 0, 0));

		prefPanel.add(new JLabel("Remote host:"), new GridBagConstraints(
			0, 4, 1, 1, 0.0, 0.0,
			GridBagConstraints.EAST, GridBagConstraints.NONE,
			new Insets(0, 12, 12, 11), 0, 0));

		remoteHost = new JTextField(20);
		remoteHost.setEnabled(false);
		remoteHost.getDocument().addDocumentListener(new ProxyDocumentListener(remoteHost));
		remoteHost.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				if (current != null)
					remoteHost.setText(current.getRemoteHost());
			}
		});
		
		prefPanel.add(remoteHost, new GridBagConstraints(
			1, 4, 1, 1, 0.0, 0.0,
			GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(0, 0, 11, 0), 0, 0));
		
		prefPanel.add(new JLabel("Remote port:"), new GridBagConstraints(
			0, 5, 1, 1, 0.0, 0.0,
			GridBagConstraints.EAST, GridBagConstraints.NONE,
			new Insets(0, 12, 12, 11), 0, 0));

		remotePort = new JTextField(5);
		remotePort.setEnabled(false);
		remotePort.getDocument().addDocumentListener(new ProxyDocumentListener(remotePort));
		remotePort.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				if (current != null)
				{
					Integer value = current.getRemotePort();
					remotePort.setText(value == null ? null : Integer.toString(value));
				}
			}
		});

		prefPanel.add(remotePort, new GridBagConstraints(
			1, 5, 1, 1, 0.0, 0.0,
			GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(0, 0, 11, 0), 0, 0));
		
		prefPanel.add(new JLabel("Local port:"), new GridBagConstraints(
			0, 6, 1, 1, 0.0, 0.0,
			GridBagConstraints.EAST, GridBagConstraints.NONE,
			new Insets(0, 12, 0, 11), 0, 0));

		localPort = new JTextField(5);
		localPort.setEnabled(false);
		localPort.getDocument().addDocumentListener(new ProxyDocumentListener(localPort));
		localPort.addFocusListener(new FocusAdapter()
		{
			@Override
			public void focusLost(FocusEvent e)
			{
				if (current != null)
				{
					Integer value = current.getLocalPort();
					localPort.setText(value == null ? null : Integer.toString(value));
				}
			}
		});
		
		prefPanel.add(localPort, new GridBagConstraints(
			1, 6, 1, 1, 0.0, 0.0,
			GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(0, 0, 0, 0), 0, 0));
		
		content.add(prefPanel, new GridBagConstraints(
			1, 0, 1, 1, 0.0, 0.0,
			GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
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
				0, 0, 1, 1, 0.0, 0.0,
				GridBagConstraints.WEST, GridBagConstraints.NONE,
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
			1, 0, 1, 1, 0.0, 0.0, 
			GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(0, 0, 0, 5), 0, 0));
		
		content.add(buttonBar, new GridBagConstraints(
			0, 1, 1, 1, 0.0, 0.0, 
			GridBagConstraints.WEST, GridBagConstraints.NONE,
			new Insets(11, 12, 11, 11), 0, 0));
				
		setResizable(false);
		setLocationRelativeTo(null);
		pack();
		
		loadSettings();
	}
	
	public void loadSettings()
	{
		// TODO stub
		listModel.clear();
		current = null;
		original = null;
		dirty = false;
		currentIndex = -1;
		handleEdit(false);
	}
	
	public void saveSettings()
	{
		// TODO stub
	}
	
	protected void handleAdd()
	{
		if (!validateForm(false))
			return;
		
		connectionList.setSelectedIndex(listModel.addNew());
		currentIndex = connectionList.getSelectedIndex();
		current = listModel.getElementAt(currentIndex);
		dirty = false;
		handleEdit(true);
		original = null;
	}
	
	protected void handleEdit(boolean isNew)
	{
		current = listModel.getElementAt(connectionList.getSelectedIndex());
		
		if (current == null)
		{
			connectionName.setEnabled(false);
			connectionName.setText("");
			
			proxyUrl.setEnabled(false);
			proxyUrl.setText("");
			
			username.setEnabled(false);
			username.setText("");
			
			password.setEnabled(false);
			password.setText("");
			
			remoteHost.setEnabled(false);
			remoteHost.setText("");
			
			remotePort.setEnabled(false);
			remotePort.setText("");
			
			localPort.setEnabled(false);
			localPort.setText("");
		}
		else
		{
			if (!validateForm(false))
			{
				connectionList.setSelectedIndex(currentIndex);
				current = listModel.getElementAt(currentIndex);
				return;
			}
			
			if (!dirty)
				original = isNew ? null : current.clone();
			
			dirty = false;
			currentIndex = connectionList.getSelectedIndex();
			
			DecimalFormat df = new DecimalFormat("#####");
			
			connectionName.setText(current.getName());
			connectionName.setEnabled(true);
			connectionName.setSelectionStart(0);
			connectionName.setSelectionEnd(connectionName.getText().length());
			
			proxyUrl.setText(current.getProxyUrl());
			proxyUrl.setEnabled(true);
			
			username.setText(current.getUsername());			
			username.setEnabled(true);
			
			password.setText(current.getPassword());
			password.setEnabled(true);
			
			remoteHost.setText(current.getRemoteHost());
			remoteHost.setEnabled(true);
			
			Integer currentRemotePort = current.getRemotePort();			
			remotePort.setText(currentRemotePort == null ? "" : df.format(currentRemotePort));
			remotePort.setEnabled(true);
			
			Integer currentLocalPort = current.getLocalPort();			
			localPort.setText(currentLocalPort == null ? "" : df.format(currentLocalPort));
			localPort.setEnabled(true);
		}
		
		connectionName.requestFocusInWindow();
	}
	
	protected void handleDelete()
	{
		int index = connectionList.getSelectedIndex();
		if (index < 0)
			return;
				
		current = null;
		currentIndex = -1;
		dirty = false;
		
		listModel.deleteCurrent();		
	}
	
	protected boolean validateForm(boolean prompt)
	{
		ProxyConfiguration test = listModel.getElementAt(currentIndex);
		
		if (dirty && test != null && currentIndex >= 0)
		{			
			// perform validation
			List<ValidationResult> results = test.validate();
			
			if (results.size() > 0)
			{
				StringBuilder buf = new StringBuilder();
				
				for (ValidationResult result : results)
				{
					buf.append(result.getMessage());
					buf.append("\r\n");
				}
				
				// build an error message
				if (prompt)
				{
					Object[] options = new Object[] { "Close anyway", "Continue editing" };
					
					if (JOptionPane.YES_OPTION == JOptionPane.showOptionDialog(this, "The current configuration cannot be saved due to the following problems:\r\n\r\n" + buf.toString(), "Validation errors", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]))
					{
						// undo current changes and rollback
						if (original == null)
						{
							// newly added; delete
							listModel.deleteCurrent();
							original = null;
							current = null;
							currentIndex = -1;
							dirty = false;
							return true;
						}
						else
						{
							// revert changes
							current = original.clone();
							listModel.replaceCurrent(current);
							dirty = false;
							return true;
						}
					}
					else
					{
						return false;
					}
				}
				else
				{
					// show a message dialog
					JOptionPane.showMessageDialog(this, "The following problems must be corrected:\r\n\r\n" + buf.toString(), "Validation errors", JOptionPane.WARNING_MESSAGE);
				}
				
				// reset to previously selected value
				connectionList.setSelectedIndex(currentIndex);
				return false;
			}
		}
		
		return true;
	}
	
	protected final class ProxyListModel extends AbstractListModel
	{
		private final List<ProxyConfiguration> data = new ArrayList<ProxyConfiguration>();

		private static final long serialVersionUID = 1393206449025185349L;
		
		public ProxyConfiguration getElementAt(int index)
		{
			if (index < 0 || index >= data.size())
				return null;
			
			return data.get(index);
		}

		public int getSize()
		{
			return data.size();
		}
		
		public void clear()
		{
			data.clear();
		}
		
		public int addNew()
		{
			ProxyConfiguration config = new ProxyConfiguration();
			config.setName("New Item");
			data.add(config);
			dirty = false;
			
			fireIntervalAdded(this, data.size() - 1, data.size());
			return data.size() - 1;			
		}
		
		public void updateCurrent()
		{
			fireContentsChanged(this, connectionList.getSelectedIndex(), connectionList.getSelectedIndex());
		}
		
		public void deleteCurrent()
		{
			data.remove(connectionList.getSelectedIndex());
			fireIntervalRemoved(this, connectionList.getSelectedIndex(), connectionList.getSelectedIndex());
		}
		
		public void replaceCurrent(ProxyConfiguration element)
		{
			data.set(connectionList.getSelectedIndex(), element);
			fireContentsChanged(this, connectionList.getSelectedIndex(), connectionList.getSelectedIndex());
		}
	}
	
	protected final class ProxyListCellRenderer extends DefaultListCellRenderer
	{
		private static final long serialVersionUID = 5013801825421704387L;

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
		{
			ProxyConfiguration item = (ProxyConfiguration) value;
			
			String text = StringUtils.trimToEmpty(item.getName());
			
			JComponent comp = (JComponent) super.getListCellRendererComponent(list, text + " ", index, isSelected, cellHasFocus);

			comp.setToolTipText(text);
			
			return comp;
		}		
	}
	
	protected final class ProxyDocumentListener implements DocumentListener
	{
		private final Component target;
		
		public ProxyDocumentListener(Component target)
		{
			this.target = target;
		}
		
		public void changedUpdate(DocumentEvent e) { update(e); }
		public void insertUpdate(DocumentEvent e) { update(e); }
		public void removeUpdate(DocumentEvent e) { update(e); }
		
		private void update(DocumentEvent e)
		{
			if (current == null)
				return;
			
			dirty = true;
			
			if (target == connectionName)
			{
				current.setName(connectionName.getText());
				listModel.updateCurrent();
			}
			
			if (target == proxyUrl)
				current.setProxyUrl(proxyUrl.getText());
			
			if (target == username)
				current.setUsername(username.getText());
			
			if (target == password)
				current.setPassword(new String(password.getPassword()));
			
			if (target == remoteHost)
				current.setRemoteHost(new String(remoteHost.getText()));
				
			if (target == remotePort)
			{
				Integer port = null;
				try
				{
					port = Integer.parseInt(remotePort.getText());
					if (port < 0)
						port = null;
				}
				catch (NumberFormatException nfe)
				{
					port = null;
				}
				current.setRemotePort(port);
			}
			
			if (target == localPort)
			{
				Integer port = null;
				try
				{
					port = Integer.parseInt(localPort.getText());
					if (port < 0)
						port = null;
				}
				catch (NumberFormatException nfe)
				{
					port = null;
				}
				current.setLocalPort(port);
			}
		}
	}
	
	public static void main(String[] args)
	{
		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
			PreferencesWindow window = new PreferencesWindow();
			window.setVisible(true);
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}
}