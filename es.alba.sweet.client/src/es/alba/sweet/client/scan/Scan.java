package es.alba.sweet.client.scan;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;

import es.alba.sweet.base.output.Output;
import es.alba.sweet.base.scan.Header;
import es.alba.sweet.base.scan.ScanDataSet;
import es.alba.sweet.client.EclipseUI;
import es.alba.sweet.client.core.constant.Id;
import es.alba.sweet.client.scan.graph.Edge;
import es.alba.sweet.client.scan.graph.Legend;
import es.alba.sweet.client.scan.graph.ScanLegend;
import es.alba.sweet.client.scan.graph.ScanPart;

public enum Scan {

	PROCESS;

	private ScanState scanState = ScanState.IDLE;

	public void initialise(Header scanHeader) {
		setScanState(ScanState.RUNNING);
		Output.MESSAGE.info("es.alba.sweet.client.scan.Scan.initialise", scanState.name());
		ScanConfiguration scanConfiguration = EclipseUI.getEclipseContext().get(ScanConfiguration.class);

		List<Legend> legends = scanConfiguration.getLegends();
		List<String> diagnostics = scanHeader.getDiagnostics();

		List<Legend> newLegends = new ArrayList<>();

		int nDiagnostics = diagnostics.size();
		int nRenderers = legends.size();
		for (int i = 0; i < nDiagnostics; i++) {
			if (i >= nRenderers) newLegends.add(new Legend(diagnostics.get(i)));
			else newLegends.add(new Legend(diagnostics.get(i), legends.get(i).isxAxis(), legends.get(i).isPlotYAxis(), legends.get(i).getRenderer()));
		}

		newLegends.forEach(a -> a.setPlotYAxis(false));
		List<Legend> yAxisLegend = newLegends.stream().filter(p -> p.getName().contains(scanHeader.getSelectedDiagnostic())).collect(Collectors.toList());
		yAxisLegend.forEach(a -> a.setPlotYAxis(true));

		newLegends.forEach(a -> a.setxAxis(false));
		Legend legendX = newLegends.stream().filter(p -> p.getName().equals(scanHeader.getMotor())).findFirst().orElse(newLegends.get(0));
		legendX.setxAxis(true);

		MPerspective activePerspective = EclipseUI.activePerspective();

		MPart legendPart = (MPart) EclipseUI.modelService().find(Id.SCAN_LEGEND, activePerspective);
		if (legendPart != null) {
			ScanLegend scanLegendPart = (ScanLegend) legendPart.getObject();
			scanLegendPart.initialise(newLegends);
		}

		List<String> plotDiagnsotics = scanHeader.getPlotDiagnostics();
		nDiagnostics = plotDiagnsotics.size();
		for (int i = 0; i < 2 * nDiagnostics; i += 2) {
			plotDiagnsotics.add((i + 1), plotDiagnsotics.get(i) + " Fit");
		}

		MPart part = (MPart) EclipseUI.modelService().find(Id.SCAN_PLOT, activePerspective);
		if (part != null) {
			Path file = Paths.get(scanHeader.getFilename()).getFileName();
			part.setLabel(file.toString());
			ScanPart scanPart = (ScanPart) part.getObject();
			scanPart.initialise(scanHeader.getDiagnostics());
			scanPart.getPlot().initialise(plotDiagnsotics);
		}

		MPart edgePart = (MPart) EclipseUI.modelService().find(Id.SCAN_EDGE, activePerspective);
		if (edgePart != null) {
			Edge edge = (Edge) edgePart.getObject();
			edge.getPlot().initialise(plotDiagnsotics);
		}

		scanConfiguration.setHeader(scanHeader);
		scanConfiguration.setLegends(newLegends);

	}

	public void addDataPoint(ScanDataSet dataset) {
		MPerspective activePerspective = EclipseUI.activePerspective();
		MPart part = (MPart) EclipseUI.modelService().find(Id.SCAN_PLOT, activePerspective);
		if (part != null) {
			ScanPart scanPart = (ScanPart) part.getObject();
			scanPart.setDataset(dataset.getScanXyData(), dataset.getScanFitXyData());
		}

		MPart edgePart = (MPart) EclipseUI.modelService().find(Id.SCAN_EDGE, activePerspective);
		if (edgePart != null) {
			Edge edge = (Edge) edgePart.getObject();
			edge.getPlot().plot(dataset.getDerivativeData());
			edge.getPlot().plot(dataset.getFitDerivativeData());
		}
	}

	public ScanState getScanState() {
		return this.scanState;
	}

	public void setScanState(ScanState scanState) {
		firePropertyChange("scanstate", this.scanState, this.scanState = scanState);
	}

	private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		changeSupport.removePropertyChangeListener(listener);
	}

	protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
		changeSupport.firePropertyChange(propertyName, oldValue, newValue);
	}
}
