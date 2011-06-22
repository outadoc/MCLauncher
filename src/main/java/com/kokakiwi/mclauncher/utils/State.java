package com.kokakiwi.mclauncher.utils;

public enum State {
	INIT(1, "Initializing loader"),
	DETERMINING_PACKAGE(2, "Determining packages to load"),
	CHECKING_CACHE(3, "Checking cache for existing files"),
	DOWNLOADING(4, "Downloading packages"),
	EXTRACTING_PACKAGES(5, "Extracting downloaded packages"),
	UPDATING_CLASSPATH(6, "Updating classpath"),
	SWITCHING_APPLET(7, "Switching applet"),
	INITIALIZE_REAL_APPLET(8, "Initializing real applet"),
	START_REAL_APPLET(9 ,"Starting real applet"),
	DONE(10, "Done loading");
	
	private int opcode;
	private String description;
	
	State()
	{
		this(State.values().length + 1);
	}
	
	State(String description)
	{
		this(State.values().length + 1, description);
	}
	
	State(int opcode)
	{
		this(opcode, null);
	}
	
	State(int opcode, String description)
	{
		this.opcode = opcode;
		this.description = description;
	}
	
	public int getOpCode()
	{
		return this.opcode;
	}
	
	public String getDescription()
	{
		return this.description;
	}
}
