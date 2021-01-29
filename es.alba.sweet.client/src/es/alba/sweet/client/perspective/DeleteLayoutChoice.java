package es.alba.sweet.client.perspective;

public class DeleteLayoutChoice {

	private boolean	buttonChoice;
	private boolean	diskChoice;

	public DeleteLayoutChoice(boolean buttonChoice, boolean diskChoice) {
		this.buttonChoice = buttonChoice;
		this.diskChoice = diskChoice;
	}

	public boolean isButtonChoice() {
		return buttonChoice;
	}

	public boolean isDiskChoice() {
		return diskChoice;
	}
}
