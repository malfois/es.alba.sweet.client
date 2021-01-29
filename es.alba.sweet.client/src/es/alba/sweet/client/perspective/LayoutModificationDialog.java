package es.alba.sweet.client.perspective;

import org.eclipse.nebula.widgets.opal.dialog.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;

import es.alba.sweet.base.output.Output;

public class LayoutModificationDialog {

	public static String SaveConfirmation(String layoutName) {
		Output.DEBUG.info("es.alba.sweet.perspective.LayoutModificationDialog.GetLayoutName", "Asking for saving the modified layout");
		boolean confirm = Dialog.isConfirmed("Do you want to save the layout " + layoutName + "?", "Layout " + layoutName + " has been changed but not saved");

		if (confirm) return inputDialog(layoutName);

		return null;

	}

	public static String inputDialog(String layoutName) {
		boolean rejected = rejected(layoutName);

		String restrictions = "Only characters or digits are allowed.\nAny other characters will be removed";

		String message = "You can NOT overwrite the layout name default\n" + restrictions;
		if (layoutName.length() == 0) message = "You must type a name\n" + restrictions;

		String input = (rejected) ? "" : layoutName;
		while (rejected) {
			Output.DEBUG.info("es.alba.sweet.perspective.LayoutModificationDialog.inputDialog", input + "not accepted. Asking for a new name");
			input = Dialog.ask("Enter the name of the new layout", message, "");

			// cancel button has been pushed
			if (input == null) {
				Output.DEBUG.info("es.alba.sweet.perspective.LayoutModificationDialog.inputDialog", "cancel button pushed, Layout will not be saved");
				return null;
			}

			input = input.replaceAll("[^A-Za-z0-9]", "").trim();

			rejected = rejected(input);
		}
		return input;
	}

	private static boolean rejected(String layoutName) {
		return layoutName.equals(PerspectiveConfiguration.DEFAULT) || layoutName.length() == 0;
	}

	public static String CheckLayoutName(String layoutName) {
		if (rejected(layoutName)) return inputDialog(layoutName);

		return layoutName;

	}

	public static DeleteLayoutChoice deleteLayout(String layoutName) {
		final Dialog dialog = new Dialog();
		dialog.setTitle("Deleting layout");
		dialog.setMinimumWidth(400);
		dialog.getMessageArea().setTitle("Do you want to delete this layout? [" + layoutName + "]") //
				.setIcon(Display.getCurrent().getSystemImage(SWT.ICON_QUESTION));

		dialog.getFooterArea().addCheckBox("Delete the layout on disk", false).setButtonLabels("Yes", "No");
		dialog.show();

		boolean buttonChoice = dialog.getSelectedButton() == 0 ? true : false;
		return new DeleteLayoutChoice(buttonChoice, dialog.getCheckboxValue());

	}
}
