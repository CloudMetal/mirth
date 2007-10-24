package com.webreach.mirth.plugins.transformer.step.javascript;

import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.EvaluatorException;
import org.mozilla.javascript.Script;
import org.syntax.jedit.tokenmarker.JavaScriptTokenMarker;

import com.webreach.mirth.client.ui.editors.BasePanel;
import com.webreach.mirth.client.ui.editors.ScriptPanel;
import com.webreach.mirth.client.ui.editors.MirthEditorPane;
import com.webreach.mirth.client.ui.editors.transformer.TransformerPane;
import com.webreach.mirth.client.ui.panels.reference.ReferenceListFactory;
import com.webreach.mirth.plugins.TransformerStepPlugin;
import com.webreach.mirth.server.util.UUIDGenerator;

public class JavascriptStepPlugin extends TransformerStepPlugin {
	private ScriptPanel panel;

	public JavascriptStepPlugin(String name, TransformerPane parent) {
		super(name, parent);
		panel = new ScriptPanel(parent, new JavaScriptTokenMarker(), ReferenceListFactory.MESSAGE_CONTEXT);
	}

	@Override
	public BasePanel getPanel() {
		return panel;
	}

	@Override
	public boolean isNameEditable() {
		return true;
	}

	public String getNewName() {
		return "New Step";
	}

	@Override
	public Map<Object, Object> getData(int row) {
		return panel.getData();
	}

	@Override
	public void setData(Map<Object, Object> data) {
		panel.setData(data);
	}

	@Override
	public void clearData() {
		panel.setData(null);
	}

	@Override
	public void initData() {
		((TransformerPane) parent).invalidVar = false;
		clearData();
	}

	public String doValidate() {
		try {
			Context context = Context.enter();
			Script compiledFilterScript = context.compileString("function rhinoWrapper() {" + panel.getScript() + "}", UUIDGenerator.getUUID(), 1, null);
		} catch (EvaluatorException e) {
			return "Error on line " + e.lineNumber() + ": " + e.getMessage() + ".";
		} catch (Exception e) {
			return "Unknown error occurred during validation.";
		} finally {
			Context.exit();
		}
		return null;
	}

	@Override
	public String getScript(Map<Object, Object> data) {
		return data.get("Script").toString();
	}

	public boolean showValidateTask() {
		return true;
	}

	@Override
	public String getDisplayName() {
		return "JavaScript";
	}

}
