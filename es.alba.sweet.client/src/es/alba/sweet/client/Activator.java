package es.alba.sweet.client;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import es.alba.sweet.base.constant.SharedDirectory;
import es.alba.sweet.base.constant.UserHome;
import es.alba.sweet.base.logger.LogFile;
import es.alba.sweet.base.output.Output;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;

		System.out.println(SharedDirectory.NAME.get());
		LogFile.create(Activator.class.getName(), UserHome.CLIENT);

		Output.DEBUG.info("es.alba.sweet.Activator.start", "Activator started for " + bundleContext.getBundle());

	}

	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
