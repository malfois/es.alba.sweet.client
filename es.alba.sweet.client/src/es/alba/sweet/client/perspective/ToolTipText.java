package es.alba.sweet.client.perspective;

public class ToolTipText {

	private String	label;

	private String	comment;

	public ToolTipText(String label, String comment) {
		this.label = label;
		this.comment = comment;
	}

	public ToolTipText(String toolTipText) {
		String[] lines = toolTipText.split("\n");
		String[] label = lines[0].split(":");
		this.label = label[1].trim();
		this.comment = lines[1];
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public String getLabel() {
		return this.label;
	}

	@Override
	public String toString() {
		return "Label: " + label + "\n" + comment;
	}

}
