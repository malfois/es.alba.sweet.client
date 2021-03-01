package es.alba.sweet.client.scan.graph;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.dataprovider.Sample;
import org.eclipse.nebula.visualization.xygraph.figures.Axis;
import org.eclipse.nebula.visualization.xygraph.figures.ITraceListener;
import org.eclipse.nebula.visualization.xygraph.figures.ToolbarArmedXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.nebula.visualization.xygraph.util.XYGraphMediaFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;

public class Plot extends Canvas {

	private XYGraph	xyGraph	= new XYGraph();
	Trace			traceTraining;

	public Plot(Composite parent) {
		super(parent, SWT.NONE);
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		LightweightSystem lws = new LightweightSystem(this);
		ToolbarArmedXYGraph toolbarArmedXYGraph = new ToolbarArmedXYGraph(xyGraph);
		lws.setContents(toolbarArmedXYGraph);
		CircularBufferDataProvider traceDataProviderTraining = new CircularBufferDataProvider(false);
		traceDataProviderTraining.setBufferSize(100);
		traceTraining = new Trace("Trace legende", xyGraph.getPrimaryXAxis(), xyGraph.getPrimaryYAxis(), traceDataProviderTraining);
		xyGraph.addTrace(traceTraining);
		xyGraph.setShowLegend(true);

		xyGraph.getPrimaryXAxis().setTitle("X axis");
		xyGraph.getPrimaryYAxis().setTitle("Y Axis");

		xyGraph.getPrimaryYAxis().setScaleLineVisible(true);
		xyGraph.getPrimaryXAxis().setShowMajorGrid(true);
		xyGraph.getPrimaryYAxis().setShowMajorGrid(true);
		xyGraph.getPrimaryXAxis().setVisible(true);
		traceTraining.setPointStyle(PointStyle.DIAMOND);
		traceTraining.setTraceColor(XYGraphMediaFactory.getInstance().getColor(XYGraphMediaFactory.COLOR_RED));
		xyGraph.getPrimaryYAxis().setDashGridLine(true);

		traceTraining.addListener(new ITraceListener() {

			@Override
			public void traceYAxisChanged(Trace trace, Axis oldName, Axis newName) {
				// TODO Auto-generated method stub

			}

			@Override
			public void traceWidthChanged(Trace trace, int old, int newWidth) {
				// TODO Auto-generated method stub

			}

			@Override
			public void traceTypeChanged(Trace trace, TraceType old, TraceType newTraceType) {
				System.out.println("Trace type changed " + old + " " + newTraceType);

			}

			@Override
			public void traceNameChanged(Trace trace, String oldName, String newName) {
				// TODO Auto-generated method stub

			}

			@Override
			public void traceColorChanged(Trace trace, Color old, Color newColor) {
				System.out.println("Color changed " + old + " " + newColor);

			}

			@Override
			public void pointStyleChanged(Trace trace, PointStyle old, PointStyle newStyle) {
				System.out.println("Point Style changed " + old + " " + newStyle);

			}
		});

		// Plot our xy function
		for (int x = -20; x < 20; x++) {
			double y = 1.0 / (1.0 + Math.exp(-x));
			traceDataProviderTraining.addSample(new Sample(x, y));
			xyGraph.performAutoScale();
		}

	}

}
