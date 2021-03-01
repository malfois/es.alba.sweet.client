
package es.alba.sweet.client.scan.graph;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import es.alba.sweet.base.configuration.Json;
import es.alba.sweet.base.output.Output;
import es.alba.sweet.client.scan.LegendTable;
import es.alba.sweet.client.scan.ScanConfiguration;
import es.alba.sweet.client.scan.XAxisLabelProvider;
import es.alba.sweet.client.scan.YAxisLabelProvider;

public class ScanLegend {

	private TableViewer			tableViewer;
	private List<Legend>		diagnostics			= new ArrayList<>();

	private XAxisLabelProvider	xAxisLabelProvider	= (XAxisLabelProvider) LegendTable.X_AXIS.getColumnLabelProvider();
	private YAxisLabelProvider	yAxisLabelProvider	= (YAxisLabelProvider) LegendTable.Y_AXIS.getColumnLabelProvider();
	private DataBindingContext	dataBindingContext	= new DataBindingContext();

	@Inject
	ScanConfiguration			configuration;

	@PostConstruct
	public void postConstruct(Composite parent) {
		diagnostics = configuration.getLegends();

		Json<ScanConfiguration> jscan = new Json<>(configuration);
		jscan.print();

		tableViewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		createColumns();

		tableViewer.setContentProvider(ArrayContentProvider.getInstance());

		// make lines and header visible
		final Table table = tableViewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		tableViewer.setInput(diagnostics); // returns a list of Record-Objects

	}

	public void initialise(List<Legend> diagnostics) {
		this.diagnostics.clear();

		this.diagnostics = diagnostics;

		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				// createTableEditor();
				// tableViewer.getTable().removeAll();
				// tableViewer.refresh();
				clearLabelProvider();
				tableViewer.setInput(diagnostics);
				createTableEditor();
				// for (Legend legend : diagnostics) {
				// tableViewer.setChecked(legend, legend.isxAxis());
				// tableViewer.setChecked(legend, legend.isPlotYAxis());
				// }
			}
		});

	}

	private void createColumns() {
		LegendTable[] legends = LegendTable.values();
		for (LegendTable legend : legends) {
			TableViewerColumn colName = new TableViewerColumn(tableViewer, SWT.NONE);
			colName.getColumn().setWidth(legend.getWidth());
			colName.getColumn().setText(legend.getName());
			colName.setLabelProvider(legend.getColumnLabelProvider());

		}
	}

	private void clearLabelProvider() {
		this.dataBindingContext.dispose();

		this.xAxisLabelProvider.getButtons().values().forEach(a -> a.dispose());
		this.xAxisLabelProvider.getButtons().clear();

		this.yAxisLabelProvider.getButtons().values().forEach(a -> a.dispose());
		this.yAxisLabelProvider.getButtons().clear();

		// This will dispose of all the control button that were created previously
		if (tableViewer.getTable() != null && tableViewer.getTable().getChildren() != null) {
			for (Control item : tableViewer.getTable().getChildren()) {
				// at this point there are no other controls embedded in the viewer, however different instances may require more checking of the controls here.
				if ((item != null) && (!item.isDisposed())) {
					item.dispose();
				}
			}
		}

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void createTableEditor() {
		Table table = tableViewer.getTable();
		for (int i = 0; i < diagnostics.size(); i++) {
			Legend legend = diagnostics.get(i);
			TableItem item = table.getItem(i);
			TableEditor editor = new TableEditor(table);
			editor.grabHorizontal = true;
			Button xAxis = new Button(table, SWT.CHECK);
			xAxis.setSelection(legend.isxAxis());
			xAxis.addSelectionListener(new PlotXAxisAdapter(legend));
			xAxis.pack();

			ISWTObservableValue xAxisEnabledObservable = WidgetProperties.enabled().observe(xAxis);
			ISWTObservableValue xAxisButtonObservable = WidgetProperties.buttonSelection().observe(xAxis);

			IObservableValue<Object> observableXAxis = BeanProperties.value(Legend.class, "xAxis").observe(legend);

			IObservableValue<Boolean> isDisabled = ComputedValue.create(() -> {
				return !(Boolean) observableXAxis.getValue();
			});

			dataBindingContext.bindValue(xAxisEnabledObservable, isDisabled);
			dataBindingContext.bindValue(xAxisButtonObservable, observableXAxis);

			editor.setEditor(xAxis, item, 1);
			item.setData("xAxisEditor", editor);

			editor = new TableEditor(table);
			editor.grabHorizontal = true;
			Button yAxis = new Button(table, SWT.CHECK);
			yAxis.setSelection(diagnostics.get(i).isPlotYAxis());
			yAxis.addSelectionListener(new PlotYAxisAdapter(diagnostics.get(i)));
			yAxis.pack();
			editor.setEditor(yAxis, item, 2);
			item.setData("xAxisEditor", editor);

		}
	}

	private class PlotYAxisAdapter extends SelectionAdapter {

		private Legend legend;

		public PlotYAxisAdapter(Legend legend) {
			this.legend = legend;
		}

		// Selecting a name in the list
		public void widgetSelected(SelectionEvent e) {
			Button button = (Button) e.getSource();
			boolean selected = button.getSelection();
			legend.setPlotYAxis(selected);
			String text = (selected) ? legend.getName() + " will be added to the plot" : legend.getName() + " will be removed from the plot";
			Output.MESSAGE.info("es.alba.sweet.client.scan.graph.ScanLegend.PlotYAxisAdapter.widgetSelected", text);
		}
	}

	private class PlotXAxisAdapter extends SelectionAdapter {

		private Legend legend;

		public PlotXAxisAdapter(Legend legend) {
			this.legend = legend;
		}

		// Selecting a name in the list
		public void widgetSelected(SelectionEvent e) {
			Button button = (Button) e.getSource();
			boolean selected = button.getSelection();
			diagnostics.forEach(a -> a.setxAxis(false));
			legend.setxAxis(selected);
			Output.MESSAGE.info("es.alba.sweet.client.scan.graph.ScanLegend.PlotXAxisAdapter.widgetSelected", "new X-Axis is now " + legend.getName());
		}
	}

}