package es.alba.sweet.client.server;

import javax.annotation.PostConstruct;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import es.alba.sweet.client.core.IconLoader;

public class ServerOutput {

	private Label state;
	private Label image;

	@PostConstruct
	public void createGui(Composite parent) {
		System.out.println(this.getClass());
		WritableValue<ServerState> observableServerState = Server.SERVER.getObservableServerState();

		Composite serverComposite = new Composite(parent, SWT.NONE);
		GridLayout compositeLayout = new GridLayout();
		compositeLayout.horizontalSpacing = 10;
		compositeLayout.numColumns = 3;
		serverComposite.setLayout(compositeLayout);

		Label label = new Label(serverComposite, SWT.NONE | SWT.BOTTOM);
		label.setText("Sweet Server");

		state = new Label(serverComposite, SWT.NONE | SWT.BOTTOM);
		state.setText(observableServerState.getValue().getDescription());

		image = new Label(serverComposite, SWT.NONE);
		image.setImage(IconLoader.load(observableServerState.getValue().getIconFileName()).createImage());
		observableServerState.addChangeListener(new ChangeServerStateListener());

	}

	private class ChangeServerStateListener implements IChangeListener {

		@SuppressWarnings("unchecked")
		@Override
		public void handleChange(ChangeEvent event) {
			WritableValue<ServerState> serverState = (WritableValue<ServerState>) event.getObservable();
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					state.setText(serverState.getValue().getDescription());
					image.setImage(IconLoader.load(serverState.getValue().getIconFileName()).createImage());
				}
			});

		}

	}

}