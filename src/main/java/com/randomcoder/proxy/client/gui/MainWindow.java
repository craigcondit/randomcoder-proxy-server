package com.randomcoder.proxy.client.gui;

import java.awt.TrayIcon;
import java.awt.event.*;

import javax.swing.*;

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
	private final JFrame aboutWindow;
	private final JFrame prefsWindow;
	private TrayMenu trayMenu;
	
	public MainWindow(final JFrame aboutWindow, final JFrame prefsWindow)
	{
		super("HTTP Proxy Status");
		
		this.aboutWindow = aboutWindow;
		this.prefsWindow = prefsWindow;
			
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				if (trayMenu != null && trayMenu.isSupported())
				{
					setVisible(false);
				}
				else
				{
					if (handleExit())
						System.exit(0);
				}
			}			
		});

		boolean mac = Application.isSupported();
		
		JMenuBar menuBar = new JMenuBar();
		
		if (mac)
		{
			Application.getApplication().addApplicationListener(new ApplicationAdapter()
			{
				@Override
				public void handleOpenApplication(ApplicationEvent event)
				{
					setVisible(true);
				}

				@Override
				public void handleQuit(ApplicationEvent event)
				{
					event.setHandled(handleExit());
				}

				@Override
				public void handleReOpenApplication(ApplicationEvent event)
				{
					event.setHandled(true);
					setVisible(true);
					requestFocus();
				}

				@Override
				public void handleAbout(ApplicationEvent event)
				{
					event.setHandled(true);
					if (aboutWindow != null)
					{
						aboutWindow.setVisible(true);
						aboutWindow.requestFocus();
					}
				}

				@Override
				public void handlePreferences(ApplicationEvent event)
				{
					event.setHandled(true);
					if (prefsWindow != null)
					{
						prefsWindow.setVisible(true);
						prefsWindow.requestFocus();
					}
				}
			});
			
			JMenu windowMenu = new JMenu("Window");
			
			JMenuItem statusItem = new JMenuItem("Status");
			statusItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setVisible(true);
				}
			});
			windowMenu.add(statusItem);
			
			menuBar.add(windowMenu);			
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
					if (handleExit())
						System.exit(0);
				}
			});
			exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
			fileMenu.add(exitItem);
			
			menuBar.add(fileMenu);
			
			JMenu editMenu = new JMenu("Edit");
			editMenu.setMnemonic(KeyEvent.VK_E);
			
			JMenuItem prefsItem = new JMenuItem("Preferences", KeyEvent.VK_P);
			prefsItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if (prefsWindow != null)
					{
						prefsWindow.setVisible(true);
						prefsWindow.requestFocus();
					}
				}
			});			
			editMenu.add(prefsItem);
			
			menuBar.add(editMenu);
			
			JMenu helpMenu = new JMenu("Help");
			helpMenu.setMnemonic(KeyEvent.VK_H);
			
			JMenuItem aboutItem = new JMenuItem("About", KeyEvent.VK_A);
			aboutItem.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if (aboutWindow != null)
					{
						aboutWindow.setVisible(true);
						aboutWindow.requestFocus();
					}
				}
			});
			helpMenu.add(aboutItem);
			
			menuBar.add(helpMenu);			
		}
		
		setJMenuBar(menuBar);
				
		// set the window's icon
		setIconImage(new ImageIcon(getClass().getResource("/icon-512x512.png")).getImage());
		
		pack();
		setResizable(false);
		setLocationRelativeTo(null);
	}
	
	public void setTrayMenu(TrayMenu trayMenu)
	{
		this.trayMenu = trayMenu;
	}
	
	protected boolean handleExit()
	{
		// TODO stub
		return true;
	}
	
	public static void main(String[] args)
	{
		try
		{
			System.setProperty("apple.laf.useScreenMenuBar", "true");
			
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
			AboutWindow about = new AboutWindow();
			PreferencesWindow prefs = new PreferencesWindow();
			
			MainWindow window = new MainWindow(about, prefs);
			
			TrayMenu tray = new TrayMenu(window, about, prefs);
			tray.setVisible(true);
			
			window.setTrayMenu(tray);
			window.setVisible(!tray.isSupported());
			
		}
		catch (Throwable t)
		{
			t.printStackTrace();
		}
	}

}