
package es.alba.sweet.client.scan.graph;

import javax.annotation.PostConstruct;

import org.eclipse.swt.widgets.Composite;

public class Edge {

	private Plot plot;

	@PostConstruct
	public void postConstruct(Composite parent) {
		plot = new Plot(parent);
	}

	public Plot getPlot() {
		return plot;
	}

}