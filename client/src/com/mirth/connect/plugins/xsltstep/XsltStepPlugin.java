/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.plugins.xsltstep;

import com.mirth.connect.plugins.TransformerStepPlugin;
import com.mirth.connect.client.ui.editors.BasePanel;
import com.mirth.connect.client.ui.editors.XsltStepPanel;
import com.mirth.connect.client.ui.editors.transformer.TransformerPane;

import java.util.Map;
import java.util.HashMap;

public class XsltStepPlugin extends TransformerStepPlugin {

    private XsltStepPanel panel;

    public XsltStepPlugin(String name) {
        super(name);
    }

    public XsltStepPlugin(String name, TransformerPane parent) {
        super(name, parent);
        panel = new XsltStepPanel(parent);
    }

    @Override
    public BasePanel getPanel() {
        return panel;
    }

    @Override
    public boolean isNameEditable() {
        return true;
    }

    @Override
    public Map<Object, Object> getData(int row) {
        Map<Object, Object> data = panel.getData();
        String sourceVar = data.get("Source").toString();
        String resultVar = data.get("Result").toString();

        // check for empty variable names
        if (sourceVar == null || sourceVar.equals("")) {

            ((TransformerPane) parent).setInvalidVar(true);
            String msg = "The source field cannot be blank.\nPlease enter a new source.\n";
            ((TransformerPane) parent).setRowSelectionInterval(row, row);
            ((TransformerPane) parent).getParentFrame().alertWarning(parent.parent, msg);

        } else if (resultVar == null || resultVar.equals("")) {

            ((TransformerPane) parent).setInvalidVar(true);
            String msg = "The result field cannot be blank.\nPlease enter a new result.\n";
            ((TransformerPane) parent).setRowSelectionInterval(row, row);
            ((TransformerPane) parent).getParentFrame().alertWarning(parent.parent, msg);

        } else {
            ((TransformerPane) parent).setInvalidVar(false);
        }

        return data;
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
        Map<Object, Object> data = new HashMap<Object, Object>();
        data.put("Source", "");
        data.put("Result", "");
        data.put("XsltTemplate", "");
        panel.setData(data);
    }

    @Override
    public String getScript(Map<Object, Object> data) {

        StringBuilder script = new StringBuilder();

        script.append("tFactory = Packages.javax.xml.transform.TransformerFactory.newInstance();");
        script.append("xsltTemplate = new Packages.java.io.StringReader(" + data.get("XsltTemplate") + ");");
        script.append("transformer = tFactory.newTransformer(new Packages.javax.xml.transform.stream.StreamSource(xsltTemplate));");
        script.append("sourceVar = new Packages.java.io.StringReader(" + data.get("Source") + ");");
        script.append("resultVar = new Packages.java.io.StringWriter();");
        script.append("transformer.transform(new Packages.javax.xml.transform.stream.StreamSource(sourceVar), new Packages.javax.xml.transform.stream.StreamResult(resultVar));");
        script.append("channelMap.put('" + data.get("Result") + "', resultVar)");

        return script.toString();
    }

    public String getDisplayName() {
        return "XSLT Step";
    }
}
