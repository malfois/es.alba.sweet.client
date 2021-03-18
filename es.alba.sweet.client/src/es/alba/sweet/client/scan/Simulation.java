
package es.alba.sweet.client.scan;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import es.alba.sweet.base.communication.command.CommandName;
import es.alba.sweet.base.communication.command.CommandStream;
import es.alba.sweet.base.communication.command.ScanSimulationParameter;
import es.alba.sweet.base.output.Output;
import es.alba.sweet.client.core.DirectoryLocator;
import es.alba.sweet.client.core.constant.Directory;
import es.alba.sweet.client.server.Server;

public class Simulation {

	private ComboViewer		fileViewer;
	private List<String>	diagnostics	= new ArrayList<>();

	private Label			scan;

	private ComboViewer		combo;

	private Button			start;
	private Button			stop;

	@Inject
	public Simulation() {

	}

	@PostConstruct
	public void postConstruct(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(7, false);
		layout.horizontalSpacing = 10;
		composite.setLayout(layout);

		Label file = new Label(composite, SWT.NONE);
		file.setText("File names");

		fileViewer = new ComboViewer(composite, SWT.BORDER | SWT.READ_ONLY);

		fileViewer.setContentProvider(ArrayContentProvider.getInstance());
		fileViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Path) {
					Path path = (Path) element;
					return path.getFileName().toString();
				}
				return super.getText(element);
			}
		});
		fileViewer.setInput(new FileList().get());

		Label command = new Label(composite, SWT.NONE);
		command.setText("Scan command");

		scan = new Label(composite, SWT.NONE);
		scan.setText("");

		combo = new ComboViewer(composite, SWT.READ_ONLY);
		combo.setContentProvider(ArrayContentProvider.getInstance());
		combo.setInput(diagnostics);

		start = new Button(composite, SWT.PUSH);
		start.setText("Start");
		start.addSelectionListener(new ButtonStartListener());
		start.setEnabled(Scan.PROCESS.getScanState().isIdle());

		stop = new Button(composite, SWT.PUSH);
		stop.setText("Stop");
		stop.addSelectionListener(new ButtonStopListener());
		stop.setEnabled(Scan.PROCESS.getScanState().isRunning());

		fileViewer.addSelectionChangedListener(new FileChangeListener());
		fileViewer.setSelection(new StructuredSelection(fileViewer.getElementAt(0)), true);

		Scan.PROCESS.addPropertyChangeListener(new ChangeServerStateListener());
	}

	private class ButtonStartListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent e) {
			String filename = fileViewer.getStructuredSelection().getFirstElement().toString();
			String diagnostic = combo.getStructuredSelection().getFirstElement().toString();

			Output.MESSAGE.info("es.alba.sweet.client.scan.Simulation.ButtonListener.widgetSelected",
					"Sna simulation will be performed on " + filename + " with diagnostic " + diagnostic);
			ScanSimulationParameter parameter = new ScanSimulationParameter();
			parameter.setFilename(filename);
			parameter.setDiagnostics(diagnostic);
			Server.SERVER.getConnection().send(new CommandStream(CommandName.SCAN_SIMULATION, parameter.toJson()));

		}
	}

	private class ChangeServerStateListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			ScanState scanState = (ScanState) event.getNewValue();
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					start.setEnabled(scanState.isIdle());
					stop.setEnabled(scanState.isRunning());
				}
			});
		}

	}

	private class FileChangeListener implements ISelectionChangedListener {

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			Output.MESSAGE.info("es.alba.sweet.client.scan.Simulation.FileChangeListener.selectionChanged",
					"File selection is " + event.getStructuredSelection().getFirstElement().toString());

			try {
				Path path = (Path) event.getStructuredSelection().getFirstElement();
				List<String> lines = Files.lines(path).collect(Collectors.toList());
				String commandLine = lines.stream().filter(p -> p.startsWith("#S")).findFirst().orElse("");
				commandLine = commandLine.substring(2);
				scan.setText(commandLine);

				String diagnosticLine = lines.stream().filter(p -> p.startsWith("#L")).findFirst().orElse("");
				List<String> diags = List.of(diagnosticLine.substring(2).split(" ")).stream().filter(p -> p.trim().length() != 0).collect(Collectors.toList());
				diagnostics.clear();
				diagnostics.addAll(diags);

				combo.refresh();

				if (combo.getStructuredSelection().getFirstElement() == null) {
					Output.MESSAGE.info("es.alba.sweet.client.scan.Simulation.FileChangeListener.selectionChanged", "Setting the diagnostic selection to " + diagnostics.get(0));
					combo.setSelection(new StructuredSelection(diagnostics.get(0)));
				}

			} catch (IOException e1) {
				e1.printStackTrace();
			}

		}

	}

	private class FileList {

		private List<Path> paths = new ArrayList<>();

		public FileList() {
			Path folderPath = DirectoryLocator.findPath(Directory.TEST);

			try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(folderPath)) {
				for (Path path : directoryStream) {
					paths.add(path);
				}
			} catch (IOException ex) {
				System.err.println("Error reading files");
				ex.printStackTrace();
			}
		}

		public List<Path> get() {
			return this.paths;
		}
	}
}