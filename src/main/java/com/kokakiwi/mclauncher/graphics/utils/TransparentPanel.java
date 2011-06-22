package com.kokakiwi.mclauncher.graphics.utils;

import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.JPanel;

public class TransparentPanel extends JPanel {
	private static final long serialVersionUID = 3818161668902701298L;
	
	private Insets insets;
	
	public TransparentPanel()
	{
		
	}
	
	public TransparentPanel(LayoutManager layout)
	{
		setLayout(layout);
	}

	@Override
	public boolean isOpaque() {
		return false;
	}
	
	public void setInsets(int a, int b, int c, int d) {
		this.insets = new Insets(a, b, c, d);
	}

	public Insets getInsets() {
		if (this.insets == null)
			return super.getInsets();
		return this.insets;
	}

}
