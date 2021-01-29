package es.alba.sweet.client.parts.output;

import java.awt.TrayIcon.MessageType;

import org.eclipse.core.databinding.beans.IBeanValueProperty;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import es.alba.sweet.base.output.AMessage;
import es.alba.sweet.base.output.Output;

public class OutputTextViewer {

	private Color		black;
	private Color		orange;
	private Color		red;

	private StyledText	textViewer;
	private Output		output;

	public OutputTextViewer(Composite parent, Output output) {
		this.black = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
		this.orange = new Color(Display.getCurrent(), 255, 127, 0);
		this.red = new Color(Display.getCurrent(), 255, 0, 0);

		this.output = output;

		textViewer = new StyledText(parent, SWT.NONE | SWT.H_SCROLL | SWT.V_SCROLL);
		textViewer.setEditable(false);

		IBeanValueProperty<Output, AMessage> property = BeanProperties.value(Output.class, "currentMessage");
		IObservableValue<AMessage> observableCurrentMessage = property.observe(output);
		observableCurrentMessage.addChangeListener(new Change());
	}

	private class Change implements IChangeListener {

		@SuppressWarnings("unchecked")
		@Override
		public void handleChange(ChangeEvent event) {
			if (textViewer.isDisposed()) return;
			if (textViewer.getText().length() == 0) {
				output.getMessages().forEach(a -> add(a));
				return;
			}

			IObservableValue<AMessage> e = (IObservableValue<AMessage>) event.getObservable();
			add(e.getValue());
		}

	}

	public void add(AMessage message) {
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				while (textViewer.getText().length() + message.toString().length() > Output.MAX_CHARACTERS) {
					textViewer.replaceTextRange(0, textViewer.getLine(0).length() + 1, "");
				}

				StyleRange range = style(message);
				textViewer.append(message.toString());
				textViewer.setStyleRange(range);

				textViewer.setSelection(textViewer.getCharCount());
			}
		});
	}

	private StyleRange style(AMessage message) {
		int start = textViewer.getText().length();
		int length = message.toString().length();

		MessageType type = message.getType();
		switch (type) {
		case INFO:
			return new StyleRange(start, length, black, null);
		case WARNING:
			return new StyleRange(start, length, orange, null);
		case ERROR:
			return new StyleRange(start, length, red, null);
		default:
			return new StyleRange(start, length, black, null);
		}

	}
}
