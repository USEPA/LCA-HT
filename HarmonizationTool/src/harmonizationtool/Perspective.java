package harmonizationtool;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);

//		layout.setFixed(true);
		layout.addView(View.ID, IPageLayout.LEFT, 0.2f, editorArea);
		layout.addView(QueryView.ID, IPageLayout.LEFT, 0.2f, editorArea);
		layout.addView(ViewData.ID, IPageLayout.LEFT, 0.8f, editorArea);
		layout.addView(ResultsView.ID, IPageLayout.LEFT, 0.8f, editorArea);
	}

}
