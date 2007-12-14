/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Mirth.
 *
 * The Initial Developer of the Original Code is
 * WebReach, Inc.
 * Portions created by the Initial Developer are Copyright (C) 2006
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 *   Gerald Bortis <geraldb@webreachinc.com>
 *
 * ***** END LICENSE BLOCK ***** */

package com.webreach.mirth.server.mule.transformers;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.RhinoException;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mule.transformers.AbstractEventAwareTransformer;
import org.mule.umo.UMOEventContext;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.transformer.TransformerException;

import com.webreach.mirth.model.Attachment;
import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.model.Connector.Mode;
import com.webreach.mirth.model.MessageObject.Protocol;
import com.webreach.mirth.model.converters.IXMLSerializer;
import com.webreach.mirth.server.Constants;
import com.webreach.mirth.server.MirthJavascriptTransformerException;
import com.webreach.mirth.server.builders.ErrorMessageBuilder;
import com.webreach.mirth.server.controllers.AlertController;
import com.webreach.mirth.server.controllers.ControllerException;
import com.webreach.mirth.server.controllers.MessageObjectController;
import com.webreach.mirth.server.controllers.ScriptController;
import com.webreach.mirth.server.controllers.TemplateController;
import com.webreach.mirth.server.mule.adaptors.Adaptor;
import com.webreach.mirth.server.mule.adaptors.AdaptorFactory;
import com.webreach.mirth.server.util.CompiledScriptCache;
import com.webreach.mirth.server.util.JavaScriptScopeUtil;
import com.webreach.mirth.server.util.UUIDGenerator;

public class JavaScriptTransformer extends AbstractEventAwareTransformer {
	private Logger logger = Logger.getLogger(this.getClass());
	private MessageObjectController messageObjectController = MessageObjectController.getInstance();
	private AlertController alertController = AlertController.getInstance();
	private TemplateController templateController = TemplateController.getInstance();
	private CompiledScriptCache compiledScriptCache = CompiledScriptCache.getInstance();
	private ScriptController scriptController = ScriptController.getInstance();
	private ErrorMessageBuilder errorBuilder = new ErrorMessageBuilder();

	private String inboundProtocol;
	private String outboundProtocol;
	private Map inboundProperties;
	private Map outboundProperties;
	private String channelId;
	private String connectorName;
	private boolean encryptData;
	private boolean removeNamespace;
	private String scriptId;
	private String templateId;
	private String mode;
	private String template;
	private static ScriptableObject sealedSharedScope;
	private boolean emptyFilterAndTransformer;

	public String getChannelId() {
		return this.channelId;
	}

