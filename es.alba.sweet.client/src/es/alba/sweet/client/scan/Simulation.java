
package es.alba.sweet.client.scan;

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
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import es.alba.sweet.base.communication.command.CommandName;
import es.alba.sweet.base.communication.command.CommandStream;
import es.alba.sweet.base.communication.command.ScanSimulationParameter;
import es.alba.sweet.base.output.Output;
import es.alba.sweet.client.core.DirectoryLocator;
import es.alba.sweet.client.core.constant.Directory;
import es.alba.sweet.client.server.Server;

public class Simulation {

	private ListViewer		listViewer;
	private List<String>	diagnostics	= new ArrayList<>();

	private Label			scan;

	private ComboViewer		combo;

	@Inject
	public Simulation() {

	}

	@PostConstruct
	public void postConstruct(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));

		Composite fileComposite = new Composite(composite, SWT.NONE);
		fileComposite.setLayout(new GridLayout(1, false));

		Label file = new Label(fileComposite, SWT.NONE);
		file.setText("File names");

		listViewer = new ListViewer(fileComposite, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);

		listViewer.setContentProvider(ArrayContentProvider.getInstance());
		listViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Path) {
					Path path = (Path) element;
					return path.getFileName().toString();
				}
				return super.getText(element);
			}
		});
		listViewer.setInput(new FileList().get());

		Composite commandComposite = new Composite(composite, SWT.NONE);
		commandComposite.setLayout(new GridLayout(10, false));

		Label command = new Label(commandComposite, SWT.NONE);
		command.setText("Scan command");

		scan = new Label(commandComposite, SWT.NONE);
		scan.setText("");

		combo = new ComboViewer(commandComposite, SWT.READ_ONLY);
		combo.setContentProvider(ArrayContentProvider.getInstance());
		combo.setInput(diagnostics);

		Button button = new Button(composite, SWT.PUSH);
		button.setText("Load");
		button.addSelectionListener(new ButtonListener());

		listViewer.addSelectionChangedListener(new FileChangeListener());
		listViewer.setSelection(new StructuredSelection(listViewer.getElementAt(0)), true);

	}

	private class ButtonListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent e) {
			String filename = listViewer.getStructuredSelection().getFirstElement().toString();
			String diagnostic = combo.getStructuredSelection().getFirstElement().toString();

			Output.MESSAGE.info("es.alba.sweet.client.scan.Simulation.ButtonListener.widgetSelected",
					"Sna simulation will be performed on " + filename + " with diagnostic " + diagnostic);
			ScanSimulationParameter parameter = new ScanSimulationParameter();
			parameter.setFilename(filename);
			parameter.setDiagnostics(diagnostic);
			Server.SERVER.getConnection().send(new CommandStream(CommandName.SCAN_SIMULATION, parameter.toJson()));

		}
	}

	private class FileChangeListener implements ISelectionChangedListener {

		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			Output.MESSAGE.info("es.alba.sweet.client.scan.Simulation.FileChangeListener.selectionChanged",
					"File selection is " + event.getStructuredSelection().getFirstElement().toString());

			try {
				Path path = (Path) listViewer.getStructuredSelection().getFirstElement();
				List<String> lines = Files.lines(path).collect(Collectors.toList());
				String commandLine = lines.stream().filter(p -> p.startsWith("#S")).findFirst().orElse("");
				commandLine = commandLine.substring(2);
				scan.setText(commandLine);

				String diagnosticLine = lines.stream().filter(p -> p.startsWith("#L")).findFirst().orElse("");
				List<String> diags = List.of(diagnosticLine.substring(2).split(" ")).stream().filter(p -> p.trim().length() != 0).collect(Collectors.toList());
				diagnostics.clear();
				diagnostics.addAll(diags);

				combo.refresh();

				System.out.println(combo.getStructuredSelection().getFirstElement() + " " + diagnostics.size());
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