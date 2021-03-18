
package es.alba.sweet.client.scan;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import es.alba.sweet.base.communication.command.CommandName;
import es.alba.sweet.base.communication.command.CommandStream;
import es.alba.sweet.base.communication.command.FunctionSimulationArgument;
import es.alba.sweet.base.output.Output;
import es.alba.sweet.client.server.Server;

public class FunctionSimulation {

	private ComboViewer	functionViewer;
	private LabelText	xMin;
	private LabelText	xMax;
	private LabelText	position;
	private LabelText	fwhm;
	private LabelText	height;
	private LabelText	offset;

	private Button		start;
	private Button		stop;

	@Inject
	public FunctionSimulation() {

	}

	@PostConstruct
	public void postConstruct(Composite parent) {
		Composite composite = new Composite(parent, SWT.BORDER | SWT.READ_ONLY);
		GridLayout layout = new GridLayout(9, false);
		layout.horizontalSpacing = 10;
		composite.setLayout(layout);

		List<String> functions = new ArrayList<>();
		functions.add("Gaussian");
		functions.add("Error functions");

		functionViewer = new ComboViewer(composite, SWT.READ_ONLY);
		functionViewer.setContentProvider(ArrayContentProvider.getInstance());
		functionViewer.setInput(functions);
		functionViewer.setSelection(new StructuredSelection(functionViewer.getElementAt(0)), true);

		xMin = new LabelText(composite, "xMin", -2.0);
		xMax = new LabelText(composite, "xMax", 2.0);
		position = new LabelText(composite, "Position", 0.0);
		fwhm = new LabelText(composite, "fwhm", 0.5);
		height = new LabelText(composite, "Height", 1.0);
		offset = new LabelText(composite, "Offset", 0.0);

		start = new Button(composite, SWT.PUSH);
		start.setText("Start");
		start.addSelectionListener(new ButtonStartListener());
		start.setEnabled(Scan.PROCESS.getScanState().isIdle());

		stop = new Button(composite, SWT.PUSH);
		stop.setText("Stop");
		stop.addSelectionListener(new ButtonStopListener());
		stop.setEnabled(Scan.PROCESS.getScanState().isRunning());

		Scan.PROCESS.addPropertyChangeListener(new ChangeServerStateListener());

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

	private String parametersToText() {
		return this.xMin.toText() + ", " + this.xMax.toText() + ", " + this.position.toText() + ", " + this.fwhm.toText() + ", " + this.height.toText() + ", "
				+ this.offset.toText();
	}

	private class ButtonStartListener extends SelectionAdapter {
		public void widgetSelected(SelectionEvent e) {
			FunctionSimulationArgument argument = new FunctionSimulationArgument();
			argument.setFunction(functionViewer.getStructuredSelection().getFirstElement().toString());
			argument.setxMin(xMin.getValue());
			argument.setxMax(xMax.getValue());
			argument.setPosition(position.getValue());
			argument.setFwhm(fwhm.getValue());
			argument.setHeight(height.getValue());
			argument.setOffset(offset.getValue());

			Output.MESSAGE.info("es.alba.sweet.client.scan.Simulation.ButtonListener.widgetSelected",
					"Scan simulation will be performed on " + argument.getFunction() + " with parameters " + parametersToText());

			Server.SERVER.getConnection().send(new CommandStream(CommandName.SCAN_FUNCTION_SIMULATION, argument.toJson()));

		}
	}

	private class LabelText {

		private Label	label;
		private Text	text;

		public LabelText(Composite parent, String label, Double value) {

			Composite composite = new Composite(parent, SWT.NONE);
			GridLayout layout = new GridLayout(2, false);
			layout.horizontalSpacing = 5;
			composite.setLayout(layout);

			this.label = new Label(composite, SWT.NONE);
			this.label.setText(label);

			this.text = new Text(composite, SWT.BORDER | SWT.RIGHT);
			GridData gridData = new GridData();
			gridData.widthHint = 30;
			this.text.setLayoutData(gridData);
			this.text.setText(String.valueOf(value));

			this.text.addVerifyListener(new VerifyListener() {
				ControlDecoration decorator;
				{
					decorator = new ControlDecoration(text, SWT.LEFT | SWT.TOP);
					decorator.setDescriptionText("Not a number");
					Image image = FieldDecorationRegistry.getDefault().getFieldDecoration(FieldDecorationRegistry.DEC_ERROR).getImage();
					decorator.setImage(image);
					decorator.hide();
				}

				@Override
				public void verifyText(VerifyEvent e) {
					/* Notice how we combine the old and new below */
					String currentText = ((Text) e.widget).getText();
					String text = currentText.substring(0, e.start) + e.text + currentText.substring(e.end);
					try {
						Double.valueOf(text);
						decorator.hide();
					} catch (NumberFormatException ex) {
						decorator.show();
					}
				}
			});
		}

		public Double getValue() {
			String text = this.text.getText();
			return Double.valueOf(text);
		}

		public String toText() {
			return this.label.getText() + " = " + this.text.getText();
		}
	}
}