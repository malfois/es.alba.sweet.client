package es.alba.sweet.client;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

import es.alba.sweet.client.core.constant.Id;

public class EclipseUI {

	private static IEclipseContext eclipseContext;

	public static void start(IEclipseContext eclipseContext) {
		EclipseUI.eclipseContext = eclipseContext;
	}

	public static EModelService modelService() {
		return eclipseContext.get(EModelService.class);
	}

	public static EPartService partService() {
		return eclipseContext.get(EPartService.class);
	}

	public static MApplication application() {
		return eclipseContext.get(MApplication.class);
	}

	public static MTrimmedWindow window() {
		return (MTrimmedWindow) application().getSelectedElement();
	}

	public static MPerspective activePerspective() {
		return modelService().getActivePerspective(window());
	}

	public static MToolControl getPerspectiveToolControl() {
		System.out.println(window());
		return (MToolControl) modelService().find(Id.PERSPECTIVE_TOOL_CONTROL, window());
	}

}
