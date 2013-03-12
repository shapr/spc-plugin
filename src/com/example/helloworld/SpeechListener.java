package com.example.helloworld;

import java.io.IOException;
import java.net.URL;
import java.util.Stack;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchWindow;

import com.example.helloworld.DialogManager.DialogNode;

import edu.cmu.sphinx.frontend.util.AudioFileDataSource;
import edu.cmu.sphinx.jsgf.JSGFGrammarException;
import edu.cmu.sphinx.jsgf.JSGFGrammarParseException;
import edu.cmu.sphinx.result.Result;
import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;

public class SpeechListener implements Runnable, SLResultListener {
	private IWorkbenchWindow window;
	private String configFile;
	private URL cfgURL;
	private URL audioURL;
	private ConfigurationManager configManager;
	private DialogManager dialogManager;
	private Stack<TextInsertion> previousInserts;
	
	public SpeechListener( IWorkbenchWindow w, String config )
	{
		this.window = w;
		this.configFile = config;
		this.setAudioURL(null);
		
		try {
			String filePath = "lib/" + configFile;
			URL tmp = Platform.getBundle( Activator.PLUGIN_ID ).getEntry(filePath);
			this.cfgURL = FileLocator.toFileURL( tmp );
			
			configManager = new ConfigurationManager( cfgURL );

	        dialogManager = (DialogManager)configManager.lookup("dialogManager");

	        dialogManager.addNode( "program", new SLBehavior() );
	        dialogManager.addNode( "function", new SLBehavior() );
	        dialogManager.addNode( "while", new SLBehavior() );
	        dialogManager.addNode( "if_block", new SLBehavior() );
	        dialogManager.addNode( "else_block", new SLBehavior() );
	        dialogManager.addNode( "string", new SLBehavior() );
	        
	        dialogManager.setInitialNode("program");

	        dialogManager.addResultListener( this );
	        
			dialogManager.allocate();

		}
		catch ( IOException e ) {
			System.out.println( "Cannot find " + configFile + ": " + e.getMessage() );
			return;
		}
	}
	
	public void run()
	{        

		try {	
	        if ( this.audioURL != null ) {
	        	AudioFileDataSource dataSource = (AudioFileDataSource) configManager.lookup("audioFileDataSource");
	        	dataSource.setAudioFile(audioURL, null);
	        }

            System.out.println("Running  ...");
    		this.previousInserts = new Stack<TextInsertion>();	// new one every time even though it's static
            DialogManager.clearSavedStates();
            dialogManager.go();
            System.out.println("Cleaning up  ...");
			dialogManager.deallocate();
		} catch ( JSGFGrammarParseException e ) {
			System.err.println( "Caught parse exception while listening: " + e );
		} catch ( JSGFGrammarException e ) {
			System.err.println( "Caught grammar exception while listening: " + e );
		}
		return;
	}

	private void insertText( String resultText, DialogNode context, String tag )
	{
		if ( resultText.length() == 0 ) {
			return;
		}
		
		String tmp = resultText;
		tmp = tmp.replaceAll( "\\bzero\\b", "0" );
		tmp = tmp.replaceAll( "\\bone\\b", "1" );
		tmp = tmp.replaceAll( "\\btwo\\b", "2" );
		tmp = tmp.replaceAll( "\\bthree\\b", "3" );
		tmp = tmp.replaceAll( "\\bfour\\b", "4" );
		tmp = tmp.replaceAll( "\\bfive\\b", "5" );
		tmp = tmp.replaceAll( "\\bsix\\b", "6" );
		tmp = tmp.replaceAll( "\\bseven\\b", "7" );
		tmp = tmp.replaceAll( "\\beight\\b", "8" );
		tmp = tmp.replaceAll( "\\bnine\\b", "9" );
		tmp = tmp.replaceAll( "\\bten\\b", "10" );
		tmp = tmp.replaceAll( "\\bless than or equal to\\b", "<=" );
		tmp = tmp.replaceAll( "\\bless than\\b", "<" );
		tmp = tmp.replaceAll( "\\bgreater than or equal to\\b", ">=" );
		tmp = tmp.replaceAll( "\\bgreater than\\b", ">" );
		tmp = tmp.replaceAll( "\\bnot equals\\b", "!=" );
		tmp = tmp.replaceAll( "\\bequals\\b", "=" );
		tmp = tmp.replaceAll( "\\bplus\\b", "+" );
		tmp = tmp.replaceAll( "\\bminus\\b", "-" );
		tmp = tmp.replaceAll( "\\btimes\\b", "*" );
		tmp = tmp.replaceAll( "\\bdivided by\\b", "/" );
		tmp = tmp.replaceAll( "\\bover\\b", "/" );
		
		String insertText = tmp + " \n";
		
        //IWorkbench wb = PlatformUI.getWorkbench();
        //IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		int depth = DialogManager.getSavedStates().size() - 1;
		if ( tag != null && (tag.equals("out") || tag.equals("else_block")) ) {
			depth--;
		}
		TextInsertOp op = new TextInsertOp( window, insertText, previousInserts, context, depth );
        Display.getDefault().asyncExec( op );
	}

	@Override
	public void newProperties(PropertySheet ps) throws PropertyException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void newResult(Result result) {
		// FIXME: should this ever happen?
		assert( false );
	}

	public URL getAudioURL() {
		return audioURL;
	}

	public void setAudioURL(URL audioURL) {
		this.audioURL = audioURL;
	}

	@Override
	public String newResult(Result result, DialogNode context, String tag) {
		// FIXME: handle null better
		if ( result == null ) {
			return "exit";
		}
		
		boolean isFinal = result.isFinal();
		String hyp = result.getBestResultNoFiller();
		String text = (result == null) ? "" : result.getBestFinalResultNoFiller();
		
		if ( !isFinal || !text.equals(hyp) || text.equals("") ) {
            SpeechManager.getManager().setHypothesis( hyp, false );
		} else {
			SpeechManager.getManager().setHypothesis( text, true );
		}
		String nnTag = (tag == null) ? "" : tag;
		System.out.println( (isFinal ? "Final" : "Intermediate") + " hypothesis: " + text );

		String newTag;
		
		if ( nnTag.equals("exit") ) {
			newTag = "exit";
			
		} else if ( nnTag.equals("correction") ) {
			if ( previousInserts.size() > 0 ) {
				TextInsertion lastInsertion = previousInserts.pop();
				TextCorrectionOp op = new TextCorrectionOp( window, lastInsertion );
		        Display.getDefault().asyncExec( op );
		        if ( previousInserts.size() == 0 || lastInsertion.context == previousInserts.peek().context ) {
		        	newTag = "";
		        } else {
		        	newTag = "out";
		        }
			} else {
				newTag = "";
			}
	      
		} else if ( nnTag.equals("reset") ) {
            TextClearOp op = new TextClearOp( window );
            Display.getDefault().asyncExec( op );

			newTag = "reset";
			
		} else {
			if ( text.length() > 0 ) {
				insertText( text, context, tag );
			}
			newTag = tag;
		}
		
		return newTag;
	}
}
