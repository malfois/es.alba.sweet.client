package es.alba.sweet.client.perspective;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.eclipse.e4.ui.internal.workbench.E4XMIResourceFactory;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;

import es.alba.sweet.base.output.Output;
import es.alba.sweet.client.EclipseUI;
import es.alba.sweet.client.core.DirectoryLocator;
import es.alba.sweet.client.core.constant.Directory;
import es.alba.sweet.client.core.constant.Id;
import es.alba.sweet.client.core.constant.Tag;

@SuppressWarnings("restriction")
public class Perspective {

	public static String SaveCurrentPerspective(String layoutName, boolean askConfirmation) {
		String savedLayoutName = (askConfirmation) ? LayoutModificationDialog.SaveConfirmation(layoutName) : LayoutModificationDialog.CheckLayoutName(layoutName);

		if (savedLayoutName == null) {
			Output.DEBUG.info("es.alba.sweet.perspective.Perspective.SaveCurrentPerspective", "Cancel button has been pushed. The current layout will not be saved");
			return savedLayoutName;
		}

		SavePerspective(savedLayoutName);
		return savedLayoutName;

	}

	public static String SaveAsCurrentPerspective(String layoutName) {
		String savedLayoutName = LayoutModificationDialog.inputDialog(layoutName);

		if (savedLayoutName == null) {
			Output.DEBUG.info("es.alba.sweet.perspective.Perspective.SaveCurrentPerspective", "Cancel button has been pushed. The current layout will not be saved");
			return savedLayoutName;
		}

		SavePerspective(savedLayoutName);
		return savedLayoutName;

	}

	@SuppressWarnings({})
	public static void SavePerspective(String layoutName) {
		Output.DEBUG.info("es.alba.sweet.perspective.Layout.savePerspective", "layout name will be saved as " + layoutName);

		// store model of the active perspective
		MPerspective activePerspective = EclipseUI.activePerspective();
		if (activePerspective == null) {
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "No perspective found", "Are you using perspectives?");
			// no perspective found, return
			return;
		}

		// create a resource, which is able to store e4 model elements
		E4XMIResourceFactory e4xmiResourceFactory = new E4XMIResourceFactory();
		Resource resource = e4xmiResourceFactory.createResource(null);

		// You must clone the perspective as snippet, otherwise the running
		// application would break, because the saving process of the resource
		// removes the element from the running application model
		MUIElement clonedPerspective = EclipseUI.modelService().cloneElement(activePerspective, EclipseUI.window());

		// add the cloned model element to the resource so that it may be stored
		resource.getContents().add((EObject) clonedPerspective);

