package es.alba.sweet.client.scan;

import org.eclipse.jface.viewers.ColumnLabelProvider;

public enum LegendTable {

	NAME("Name", 200, new DiagnosticNameProvider()), X_AXIS("X-Axis", 50, new XAxisLabelProvider()), Y_AXIS("Y-Axis", 50, new YAxisLabelProvider());

	private String				name;
	private int					width;
	private ColumnLabelProvider	columnLabelProvider;

	private LegendTable(String name, int width, ColumnLabelProvider columnLabelProvider) {
		this.name = name;
		this.width = width;
		this.columnLabelProvider = columnLabelProvider;
	}

	public String getName() {
		return name;
	}

	public int getWidth() {
		return width;
	}

	public ColumnLabelProvider getColumnLabelProvider() {
		return columnLabelProvider;
	}

}
