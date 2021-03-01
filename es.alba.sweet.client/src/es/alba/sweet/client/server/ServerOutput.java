package es.alba.sweet.client.server;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.annotation.PostConstruct;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import es.alba.sweet.client.core.IconLoader;

public class ServerOutput {

	private Label	state;
	private Label	image;

	@PostConstruct
	public void createGui(Composite parent) {

		Server.SERVER.addPropertyChangeListener(new ChangeServerStateListener());

		ServerState serverState = Server.SERVER.getServerState();

		Composite serverComposite = new Composite(parent, SWT.NONE);
		GridLayout compositeLayout = new GridLayout();
		compositeLayout.horizontalSpacing = 10;
		compositeLayout.numColumns = 3;
		serverComposite.setLayout(compositeLayout);

		Label label = new Label(serverComposite, SWT.NONE | SWT.BOTTOM);
		label.setText("Sweet Server");

		state = new Label(serverComposite, SWT.NONE | SWT.BOTTOM);
		state.setText(serverState.getDescription());

		image = new Label(serverComposite, SWT.NONE);
		image.setImage(IconLoader.load(serverState.getIconFileName()).createImage());

	}

	private class ChangeServerStateListener implements PropertyChangeListener {

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			ServerState serverState = (ServerState) event.getNewValue();
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					state.setText(serverState.getDescription());
					image.setImage(IconLoader.load(serverState.getIconFileName()).createImage());
				}
			});

		}

	}

}