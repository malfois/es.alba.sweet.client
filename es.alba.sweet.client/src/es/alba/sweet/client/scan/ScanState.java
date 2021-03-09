package es.alba.sweet.client.scan;

public enum ScanState {

	RUNNING("Scan Running", "ballgreen.png"), IDLE("NO scan running", "ballgrey.png");

	private String	description;
	private String	iconFilename;

	private ScanState(String description, String name) {
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
