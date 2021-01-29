package es.alba.sweet.client.handlers;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.modeling.IPartListener;

import es.alba.sweet.base.output.Output;
import es.alba.sweet.client.EclipseUI;
import es.alba.sweet.client.perspective.PerspectiveControl;

public class VisibilityPartListener implements IPartListener {

	public VisibilityPartListener() {
	}

	@Override
	public void partActivated(MPart part) {
		// TODO Auto-generated method stub
		Output.DEBUG.info("es.alba.sweet.handlers.VisibilityPartListener.partActivated", part.getLabel());
		updateToolBar(part, true);
	}

	@Override
	public void partBroughtToTop(MPart part) {
		// TODO Auto-generated method stub

	}

	@Override
	public void partDeactivated(MPart part) {
		Output.DEBUG.info("es.alba.sweet.handlers.VisibilityPartListener.partDeactivated", part.getLabel());
		updateToolBar(part, false);
	}

	@Override
	public void partHidden(MPart part) {
		Output.DEBUG.info("es.alba.sweet.handlers.VisibilityPartListener.partHidden", part.getElementId() + " hidden");
		updateToolBar(part, false);
	}

	@Override
	public void partVisible(MPart part) {
		Output.DEBUG.info("es.alba.sweet.handlers.VisibilityPartListener.partVisible", part.getElementId() + " visible");
		updateToolBar(part, true);
	}

	private void updateToolBar(MPart part, boolean visible) {
		Output.DEBUG.info("es.alba.sweet.handlers.VisibilityPartListener.updateToolBar", "Updating button for " + part.getElementId());
		MToolControl toolControl = EclipseUI.getPerspectiveToolControl();
		PerspectiveControl control = (PerspectiveControl) toolControl.getObject();
		control.updateButton(part, visible);
		control.setDecoratorDirty();
	}

}
