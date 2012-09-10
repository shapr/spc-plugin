package com.example.helloworld;

import java.io.IOException;

import javax.speech.recognition.GrammarException;
import javax.speech.recognition.RuleGrammar;
import javax.speech.recognition.RuleParse;

import com.sun.speech.engine.recognition.BaseRecognizer;
import com.sun.speech.engine.recognition.BaseRuleGrammar;

import edu.cmu.sphinx.jsgf.JSGFGrammar;
import edu.cmu.sphinx.jsgf.JSGFGrammarException;
import edu.cmu.sphinx.jsgf.JSGFGrammarParseException;
import edu.cmu.sphinx.result.Result;

/**
* Provides the default behavior for dialog node. Applications will
* typically extend this class and override methods as appropriate
*/
class DialogNodeBehavior {
    private DialogManager.DialogNode node;

    /**
     * Called during the initialization phase
     *
     * @param node the dialog node that the behavior is attached to
     */
    public void onInit(DialogManager.DialogNode node) {
        this.node = node;
    }

    /**
     * Called when this node becomes the active node
     * @throws JSGFGrammarException 
     * @throws JSGFGrammarParseException 
     */
    public void onEntry() throws IOException, JSGFGrammarParseException, JSGFGrammarException {
        trace("Entering " + getName());
    }

    /**
     * Called when this node is ready to perform recognition
     */
    public void onReady() {
        trace("Ready " + getName());
    }

    /*
     * Called with the recognition results. Should return a string
     * representing the name of the next node.
     */
    public String onRecognize(Result result) throws GrammarException {
        String tagString =  getTagString(result);
        trace("Recognize result: " + result.getBestFinalResultNoFiller());
        trace("Recognize tag   : " + tagString);
        return tagString;
    }

    /**
     * Called when this node is no lnoger the active node
     */
    public void onExit() {
        trace("Exiting " + getName());
    }

    /**
     * Returns the name for this node
     *
     * @return the name of the node
     */
    public String getName() {
        return node.getName();
    }

    /**
     * Returns the string representation of this object
     *
     * @return the string representation of this object
     */
    public String toString() {
        return "Node " + getName();
    }

    /**
     * Retrieves the grammar associated with this ndoe
     *
     * @return the grammar
     */
    public JSGFGrammar getGrammar() {
        return node.getGrammar();
    }

    /**
     * Retrieves the rule parse for the given result
     *
     * @param the recognition result
     * @return the rule parse for the result
     * @throws GrammarException if there is an error while parsing the
     * result
     */
    RuleParse getRuleParse(Result result) throws GrammarException {
        String resultText = result.getBestFinalResultNoFiller();
        BaseRecognizer jsapiRecognizer = new BaseRecognizer(getGrammar().getGrammarManager());
        RuleGrammar ruleGrammar = new BaseRuleGrammar (jsapiRecognizer, getGrammar().getRuleGrammar());
        RuleParse ruleParse = ruleGrammar.parse(resultText, null);
        return ruleParse;
    }

    /**
     * Gets a space delimited string of tags representing the result
     *
     * @param result the recognition result
     * @return the tag string
     * @throws GrammarException if there is an error while parsing the
     * result
     */
    String getTagString(Result result) throws GrammarException {
        RuleParse ruleParse = getRuleParse(result);
        if (ruleParse == null)
            return null;
        String[] tags = ruleParse.getTags();
        if (tags == null)
            return "";
        StringBuilder sb = new StringBuilder();
        for (String tag : tags)
            sb.append(tag).append(' ');
        return sb.toString().trim();
    }

    /**
     * Outputs a trace message
     *
     * @param the trace message
     */
    void trace(String msg) {
        node.trace(msg);
    }
}
