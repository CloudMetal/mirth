package com.webreach.mirth.server.tools;

import java.util.HashMap;
import java.util.Map;

/**
 * Object representing one token in the command shell language. A token is
 * either a keyword like "channel" or "remove" (statically defined, hardcoded),
 * or a generic string or int (created dynamically when seen in the input).
 */
public class Token {
	private static Map<String, Token> keywordMap = new HashMap<String, Token>();

	private static Token addKeyword(String text) {
		Token token = new Token(text);
		keywordMap.put(text, token);
		return token;
	}

	static Token getKeyword(String text) {
		return keywordMap.get(text.toLowerCase());
	}

	// Keyword tokens
	static Token HELP = addKeyword("help");
	static Token USER = addKeyword("user");
	static Token LIST = addKeyword("list");
	static Token ADD = addKeyword("add");
	static Token REMOVE = addKeyword("remove");
	static Token CHANGEPW = addKeyword("changepw");
	static Token START = addKeyword("start");
	static Token STOP = addKeyword("stop");
	static Token PAUSE = addKeyword("pause");
	static Token RESUME = addKeyword("resume");
	static Token DEPLOY = addKeyword("deploy");
	static Token EXPORTCFG = addKeyword("exportcfg");
	static Token IMPORTCFG = addKeyword("importcfg");
	static Token IMPORT = addKeyword("import");
	static Token IMPORTALERTS = addKeyword("importalerts");
    static Token EXPORTALERTS = addKeyword("exportalerts");
	static Token IMPORTSCRIPT = addKeyword("importscript");
	static Token EXPORTSCRIPT = addKeyword("exportscript");
    static Token IMPORTCODETEMPLATES = addKeyword("importcodetemplates");
    static Token EXPORTCODETEMPLATES = addKeyword("exportcodetemplates");
	static Token FORCE = addKeyword("force");
	static Token STATUS = addKeyword("status");
	static Token EXPORT = addKeyword("export");
	static Token CHANNEL = addKeyword("channel");
	static Token RENAME = addKeyword("rename");
	static Token ENABLE = addKeyword("enable");
	static Token DISABLE = addKeyword("disable");
	static Token STATS = addKeyword("stats");
	static Token CLEAR = addKeyword("clear");
	static Token RESETSTATS = addKeyword("resetstats");
	static Token DUMP = addKeyword("dump");
	static Token EVENTS = addKeyword("events");
	static Token SHUTDOWN = addKeyword("shutdown");
	static Token QUIT = addKeyword("quit");
	

	static Token WILDCARD = addKeyword("*");

	static Token intToken(String value) {
		return new IntToken(value);
	}

	static Token stringToken(String value) {
		return new StringToken(value);
	}

	private String text;

	Token(String text) {
		this.text = text;
	}

	public String toString() {
		return "<" + text + ">";
	}

	String getText() {
		return text;
	}
}