package es.alba.sweet.client.scan;

import org.eclipse.jface.viewers.ColumnLabelProvider;

import es.alba.sweet.client.scan.graph.Legend;

public class DiagnosticNameProvider extends ColumnLabelProvider {

	@Override
	public String getText(Object element) {
		Legend legend = (Legend) element;
		return legend.getName();
	}
}