		Path filename = getLayoutFilename(activePerspective.getLabel(), layoutName);
		File layoutFile = filename.toFile();
		try {
			// if file doesnt exists, then create it
			if (!layoutFile.exists()) {
				layoutFile.createNewFile();
			}
			FileOutputStream outputStream = new FileOutputStream(layoutFile);
			resource.save(outputStream, null);

			Output.MESSAGE.info("es.alba.sweet.toolbar.PerspectiveLayout.savePerspective", "Perspective layout saved in " + filename.toString());
			outputStream.close();
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static void loadPerspective(String layoutName) {
		// get the parent perspective stack, so that the loaded perspective can be added to it.
		MPerspective activePerspective = EclipseUI.activePerspective();
		Output.DEBUG.info("es.alba.sweet.perspective.Perspective.loadPerspective", "Loading perspective " + activePerspective);
		MElementContainer<MUIElement> perspectiveParent = activePerspective.getParent();

		MPerspective loadedPerspective = (layoutName.equals(PerspectiveConfiguration.DEFAULT)) ? loadDefaultPerspective(activePerspective.getElementId())
				: loadPerspectiveFromFile(layoutName);

		// remove the current perspective, which should be replaced by the loaded one
		List<MPerspective> alreadyPresentPerspective = EclipseUI.modelService().findElements(EclipseUI.window(), loadedPerspective.getElementId(), MPerspective.class, null);
		for (MPerspective perspective : alreadyPresentPerspective) {
			EclipseUI.modelService().removePerspectiveModel(perspective, EclipseUI.window());
		}

		// add the loaded perspective and switch to it
		perspectiveParent.getChildren().add(loadedPerspective);
		EclipseUI.partService().switchPerspective(loadedPerspective);

		String message = (layoutName.equals(PerspectiveConfiguration.DEFAULT)) ? "Default perspective loaded"
				: "Perspective " + getLayoutFilename(activePerspective.getLabel(), layoutName) + " loaded";
		Output.MESSAGE.info("es.alba.sweet.perspective.Layout.loadPerspective", message);
	}

	public static MPerspective loadPerspectiveFromFile(String layoutName) {
		Output.DEBUG.info("es.alba.sweet.perspective.Perspective.loadPerspectiveFromFile", "Loading perspective " + layoutName + " from file");
		// create a resource, which is able to store e4 model elements
		E4XMIResourceFactory e4xmiResourceFactory = new E4XMIResourceFactory();
		Resource resource = e4xmiResourceFactory.createResource(null);

		MPerspective activePerspective = EclipseUI.activePerspective();
		try (FileInputStream inputStream = new FileInputStream(getLayoutFilename(activePerspective.getLabel(), layoutName).toFile())) {

			// load the stored model element
			resource.load(inputStream, null);

			if (!resource.getContents().isEmpty()) {

				// after the model element is loaded it can be obtained from the contents of the resource
				MPerspective loadedPerspective = (MPerspective) resource.getContents().get(0);

				inputStream.close();

				return loadedPerspective;
			}
		} catch (IOException e) {
			Output.MESSAGE.error("es.alba.sweet.perspective.Layout.loadPerspective", "Error loading perspective " + getLayoutFilename(activePerspective.getLabel(), layoutName));
			MPerspective loadedPerspective = loadDefaultPerspective(activePerspective.getElementId());
			return loadedPerspective;
		}
		return null;
	}

	public static MPerspective loadDefaultPerspective(String perspectiveId) {
		Output.DEBUG.info("es.alba.sweet.perspective.Perspective.loadDefaultPerspective", "Loading default perspective " + perspectiveId);
		EModelService modelService = EclipseUI.modelService();
		EPartService partService = EclipseUI.partService();
		MApplication application = EclipseUI.application();

		MUIElement element = modelService.findSnippet(application, perspectiveId);

		if (element instanceof MPerspective) {
			MPerspective perspective = (MPerspective) element;

			if (perspective.getTags().contains(Tag.RELEASE)) {
				MPerspective perspectiveClone = (MPerspective) modelService.cloneSnippet(application, perspective.getElementId(), null);

				// Find the output part stack
				String outputId = String.join(".", Id.PARTSTACK, perspectiveClone.getLabel().toLowerCase(), Id.OUTPUT);
				MPartStack outputPartStack = (MPartStack) modelService.find(outputId, perspectiveClone);

				List<String> outputPartIds = Id.OUTPUT_PART_IDS;

				for (String partId : outputPartIds) {
					MPart part = partService.createPart(partId);
					outputPartStack.getChildren().add((MPart) modelService.cloneElement(part, application));
				}

				// remove partStack is no children are to be rendered/visible
				List<MPartStack> partStacks = modelService.findElements(perspectiveClone, null, MPartStack.class);
				for (MPartStack partStack : partStacks) {
					List<MStackElement> stackElements = partStack.getChildren();

					for (MStackElement stackElement : stackElements) {
						int nRelease = 0;
						if (stackElement.getTags().contains(Tag.RELEASE)) nRelease++;
						if (nRelease == 0) {
							partStack.setToBeRendered(false);
						}
					}
				}

				return perspectiveClone;
			}
		}
		return null;
	}

	public static Path getLayoutFilename(String perspectiveName, String layoutName) {
		String filename = perspectiveName.toLowerCase() + "_" + layoutName + ".xml";
		Path currentDir = DirectoryLocator.findPath(Directory.CONFIG);
		return Paths.get(currentDir.toString(), filename);
	}

}
