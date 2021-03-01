package es.alba.sweet.client.core.constant;

import java.util.List;

public class Id {

	public final static String			SWEET						= "es.alba.sweet.client";
	public final static String			WINDOW						= String.join(".", SWEET, "window", "main");
	public final static String			PERSPECTIVE_STACK			= String.join(".", SWEET, "perspectivestack");
	public final static String			PARTSTACK					= String.join(".", SWEET, "partstack");
	public final static String			PERSPECTIVE					= String.join(".", SWEET, Word.PERSPECTIVE);

	public final static String			OUTPUT						= "output";
	public final static String			MESSAGE						= String.join(".", SWEET, Word.PART_DESCRIPTOR, OUTPUT, "message");
	public final static String			DEBUG						= String.join(".", SWEET, Word.PART_DESCRIPTOR, OUTPUT, "debug");
	public final static List<String>	OUTPUT_PART_IDS				= List.of(Id.MESSAGE, Id.DEBUG);

	public final static String			MAIN_TRIM_BAR				= String.join(".", SWEET, Word.TRIM_BAR, "top");
	public final static String			PERSPECTIVE_TOOL_CONTROL	= String.join(".", SWEET, Word.TOOL_CONTROL, Word.PERSPECTIVE);
	public final static String			SERVER_TOOL_CONTROL			= String.join(".", SWEET, Word.TOOL_CONTROL, Word.SERVER);

	public final static String			SCAN_PERSPECTIVE			= String.join(".", SWEET, Word.PERSPECTIVE, "scan");

	public final static String			SCAN_PLOT					= String.join(".", SWEET, Word.PART, "scan", "graph");
	public final static String			SCAN_LEGEND					= String.join(".", SWEET, Word.PART, "scan", "legend");

	private class Word {
		private final static String	PART_DESCRIPTOR	= "partdescriptor";
		private final static String	TRIM_BAR		= "trimbar";
		private final static String	PERSPECTIVE		= "perspective";
		private final static String	TOOL_CONTROL	= "toolcontrol";
		private final static String	SERVER			= "server";
		private final static String	PART			= "part";

	}
}
