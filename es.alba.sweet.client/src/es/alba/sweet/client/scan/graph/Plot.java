package es.alba.sweet.client.scan.graph;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.nebula.visualization.xygraph.dataprovider.CircularBufferDataProvider;
import org.eclipse.nebula.visualization.xygraph.dataprovider.Sample;
import org.eclipse.nebula.visualization.xygraph.figures.ToolbarArmedXYGraph;
import org.eclipse.nebula.visualization.xygraph.figures.Trace;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.PointStyle;
import org.eclipse.nebula.visualization.xygraph.figures.Trace.TraceType;
import org.eclipse.nebula.visualization.xygraph.figures.XYGraph;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import es.alba.sweet.base.output.Output;
import es.alba.sweet.base.scan.DataPoint;
import es.alba.sweet.base.scan.XyData;

public class Plot extends Canvas {

	private XYGraph xyGraph = new XYGraph();

	public Plot(Composite parent) {
		super(parent, SWT.NONE);
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		LightweightSystem lws = new LightweightSystem(this);
		ToolbarArmedXYGraph toolbarArmedXYGraph = new ToolbarArmedXYGraph(xyGraph);
		lws.setContents(toolbarArmedXYGraph);
		xyGraph.setShowLegend(true);

		xyGraph.getPrimaryXAxis().setTitle("X axis");
		xyGraph.getPrimaryYAxis().setTitle("Y Axis");

		xyGraph.getPrimaryYAxis().setScaleLineVisible(true);
		xyGraph.getPrimaryXAxis().setShowMajorGrid(true);
		xyGraph.getPrimaryYAxis().setShowMajorGrid(true);
		xyGraph.getPrimaryXAxis().setVisible(true);
		xyGraph.getPrimaryYAxis().setDashGridLine(true);

	}

	public void initialise(List<String> diagnostics) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				removeTraces();

				Trace[] traces = new Trace[diagnostics.size()];
				CircularBufferDataProvider[] traceDataProviders = new CircularBufferDataProvider[diagnostics.size()];
				int numberOfGraphs = diagnostics.size();
				for (int i = 0; i < numberOfGraphs; i++) {
					String diagnostic = diagnostics.get(i);
					traceDataProviders[i] = new CircularBufferDataProvider(false);
					traceDataProviders[i].setBufferSize(100);
					traces[i] = new Trace(diagnostic, xyGraph.getPrimaryXAxis(), xyGraph.getPrimaryYAxis(), traceDataProviders[i]);
					if (diagnostic.contains(" Fit")) {
						traces[i].setPointStyle(PointStyle.NONE);
						traces[i].setTraceType(TraceType.SOLID_LINE);
					} else {
						traces[i].setPointStyle(PointStyle.FILLED_CIRCLE);
						traces[i].setTraceType(TraceType.DASH_LINE);
						traces[i].setPointSize(7);
					}
					xyGraph.addTrace(traces[i]);
				}
			}
		});
	}

	private void removeTraces() {
		List<Trace> traces = xyGraph.getPlotArea().getTraceList();
		if (traces.isEmpty()) return;

		for (int i = traces.size() - 1; i >= 0; i--) {
			Output.MESSAGE.warning("es.alba.sweet.client.scan.graph.Plot.removeTraces", traces.get(i).getName());
			xyGraph.removeTrace(traces.get(i));
			Output.MESSAGE.warning("es.alba.sweet.client.scan.graph.Plot.removeTraces", traces.toString());
		}
	}

	public void addDataset(Map<String, XyData> data, Map<String, XyData> fit) {
		plotLastPoint(data);
		plot(fit);
	}

	public void plot(Map<String, XyData> data) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				Set<String> keys = data.keySet();
				List<Trace> traces = xyGraph.getPlotArea().getTraceList().stream().filter(p -> keys.contains(p.getName())).collect(Collectors.toList());
				for (Trace trace : traces) {
					CircularBufferDataProvider dataProvider = (CircularBufferDataProvider) trace.getDataProvider();
					dataProvider.clearTrace();
					XyData xyData = data.get(trace.getName());
					int nPoints = xyData.getX().size();
					for (int i = 0; i < nPoints; i++) {
						Double x = xyData.getX().get(i);
						Double y = xyData.getY().get(i);
						dataProvider.addSample(new Sample(x, y));

					}
				}
				xyGraph.performAutoScale();
			}
		});

	}

	private void plotLastPoint(Map<String, XyData> data) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				Set<String> keys = data.keySet();
				List<Trace> traces = xyGraph.getPlotArea().getTraceList().stream().filter(p -> keys.contains(p.getName())).collect(Collectors.toList());
				for (Trace trace : traces) {
					DataPoint point = data.get(trace.getName()).getLastDataPoint();
					if (point != null) {
						CircularBufferDataProvider dataProvider = (CircularBufferDataProvider) trace.getDataProvider();
						dataProvider.addSample(new Sample(point.getX(), point.getY()));
					}
				}
				xyGraph.performAutoScale();
			}
		});
	}
}
