package es.alba.sweet.client.scan;

public enum ScanState {

	RUNNING(true), IDLE(false);

	private String	description;
	private String	iconFilename;

	private boolean	running;

	private ScanState(boolean running) {
		this.running = running;
		if (running) {
			this.description = "Scan Running";
			this.iconFilename = "ballgreen.png";
			return;
		}
		this.description = "No scan Running";
		this.iconFilename = "ballgrey.png";
		return;
	}

	public String getDescription() {
		return description;
	}

	public String getIconFileName() {
		return this.iconFilename;
	}

	public boolean isRunning() {
		return running;
	}

	public boolean isIdle() {
		return !running;
	}

}
