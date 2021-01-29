
package es.alba.sweet.client.addons;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.osgi.service.event.Event;

public class ClosingApplication {

	@Optional
	public void closing(@EventTopic(UIEvents.UILifeCycle.APP_SHUTDOWN_STARTED) Event event) {
		System.out.println("Closing app");
	}

}
