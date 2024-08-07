package rotp.ui.vipconsole;

import java.awt.event.KeyEvent;
import java.util.List;

import rotp.ui.RotPUI;

public interface IVIPListener {
	int UNPROCESSED_ENTRY	= -1;
	int INVALID_ENTRY		= 0;
	int VALID_ENTRY			= 1;
	int VALID_ENTRY_NO_EXIT	= 2;
	int VALID_GAME_OVER		= 3;

	String getMessage();

	default String getEntryComments()			{ return ""; }
	default boolean exited()					{ return true; }	// Override when followed by reports
	default boolean handleKeyPress(KeyEvent e)	{ return false; }	// Override for non MapOverlay Selection
	default List<ConsoleOptions> getOptions()	{ return null; }	// Override for Console Selection!
	default void consoleEntry()					{ } // Override for Console Report!
	default String getMessageOption()			{
		List<ConsoleOptions> options = getOptions();
		if (options == null)
			return IVIPConsole.PRESS_ANY_KEY;
		String messageOption = "The options are:";
		for (ConsoleOptions option : options) 
			messageOption += IVIPConsole.NEWLINE + option.messageOption();
		
		return messageOption;
	}
	default int consoleEntry(String entry)		{
		List<ConsoleOptions> options = getOptions();
		if (options.isEmpty())
			return UNPROCESSED_ENTRY;
		for (ConsoleOptions option : options) {
			if (option.isValid(entry)) {
				if (handleKeyPress(option.getKeyEvent()))
					return VALID_ENTRY;
				else
					return UNPROCESSED_ENTRY;
			}
		}
		return INVALID_ENTRY;
	}
	default void initConsoleSelection(String title, boolean wait)	{
		if (!RotPUI.isVIPConsole)
    		return;
		VIPConsole.guiPromptMessages.newMenu(title, this, false, wait);
	};
	default void initConsoleReport(String title, boolean wait)	{
		if (!RotPUI.isVIPConsole)
    		return;
		VIPConsole.guiPromptMessages.newMenu(title, this, true, wait);
	};
	class ConsoleOptions {
		private final String key;
		private final String description;
		private final int	 keyCode;
		private final char	 keyChar;
		/**
		 * @param keyCode : the keyEvent sent as key pressed 
		 * @param key : the related player command
		 * @param description : What this action do!
		 */
		public ConsoleOptions(int keyCode, String key, String description)	{
			this.key		 = key;
			this.description = description;
			this.keyCode	 = keyCode;
			this.keyChar	 = key.charAt(0);
		}
		public ConsoleOptions(int keyCode, char keyChar, String key, String description)	{
			this.key		 = key;
			this.description = description;
			this.keyCode	 = keyCode;
			this.keyChar	 = keyChar;
		}
		public boolean	isValid(String str)	{ return key.equalsIgnoreCase(str); }
		public String	key()				{ return key; }
		public String	description()		{ return description; }
		public int		keyCode()			{ return keyCode; }
		public String	messageOption()		{ return key + " for " + description; }
		public KeyEvent	getKeyEvent()		{ return VIPConsole.getKeyEvent(keyCode, keyChar); }
	}
}
