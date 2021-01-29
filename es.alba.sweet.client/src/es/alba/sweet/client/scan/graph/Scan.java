
package es.alba.sweet.client.scan.graph;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.dataprovider.Sample;
import org.eclipse.nebula.visualization.xygraph.figures.ToolbarArmedXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.nebula.visualization.xygraph.util.XYGraphMediaFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class Scan {
	@Inject
	public Scan() {

	}

	@PostConstruct
	public void postConstruct(Composite parent) {
		Canvas canvas = new Canvas(parent, SWT.NONE);
		canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		LightweightSystem lws = new LightweightSystem(canvas);
		XYGraph xyGraph = new XYGraph();
		ToolbarArmedXYGraph toolbarArmedXYGraph = new ToolbarArmedXYGraph(xyGraph);
		lws.setContents(toolbarArmedXYGraph);
		CircularBufferDataProvider traceDataProviderTraining = new CircularBufferDataProvider(false);
		traceDataProviderTraining.setBufferSize(100);
		Trace traceTraining = new Trace("Trace legenda", xyGraph.getPrimaryXAxis(), xyGraph.getPrimaryYAxis(), traceDataProviderTraining);
		xyGraph.addTrace(traceTraining);

		xyGraph.setTitle("Sigmoid function");
		xyGraph.getPrimaryYAxis().setScaleLineVisible(true);
		xyGraph.getPrimaryXAxis().setShowMajorGrid(true);
		xyGraph.getPrimaryYAxis().setShowMajorGrid(true);
		xyGraph.getPrimaryXAxis().setVisible(true);
		traceTraining.setPointStyle(PointStyle.BAR);
		traceTraining.setTraceColor(XYGraphMediaFactory.getInstance().getColor(XYGraphMediaFactory.COLOR_RED));
		xyGraph.getPrimaryXAxis().setTitle("X axis");
		xyGraph.getPrimaryYAxis().setTitle("Y axis");
		xyGraph.getPrimaryYAxis().setDashGridLine(true);

		// Plot our xy function
		for (int x = -20; x < 20; x++) {
			double y = 1.0 / (1.0 + Math.exp(-x));
			traceDataProviderTraining.addSample(new Sample(x, y));
			xyGraph.performAutoScale();
		}
	}
}