package es.alba.sweet.client.server;

public enum ServerState {

	RUNNING_AND_CONNECTED("Running and client connected", "ballgreen.png"),
	RUNNING_AND_NOT_CONNECTED("Running and client NOT connected", "ballorange.png"),
	NOT_RUNNING("NOT running", "ballred.png");

	private String	description;
	private String	iconFilename;

	private ServerState(String description, String name) {
		this.description = description;
		this.iconFilename = name;
	}

	public String getDescription() {
		return description;
	}

	public String getIconFileName() {
		return this.iconFilename;
	}
}
