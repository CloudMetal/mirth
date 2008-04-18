package com.webreach.mirth.plugins.serverlog;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ErrorCode;


/**
 * Created by IntelliJ IDEA. User: chrisr Date: Oct 18, 2007 Time: 4:10:46 PM To
 * change this template use File | Settings | File Templates.
 */
public class ArrayAppender extends AppenderSkeleton {
	private ServerLogProvider serverLogProvider;

	public ArrayAppender() {
		serverLogProvider = new ServerLogProvider();
	}

	protected void append(LoggingEvent loggingEvent) {
		if (this.layout == null) {
			errorHandler.error("No layout for appender " + name, null, ErrorCode.MISSING_LAYOUT);
			return;
		}

        // get the complete stack trace.
        String[] completeLogTrace = loggingEvent.getThrowableStrRep();
        StringBuffer logText = new StringBuffer();
        for (String aCompleteLogTrace : completeLogTrace) {
            logText.append(aCompleteLogTrace);
        }

        // pass the new log message to ServerLogProvider.
		serverLogProvider.newServerLogReceived(this.layout.format(loggingEvent) + logText.toString());
	}

	public boolean requiresLayout() {
		return true;
	}

	public void close() {
		// clean up and set the boolean 'closed' to true to indiciate that this
		// appender has been shut down.
		serverLogProvider = null;
		closed = true;
	}
}
