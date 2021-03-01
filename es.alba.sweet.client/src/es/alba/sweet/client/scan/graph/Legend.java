package es.alba.sweet.client.scan.graph;

import es.alba.sweet.base.configuration.AModelObject;

public class Legend extends AModelObject {

	private boolean		xAxis		= false;
	private String		name;
	private Renderer	renderer	= new Renderer();
	private boolean		plotYAxis	= false;

	public Legend(String name, boolean xAxis, boolean plotYAxis, Renderer renderer) {
		this.xAxis = xAxis;
		this.plotYAxis = plotYAxis;
		this.name = name;
		this.renderer = renderer;
	}

	public Legend(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Renderer getRenderer() {
		return renderer;
	}

	public void setRenderer(Renderer renderer) {
		this.renderer = renderer;
	}

	public boolean isxAxis() {
		return xAxis;
	}

	public void setxAxis(boolean xAxis) {
		firePropertyChange("xAxis", this.xAxis, this.xAxis = xAxis);
	}

	public boolean isPlotYAxis() {
		return plotYAxis;
	}

	public void setPlotYAxis(boolean plotYAxis) {
		this.plotYAxis = plotYAxis;
	}

}
