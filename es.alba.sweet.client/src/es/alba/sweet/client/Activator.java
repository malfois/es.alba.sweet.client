package es.alba.sweet.client;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import es.alba.sweet.base.output.Output;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;

		Logger LOG = Logger.getLogger(Activator.class.getName());

		// suppress the logging output to the console
		Logger rootLogger = Logger.getLogger("");
		Handler[] handlers = rootLogger.getHandlers();
		if (handlers[0] instanceof ConsoleHandler) {
			rootLogger.removeHandler(handlers[0]);
		}

		Output.MESSAGE.setLogger(LOG);
		Output.DEBUG.setLogger(LOG);

		Output.DEBUG.info("es.alba.sweet.Activator.start", "Activator started for " + bundleContext.getBundle());

	}

	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
