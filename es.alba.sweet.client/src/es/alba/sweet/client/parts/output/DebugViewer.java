
package es.alba.sweet.client.parts.output;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.widgets.Composite;

import es.alba.sweet.base.output.AMessage;
import es.alba.sweet.base.output.Output;
import es.alba.sweet.base.output.OutputName;

public class DebugViewer {

	private OutputTextViewer viewer;

	@Inject
	public DebugViewer() {

	}

	@PostConstruct
	public void postConstruct(Composite parent) {
		viewer = new OutputTextViewer(parent, Output.DEBUG);

		Output.DEBUG.info("es.alba.sweet.parts.DebugViewer.postConstruct", "DebugViewer constructed");

	}

	@Inject
	@Optional
	public void listUpdated(@UIEventTopic(OutputName.DEBUG) AMessage message, EPartService partService, MPart part) {

		if (partService.isPartVisible(part)) {
			System.out.println("es.alba.sweet.parts.output.class_name.listUpdated " + part.getElementId());
			this.viewer.add(message);
		}
	}
}