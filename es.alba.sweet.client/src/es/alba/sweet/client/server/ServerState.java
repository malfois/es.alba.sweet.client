package es.alba.sweet.client.server;

public enum ServerState {

	RUNNING_AND_CONNECTED("Server running and client connected", "ballgreen.png"),
	RUNNING_AND_NOT_CONNECTED("Server running and client NOT connected", "ballorange.png"),
	NOT_RUNNING("Server NOT running", "ballred.png");

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
