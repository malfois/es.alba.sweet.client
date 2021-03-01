
package es.alba.sweet.client.scan.graph;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

public class ScanPart {

	private CTabFolder	folder;
	private Plot		plot;
	private TableViewer	tableViewer;

	@Inject
	public ScanPart() {

	}

	@PostConstruct
	public void postConstruct(Composite parent) {

		folder = new CTabFolder(parent, SWT.BOTTOM);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		folder.setLayoutData(data);
		CTabItem cTabItem1 = new CTabItem(folder, SWT.NONE);
		cTabItem1.setText("Plot");
		CTabItem cTabItem2 = new CTabItem(folder, SWT.NONE);
		cTabItem2.setText("Data");

		plot = new Plot(folder);
		cTabItem1.setControl(plot);

		tableViewer = new TableViewer(folder, SWT.READ_ONLY | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		cTabItem2.setControl(tableViewer.getTable());

		folder.setSelection(cTabItem1);
	}

	public void initialise(List<String> diagnostics) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				tableViewer = new TableViewer(folder, SWT.READ_ONLY | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
				tableViewer.setContentProvider(ArrayContentProvider.getInstance());
				createColumns(diagnostics);
				tableViewer.setInput(new ArrayList<>());
			}
		});

	}

	public void createColumns(List<String> diagnostics) {
		for (String diagnostic : diagnostics) {
			TableViewerColumn colPlot = new TableViewerColumn(tableViewer, SWT.NONE);
			colPlot.getColumn().setWidth(60);
			colPlot.getColumn().setText(diagnostic);
			colPlot.setLabelProvider(new ColumnLabelProvider() {
				@Override
				public String getText(Object element) {
					return "";
				}
			});
		}

	}

}