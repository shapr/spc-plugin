package com.example.helloworld.views;


import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.example.helloworld.SpeechManager;
import com.example.helloworld.UpdateViewOp;


/**
 * This sample class demonstrates how to plug-in a new
 * workbench view. The view shows data obtained from the
 * model. The sample creates a dummy model on the fly,
 * but a real implementation would connect to the model
 * available either in this or another plug-in (e.g. the workspace).
 * The view is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model
 * objects should be presented in the view. Each
 * view can present the same model objects using
 * different labels and icons, if needed. Alternatively,
 * a single label provider can be shared between views
 * in order to ensure that objects of the same type are
 * presented in the same way everywhere.
 * <p>
 */

public class SpokenLangView extends ViewPart
{

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "com.example.helloworld.views.SpokenLangView";

	private TableViewer viewer;
	private Label listenStatus;
	private Label currentContext;
	private Label currentHypothesis;

	/*
	 * The content provider class is responsible for
	 * providing objects to the view. It can wrap
	 * existing objects in adapters or simply return
	 * objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore
	 * it and always show the same content 
	 * (like Task List, for example).
	 */
	 
	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			return new String[] { "One", "Two", "Three" };
		}
	}
	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}
		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().
					getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}
	class NameSorter extends ViewerSorter {
	}

	/**
	 * The constructor.
	 */
	public SpokenLangView()
	{
	}

	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent)
	{
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		parent.setLayout( layout );
		GridData gridData;

		Label label = new Label( parent, SWT.RIGHT );
		label.setText( "Current Status:" );
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		label.setLayoutData( gridData );
		
		listenStatus = new Label( parent, SWT.LEFT );
		listenStatus.setText( "Not listening" );
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		listenStatus.setLayoutData( gridData );
		Display display = Display.getDefault();
		Color textColor = display.getSystemColor(SWT.COLOR_RED);
		listenStatus.setForeground( textColor );
		
		label = new Label( parent, SWT.RIGHT );
		label.setText( "Context:" );
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		label.setLayoutData( gridData );

		currentContext = new Label( parent, SWT.LEFT );
		currentContext.setText( "" );
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		currentContext.setLayoutData( gridData );

		label = new Label( parent, SWT.RIGHT );
		label.setText( "Hypothesis:" );
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = false;
		label.setLayoutData( gridData );

		currentHypothesis = new Label( parent, SWT.LEFT );
		currentHypothesis.setText( "" );
		gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		currentHypothesis.setLayoutData( gridData );
		
		SpeechManager.getManager().setView( this );
		
//		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
//		viewer.setContentProvider(new ViewContentProvider());
//		viewer.setLabelProvider(new ViewLabelProvider());
//		viewer.setSorter(new NameSorter());
//		viewer.setInput(getViewSite());

//		// Create the help context id for the viewer's control
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "com.example.helloworld.viewer");
//		makeActions();
//		hookContextMenu();
//		hookDoubleClickAction();
//		contributeToActionBars();
	}

	public void updateState()
	{
		UpdateViewOp op = new UpdateViewOp( this );
        Display.getDefault().syncExec( op );
	}

	public void updateStateSafe()
	{
		SpeechManager sm = SpeechManager.getManager();
		Display display = Display.getDefault();
		listenStatus.setText( sm.isListening() ? "Listening" : "Not Listening" );
		Color textColor = sm.isListening() ? display.getSystemColor(SWT.COLOR_GREEN) : display.getSystemColor(SWT.COLOR_RED);
		listenStatus.setForeground( textColor );
		currentContext.setText( sm.getContext() );
		currentHypothesis.setText( sm.getHypothesis() );
		textColor = sm.isFinal() ? display.getSystemColor(SWT.COLOR_GREEN) : display.getSystemColor(SWT.COLOR_RED);
		currentHypothesis.setForeground( textColor );
	}
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}