package org.mule.providers;

import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.tools.VelocityFormatter;
import org.apache.velocity.tools.generic.DateTool;
import org.mule.util.UUID;
import org.mule.util.Utility;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.server.util.GlobalVariableStore;
import com.webreach.mirth.util.Entities;

public class TemplateValueReplacer {
	private Logger logger = Logger.getLogger(this.getClass());
	private long count = 1;

	protected synchronized long getCount() {
		return count++;
	}

	public String replaceValues(String template, MessageObject messageObject) {
		return replaceValues(template, messageObject, new String());
	}
	public String replaceValues(String template, MessageObject messageObject, String originalFilename){
		VelocityContext context = new VelocityContext();
		loadContext(context, messageObject, originalFilename);
		StringWriter writer = new StringWriter();

		try {
			Velocity.init();
			Velocity.evaluate(context, writer, "LOG", template);
		} catch (Exception e) {
			logger.warn("could not replace template values", e);
		}

		return writer.toString();
	}
	public String replaceValues(String template, Map values) {
		VelocityContext context = new VelocityContext();
		for (Iterator iter = values.entrySet().iterator(); iter.hasNext();) {
			Entry element = (Entry)iter.next();
			context.put(element.getKey().toString(), element.getValue());
		}
		StringWriter writer = new StringWriter();

		try {
			Velocity.init();
			Velocity.evaluate(context, writer, "LOG", template);
		} catch (Exception e) {
			logger.warn("could not replace template values", e);
		}
		return writer.toString();
	}
	public String replaceValues(String template, String originalFilename) {
		return replaceValues(template, null, originalFilename);
	}
	private void loadContext(VelocityContext context, MessageObject messageObject, String originalFilename) {
		// message variables
		if (messageObject != null) {
			context.put("message", messageObject);

			// load variables from global map
			// we don't use an iterator here because of concurrent modification
			// issues
			Map<String, Object> globalVariables = GlobalVariableStore.getInstance().getVariables();
			for (Iterator iter = globalVariables.entrySet().iterator(); iter.hasNext();) {
				Entry element = (Entry)iter.next();
				context.put(element.getKey().toString(), element.getValue());
			}
			
			// load variables from local map
			for (Iterator iter = messageObject.getConnectorMap().entrySet().iterator(); iter.hasNext();) {
				Entry entry = (Entry) iter.next();
				context.put(entry.getKey().toString(), entry.getValue());
			}
			// load variabls from the channelMap
			Map channelMap = messageObject.getChannelMap();
			Object[] channelKeys = {};
			channelKeys = channelMap.keySet().toArray(channelKeys);
			for (int i = 0; i < channelKeys.length; i++) {
				context.put(channelKeys[i].toString(),channelMap.get(channelKeys[i]));
			}
		}
			
		//we might have the originalfilename in the context
		if (context.get("originalFilename") != null){
			originalFilename = (String)context.get("originalFilename");
		} else if (originalFilename == null || originalFilename.length() == 0) {
			originalFilename = System.currentTimeMillis() + ".dat";
		}

		// system variables
		// Calendar today = Calendar.getInstance();
		context.put("date", new DateTool());
		context.put("DATE", Utility.getTimeStamp("dd-MM-yy_HH-mm-ss.SS"));
		context.put("FORMATTER", new VelocityFormatter(context));
		context.put("COUNT", String.valueOf(getCount()));
		context.put("UUID", (new UUID()).getUUID());
		context.put("SYSTIME", String.valueOf(System.currentTimeMillis()));
		context.put("ORIGINALNAME", originalFilename);
		context.put("encoder", new Entities());
	}
}
