/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 *
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.webreach.mirth.plugins;

import com.webreach.mirth.client.ui.editors.filter.FilterPane;

public abstract class FilterRulePlugin extends MirthEditorPanePlugin {

    public FilterRulePlugin(String name) {
        super(name);
    }

    public FilterRulePlugin(String name, FilterPane parent) {
        super(name, parent);
    }
}