	public String getMode() {
		return this.mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	public String getInboundProtocol() {
		return this.inboundProtocol;
	}

	public void setInboundProtocol(String inboundProtocol) {
		this.inboundProtocol = inboundProtocol;
	}

	public String getOutboundProtocol() {
		return this.outboundProtocol;
	}

	public void setOutboundProtocol(String outboundProtocol) {
		this.outboundProtocol = outboundProtocol;
	}

	public String getConnectorName() {
		return this.connectorName;
	}

	public void setConnectorName(String connectorName) {
		this.connectorName = connectorName;
	}

	public boolean isEncryptData() {
		return this.encryptData;
	}

	public void setEncryptData(boolean encryptData) {
		this.encryptData = encryptData;
	}

	public boolean isRemoveNamespace() {
		return this.removeNamespace;
	}

	public void setRemoveNamespace(boolean removeNamespace) {
		this.removeNamespace = removeNamespace;
	}

	public String getScriptId() {
		return this.scriptId;
	}

	public void setScriptId(String scriptId) {
		this.scriptId = scriptId;
	}

	public Map getInboundProperties() {
		return inboundProperties;
	}

	public void setInboundProperties(Map inboundProperties) {
		this.inboundProperties = inboundProperties;
	}

	public Map getOutboundProperties() {
		return outboundProperties;
	}

	public void setOutboundProperties(Map outboundProperties) {
		this.outboundProperties = outboundProperties;
	}

	public String getTemplateId() {
		return this.templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;

		if (templateId != null) {
			try {
				this.template = templateController.getTemplate(templateId);
			} catch (ControllerException e) {
				logger.error(errorBuilder.buildErrorMessage(Constants.ERROR_300, null, e));
			}
		}
	}

	public static Context getContext() {
		Context context = Context.enter();

		if (sealedSharedScope == null) {
			String importScript = getJavascriptImportScript();
			sealedSharedScope = new ImporterTopLevel(context);
			JavaScriptScopeUtil.buildScope(sealedSharedScope);
			Script script = context.compileString(importScript, UUIDGenerator.getUUID(), 1, null);
			script.exec(context, sealedSharedScope);
			sealedSharedScope.sealObject();
		}

		return context;
	}

	public static String getJavascriptImportScript() {
		StringBuilder script = new StringBuilder();
		script.append("importPackage(Packages.com.webreach.mirth.server.util);\n");
		script.append("importPackage(Packages.com.webreach.mirth.model.converters);\n");
		script.append("regex = new RegExp('');\n");
		script.append("xml = new XML('');\n");
		script.append("xmllist = new XMLList();\n");
		script.append("namespace = new Namespace();\n");
		script.append("qname = new QName();\n");
		// ast: Allow ending whitespaces from the input XML
		script.append("XML.ignoreWhitespace=false;");
		// ast: Allow ending whitespaces to the output XML
		script.append("XML.prettyPrinting=false;");
		return script.toString();
	}

	public Scriptable getScope() {
		Scriptable scope = getContext().newObject(sealedSharedScope);
		scope.setPrototype(sealedSharedScope);
		scope.setParentScope(null);
		return scope;
	}

	@Override
	public void initialise() throws InitialisationException {
		Context context = getContext();

		try {
			// Scripts are not compiled if they are blank or do not exist in the
			// database. Note that in Oracle, a blank script is the same as a
			// NULL script.
			String script = scriptController.getScript(scriptId);

			if ((script != null) && (script.length() > 0)) {
				String generatedScript = generateScript(script);
				logger.debug("compiling filter script");
				Script compiledScript = context.compileString(generatedScript, scriptId, 1, null);
				compiledScriptCache.putCompiledScript(scriptId, compiledScript);
			} else {
				emptyFilterAndTransformer = true;
			}
		} catch (Exception e) {
			if (e instanceof RhinoException) {
				e = new MirthJavascriptTransformerException((RhinoException) e, channelId, connectorName, 5, "Filter/Transformer");
			}

			logger.error(errorBuilder.buildErrorMessage(Constants.ERROR_300, null, e));
			throw new InitialisationException(e, this);
		}
	}

	@Override
	public Object transform(Object source, UMOEventContext context) throws TransformerException {
		MessageObject messageObject = null;

		// ---- Begin MO checks -----
		try {
			Script script = compiledScriptCache.getCompiledScript(scriptId);
			emptyFilterAndTransformer = true;
			
			if ((script != null)) {
				emptyFilterAndTransformer = false;
			}
			
			if (this.getMode().equals(Mode.SOURCE.toString())) {
				Adaptor adaptor = AdaptorFactory.getAdaptor(Protocol.valueOf(inboundProtocol));
				messageObject = adaptor.getMessage((String) source, channelId, encryptData, inboundProperties, emptyFilterAndTransformer);
				
				// Grab and process our attachments
				List<Attachment> attachments = (List<Attachment>) context.getProperties().get("attachments");
				context.getProperties().remove("attachments");
				for (Iterator iter = attachments.iterator(); iter.hasNext();) {
					Attachment attachment = (Attachment) iter.next();
					attachment.setMessageId(messageObject.getId());
					messageObject.setAttachment(true);
					MessageObjectController.getInstance().insertAttachment(attachment);
				}
				// Load properties from the context to the messageObject
				messageObject.getChannelMap().putAll(context.getProperties());
			} else if (this.getMode().equals(Mode.DESTINATION.toString())) {
				MessageObject incomingMessageObject = (MessageObject) source;
				Adaptor adaptor = AdaptorFactory.getAdaptor(Protocol.valueOf(inboundProtocol));
				messageObject = adaptor.convertMessage(incomingMessageObject, this.getConnectorName(), channelId, encryptData, inboundProperties, emptyFilterAndTransformer);
				messageObject.setEncodedDataProtocol(Protocol.valueOf(this.outboundProtocol));
			}
		} catch (Exception e) {
			alertController.sendAlerts(channelId, Constants.ERROR_301, null, e);
			throw new TransformerException(this, e);
		}
		// ---- End MO checks -----

		MessageObject finalMessageObject = evaluateScript(messageObject);
		boolean messageAccepted = finalMessageObject.getStatus().equals(MessageObject.Status.TRANSFORMED);

		try {
			if (messageAccepted) {
				if (this.getMode().equals(Mode.SOURCE.toString())) {
					// only update on the source - it doesn't matter on each
					// destination
					messageObjectController.updateMessage(finalMessageObject, false);
				}

				return finalMessageObject;
			} else {
				messageObjectController.setFiltered(messageObject, "Message has been filtered");
				return messageObject;
			}
		} catch (Exception e) {
			// send alert if the transformation process fails
			alertController.sendAlerts(channelId, Constants.ERROR_300, null, e);
			throw new TransformerException(this, e);
		}
	}

	private MessageObject evaluateScript(MessageObject messageObject) throws TransformerException {
		Logger scriptLogger = Logger.getLogger("filter");
		String phase = new String();

		try {
			Context context = getContext();
			Scriptable scope = getScope();

			// load variables in JavaScript scope
			JavaScriptScopeUtil.addMessageObject(scope, messageObject);
			JavaScriptScopeUtil.addLogger(scope, scriptLogger);
			JavaScriptScopeUtil.addChannel(scope, channelId);
			JavaScriptScopeUtil.addGlobalMap(scope);
			scope.put("template", scope, template);
			scope.put("phase", scope, phase);

			// get the script from the cache and execute it
			Script compiledScript = compiledScriptCache.getCompiledScript(scriptId);

			if (compiledScript == null) {
				logger.debug("script could not be found in cache");
			} else {
				compiledScript.exec(context, scope);
			}

			if (!messageObject.getStatus().equals(MessageObject.Status.FILTERED)) {
				// TODO: Check logic here
				Object transformedData;
				Protocol encodedDataProtocol;
				Map encodedDataProperties;

				if (template != null && template.length() > 0) {
					transformedData = scope.get("tmp", scope);
					encodedDataProtocol = Protocol.valueOf(this.getOutboundProtocol());
					encodedDataProperties = this.getOutboundProperties();
				} else {
					if (this.getInboundProtocol().equals(Protocol.XML.toString()) && !this.getOutboundProtocol().equals(Protocol.XML.toString())) {
						// we don't have a template and we have XML coming in,
						// let's convert it
						transformedData = scope.get("msg", scope);
						encodedDataProtocol = Protocol.valueOf(this.getOutboundProtocol());
						encodedDataProperties = this.getOutboundProperties();
					} else {
						transformedData = scope.get("msg", scope);
						encodedDataProtocol = Protocol.valueOf(this.getInboundProtocol());
						encodedDataProperties = this.getInboundProperties();
					}
				}

				if (transformedData != Scriptable.NOT_FOUND) {
					// set the transformedData to the template We replace all
					// here because we do not want pretty-printed XML
					messageObject.setTransformedData(context.toString(transformedData));
				}

				if ((messageObject.getTransformedData() != null)) {
					IXMLSerializer<String> serializer = AdaptorFactory.getAdaptor(encodedDataProtocol).getSerializer(encodedDataProperties);
					messageObject.setEncodedData(serializer.fromXML(messageObject.getTransformedData()));
					messageObject.setEncodedDataProtocol(encodedDataProtocol);
				}

				messageObject.setStatus(MessageObject.Status.TRANSFORMED);
			}

			return messageObject;
		} catch (Exception e) {
			if (e instanceof RhinoException) {
				e = new MirthJavascriptTransformerException((RhinoException) e, channelId, connectorName, 5, phase.toUpperCase());
			}

			if (phase.equals("filter")) {
				messageObjectController.setError(messageObject, Constants.ERROR_200, "Error evaluating filter", e);
			} else {
				messageObjectController.setError(messageObject, Constants.ERROR_300, "Error evaluating transformer", e);
			}

			throw new TransformerException(this, e);
		} finally {
			Context.exit();
		}
	}

	private String generateScript(String oldScript) {
		logger.debug("generating script");

		StringBuilder newScript = new StringBuilder();
		newScript.append("default xml namespace = '';\n");

		// script used to check for exitence of segment
		newScript.append("function validate(mapping, defaultValue, replacement) { var result; if (mapping != undefined) { result = new java.lang.String(mapping.toString()); } if ((result == undefined) || (result.length() == 0)) { result = defaultValue; } if (replacement != undefined) { for (i = 0; i < replacement.length; i++) { var entry = replacement[i]; result = result.replaceAll(entry[0], entry[1]); } } return result; }");

		// add #trim() function to JS String
		newScript.append("String.prototype.trim = function() { return this.replace(/^\\s+|\\s+$/g,\"\").replace(/^\\t+|\\t+$/g,\"\"); };");
		
		newScript.append("function $(string) { ");
		newScript.append("if (connectorMap.containsKey(string)) { return connectorMap.get(string); }");
		newScript.append("else if (channelMap.containsKey(string)) { return channelMap.get(string); }");
		newScript.append("else if (globalMap.containsKey(string)) { return globalMap.get(string); }");
		newScript.append("else { return ''; }");
		newScript.append("}");

		// Helper function to access globalMap
		newScript.append("function $g(key, value) {");
		newScript.append("if (arguments.length == 1) { return globalMap.get(key); }");
		newScript.append("else if (arguments.length == 2) { globalMap.put(key, value); }");
		newScript.append("}");
		
		// Helper function to access channelMap
		newScript.append("function $c(key, value) {");
		newScript.append("if (arguments.length == 1) { return channelMap.get(key); }");
		newScript.append("else if (arguments.length == 2) { channelMap.put(key, value); }");
		newScript.append("}");
		// Helper function to access connectorMap
		newScript.append("function $co(key, value) {");
		newScript.append("if (arguments.length == 1) { return connectorMap.get(key); }");
		newScript.append("else if (arguments.length == 2) { connectorMap.put(key, value); }");
		newScript.append("}");

		// Helper function to access responseMap
		newScript.append("function $r(key, value) {");
		newScript.append("if (arguments.length == 1) { return responseMap.get(key); }");
		newScript.append("else if (arguments.length == 2) { responseMap.put(key, value); }");
		newScript.append("}");
		
		// Helper function to create segments
		newScript.append("function createSegment(name, message, index) {");
		newScript.append("if (arguments.length == 1) { return new XML('<' + name + '></' + name + '>'); };");
		newScript.append("if (arguments.length == 2) { index = 0 };");
		newScript.append("message[name][index] = new XML('<' + name + '></' + name + '>');  return message[name][index];");
		newScript.append("}");
		
		// TODO: Look into optimizing. Potentially moving p.c.wr.m.s.c.MOC to an outside var
		
		// Helper function to get attachments
		newScript.append("function getAttachments() {");
		newScript.append("return Packages.com.webreach.mirth.server.controllers.MessageObjectController.getInstance().getAttachmentsByMessageId(messageObject.getId());");
		newScript.append("}");
		
		// Helper function to set attachment
		newScript.append("function addAttachment(data, type) {");
		newScript.append("var attachment = Packages.com.webreach.mirth.server.controllers.MessageObjectController.getInstance().createAttachment(data, type, messageObject);messageObject.setAttachment(true);");
		newScript.append("Packages.com.webreach.mirth.server.controllers.MessageObjectController.getInstance().insertAttachment(attachment);\n");
        newScript.append("return attachment;\n");
        newScript.append("}\n");
		
        // ast: Allow ending whitespaces from the input XML
		newScript.append("XML.ignoreWhitespace=false;");
		// ast: Allow ending whitespaces to the output XML
		newScript.append("XML.prettyPrinting=false;");

		if (removeNamespace) {
			newScript.append("var newMessage = message.replace(/xmlns:?[^=]*=[\"\"][^\"\"]*[\"\"]/g, '');\n");
		} else {
			newScript.append("var newMessage = message;\n");
		}

		newScript.append("msg = new XML(newMessage);\n");

		// turn the template into an E4X XML object
		if (template != null && template.length() > 0) {
			// We have to remove the namespaces so E4X allows use to use the
			// msg[''] syntax
			newScript.append("var newTemplate = template.replace(/xmlns:?[^=]*=[\"\"][^\"\"]*[\"\"]/g, '');\n");
			newScript.append("tmp = new XML(newTemplate);\n");
		}

		newScript.append(oldScript); // has doFilter() and doTransform()
		newScript.append("if (doFilter() == true) { doTransform(); } else { messageObject.setStatus(Packages.com.webreach.mirth.model.MessageObject.Status.FILTERED); };");
		return newScript.toString();
	}
}