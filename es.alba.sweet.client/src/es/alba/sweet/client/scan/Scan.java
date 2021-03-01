package es.alba.sweet.client.scan;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;

import es.alba.sweet.base.scan.Header;
import es.alba.sweet.client.EclipseUI;
import es.alba.sweet.client.core.constant.Id;
import es.alba.sweet.client.scan.graph.Legend;
import es.alba.sweet.client.scan.graph.ScanLegend;
import es.alba.sweet.client.scan.graph.ScanPart;

public enum Scan {

	PROCESS;

	@Inject
	public void initialise(Header scanHeader) {

		MPerspective activePerspective = EclipseUI.activePerspective();
		MPart part = (MPart) EclipseUI.modelService().find(Id.SCAN_PLOT, activePerspective);

		Path file = Paths.get(scanHeader.getFilename()).getFileName();
		part.setLabel(file.toString());
		ScanPart scanPart = (ScanPart) part.getObject();

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
		Legend legendX = newLegends.stream().filter(p -> p.getName().equals(scanHeader.getMotor())).findFirst().orElse(newLegends.get(1));
		legendX.setxAxis(true);

		MPart legendPart = (MPart) EclipseUI.modelService().find(Id.SCAN_LEGEND, activePerspective);
		ScanLegend scanLegendPart = (ScanLegend) legendPart.getObject();
		scanLegendPart.initialise(newLegends);

		scanPart.initialise(scanHeader.getDiagnostics());

		scanConfiguration.setHeader(scanHeader);
		scanConfiguration.setLegends(newLegends);

		// Json<ScanConfiguration> conf = new Json<>(scanConfiguration);
		// conf.print();
	}
}
