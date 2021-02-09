package es.alba.sweet.client;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.e4.ui.workbench.lifecycle.PreSave;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessAdditions;
import org.eclipse.e4.ui.workbench.lifecycle.ProcessRemovals;

import es.alba.sweet.base.configuration.Json;
import es.alba.sweet.base.output.Output;
import es.alba.sweet.client.perspective.Configuration;

/**
 * This is a stub implementation containing e4 LifeCycle annotated methods.<br />
 * There is a corresponding entry in <em>plugin.xml</em> (under the <em>org.eclipse.core.runtime.products' extension point</em>) that references this class.
 **/
public class E4LifeCycle {

	@PostContextCreate
	void postContextCreate(IEclipseContext workbenchContext) {
		Output.DEBUG.info("es.alba.sweet.E4LifeCycle.postContextCreate", "OK");
	}

	@SuppressWarnings("unchecked")
	@PreSave
	void preSave(IEclipseContext workbenchContext) {
		System.out.println(this.getClass() + " presave");

		Json<Configuration> jsonConfiguration = workbenchContext.get(Json.class);
		jsonConfiguration.print();
	}

	@ProcessAdditions
	void processAdditions(IEclipseContext workbenchContext) {
		Output.DEBUG.info("es.alba.sweet.E4LifeCycle.processAdditions", "Injecting " + Json.class.getSimpleName() + " in context " + workbenchContext);

		EclipseUI.start(workbenchContext);

		Json<Configuration> jsonConfiguration = new Json<>(new Configuration());
		jsonConfiguration.read();
		workbenchContext.set(Json.class, jsonConfiguration);
		Output.DEBUG.info("es.alba.sweet.E4LifeCycle.processAdditions", Json.class.getSimpleName() + " injected in context " + workbenchContext);

	}

	@ProcessRemovals
	void processRemovals(IEclipseContext workbenchContext) {
	}
}
