package es.alba.sweet.client.handlers;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import es.alba.sweet.base.output.Output;
import es.alba.sweet.client.perspective.PerspectiveControl;

public class MovingPartListener implements EventHandler {

	private PerspectiveControl perspectiveControl;

	public MovingPartListener(MToolControl PerspectiveControl) {
		this.perspectiveControl = (PerspectiveControl) PerspectiveControl.getObject();
	}

	@Override
	public void handleEvent(Event event) {
		Object element = event.getProperty(EventTags.ELEMENT);
		if (!(element instanceof MPartStack)) {
			return;
		}

		if (UIEvents.isADD(event)) {
			// check new value, because we check for addition and old value will be null
			Object newValue = event.getProperty(EventTags.NEW_VALUE);
			if (newValue instanceof MPart) {
				MPart part = (MPart) newValue;
				Output.DEBUG.info("es.alba.sweet.handlers.MovingPartListener.handleEvent", "Added " + part.getLabel() + " at position: " + event.getProperty(EventTags.POSITION));
			}
		} else if (UIEvents.isREMOVE(event)) {
			// check old value, because we check for remove and new value will be null
			Object oldValue = event.getProperty(EventTags.OLD_VALUE);
			if (oldValue instanceof MPart) {
				MPart part = (MPart) oldValue;
				Output.DEBUG.info("es.alba.sweet.handlers.MovingPartListener.handleEvent",
						"Removed " + part.getLabel() + " from position: " + event.getProperty(EventTags.POSITION));
			}
		}
		perspectiveControl.setDecoratorDirty();
	}

}
