/*
 * $Header: /home/projects/mule/scm/mule/providers/file/src/java/org/mule/providers/file/FileMessageReceiver.java,v 1.12 2005/11/12 20:55:57 lajos Exp $
 * $Revision: 1.12 $
 * $Date: 2005/11/12 20:55:57 $
 * ------------------------------------------------------------------------------------------------------
 *
 * Copyright (c) SymphonySoft Limited. All rights reserved.
 * http://www.symphonysoft.com
 *
 * The software in this package is published under the terms of the BSD
 * style license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 *
 */

package org.mule.providers.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import org.mule.MuleException;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;
import org.mule.impl.MuleMessage;
import org.mule.providers.ConnectException;
import org.mule.providers.PollingMessageReceiver;
import org.mule.umo.UMOComponent;
import org.mule.umo.UMOException;
import org.mule.umo.endpoint.UMOEndpoint;
import org.mule.umo.lifecycle.InitialisationException;
import org.mule.umo.provider.UMOConnector;
import org.mule.umo.provider.UMOMessageAdapter;
import org.mule.util.Utility;

/**
 * <code>FileMessageReceiver</code> is a polling listener that reads files
 * from a directory.
 * 
 * @author <a href="mailto:ross.mason@symphonysoft.com">Ross Mason</a>
 * @version $Revision: 1.12 $
 */

public class FileMessageReceiver extends PollingMessageReceiver {
	private static byte startOfMessage = (byte) 0x0B;
	private static byte endOfMessage = (byte) 0x1C;
	private static byte endOfRecord = (byte) 0x0D;
	private String readDir = null;
	private String moveDir = null;
	private File readDirectory = null;
	private File moveDirectory = null;
	private String moveToPattern = null;
	private FilenameFilter filenameFilter = null;

	public FileMessageReceiver(UMOConnector connector, UMOComponent component, UMOEndpoint endpoint, String readDir, String moveDir, String moveToPattern, Long frequency) throws InitialisationException {
		super(connector, component, endpoint, frequency);
		this.readDir = readDir;
		this.moveDir = moveDir;
		this.moveToPattern = moveToPattern;

		if (endpoint.getFilter() instanceof FilenameFilter) {
			filenameFilter = (FilenameFilter) endpoint.getFilter();
		}
	}

	public void doConnect() throws Exception {
		if (readDir != null) {
			readDirectory = Utility.openDirectory(readDir);
			if (!(readDirectory.canRead())) {
				throw new ConnectException(new Message(Messages.FILE_X_DOES_NOT_EXIST, readDirectory.getAbsolutePath()), this);
			} else {
				logger.debug("Listening on endpointUri: " + readDirectory.getAbsolutePath());
			}
		}
		if (moveDir != null) {
			moveDirectory = Utility.openDirectory((moveDir));
			if (!(moveDirectory.canRead()) || !moveDirectory.canWrite()) {
				throw new ConnectException(new Message("file", 5), this);
			}
		}
	}

	public void doDisconnect() throws Exception {}

	public void poll() {
		try {
			File[] files = listFiles();

			if (files == null) {
				return;
			}

			// sort files by specified attribute before sorting
			sortFiles(files);

			for (int i = 0; i < files.length; i++) {
				processFile(files[i]);
			}
		} catch (Exception e) {
			handleException(e);
		}
	}

	public void sortFiles(File[] files) {
		String sortAttribute = ((FileConnector) connector).getSortAttribute();

		if (sortAttribute.equals(FileConnector.SORT_DATE)) {
			Arrays.sort(files, new Comparator<File>() {
				public int compare(File file1, File file2) {
					return Float.compare(file1.lastModified(), file2.lastModified());
				}
			});
		} else if (sortAttribute.equals(FileConnector.SORT_SIZE)) {
			Arrays.sort(files, new Comparator<File>() {
				public int compare(File file1, File file2) {
					return Float.compare(file1.length(), file2.length());
				}
			});
		} else {
			Arrays.sort(files, new Comparator<File>() {
				public int compare(File file1, File file2) {
					return file1.compareTo(file2);
				}
			});
		}
	}

	public synchronized void processFile(File file) throws UMOException {
		boolean checkFileAge = ((FileConnector) connector).getCheckFileAge();
		if (checkFileAge) {
			long fileAge = ((FileConnector) connector).getFileAge();
			long lastMod = file.lastModified();
			long now = (new java.util.Date()).getTime();
			if ((now - lastMod) < fileAge)
				return;
		}

		File destinationFile = null;
		String orginalFilename = file.getName();
		UMOMessageAdapter adapter = connector.getMessageAdapter(file);
		adapter.setProperty(FileConnector.PROPERTY_ORIGINAL_FILENAME, orginalFilename);
		if (moveDir != null) {
			String fileName = file.getName();
			if (moveToPattern != null) {
				fileName = ((FileConnector) connector).getFilenameParser().getFilename(adapter, moveToPattern);
			}
			destinationFile = new File(moveDir, fileName);
		}
		boolean resultOfFileMoveOperation = false;
		try {
			// Perform some quick checks to make sure file can be processed
			if (file.isDirectory()) {
				// ignore directories
			} else if (!(file.canRead() && file.exists() && file.isFile())) {
				throw new MuleException(new Message(Messages.FILE_X_DOES_NOT_EXIST, file.getName()));
			} else {
				// Read in the file, parse it line by line
				// Vector fileContents = getFileLines(file);
				try {
					List<String> messages = loadMessages(file);

					for (Iterator iter = messages.iterator(); iter.hasNext();) {
						String message = (String) iter.next();
						routeMessage(new MuleMessage(connector.getMessageAdapter(message)), endpoint.isSynchronous());
					}
				} catch (Exception e) {
					throw new MuleException(new Message(Messages.FAILED_TO_READ_PAYLOAD, file.getName()));
				}
				// move the file if needed
				if (destinationFile != null) {
					try {
						destinationFile.delete();

					} catch (Exception e) {

					}

					resultOfFileMoveOperation = file.renameTo(destinationFile);
					if (!resultOfFileMoveOperation) {
						throw new MuleException(new Message("file", 4, file.getAbsolutePath(), destinationFile.getAbsolutePath()));
					}

				}

				if (((FileConnector) connector).isAutoDelete()) {
					adapter.getPayloadAsBytes();

					// no moveTo directory
					if (destinationFile == null) {
						resultOfFileMoveOperation = file.delete();
						if (!resultOfFileMoveOperation) {
							throw new MuleException(new Message("file", 3, file.getAbsolutePath()));
						}
					}
				}

			}
		} catch (Exception e) {
			boolean resultOfRollbackFileMove = false;
			if (resultOfFileMoveOperation) {
				resultOfRollbackFileMove = rollbackFileMove(destinationFile, file.getAbsolutePath());
			}
			Exception ex = new MuleException(new Message("file", 2, file.getName(), (resultOfRollbackFileMove ? "successful" : "unsuccessful")), e);
			handleException(ex);
		}
	}

	public Vector getFileLines(File aFile) {
		// ...checks on aFile are elided
		Vector contents = new Vector();

		// declared here only to make visible to finally clause
		BufferedReader input = null;

		try {
			// use buffering
			// this implementation reads one line at a time
			// FileReader always assumes default encoding is OK!
			input = new BufferedReader(new FileReader(aFile));
			String line = null; // not declared within while loop

			while ((line = input.readLine()) != null) {
				contents.add(line);
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
				if (input != null) {
					// flush and close both "input" and its underlying
					// FileReader
					input.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return contents;
	}

	private List<String> loadMessages(File file) throws FileNotFoundException {
		List<String> messages = new ArrayList<String>();
		StringBuilder message = new StringBuilder();
		Scanner scanner = new Scanner(file);
		char data[] = { (char) startOfMessage, (char) endOfMessage };

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine().replaceAll(new String(data, 0, 1), "").replaceAll(new String(data, 1, 1), "");

			if ((line.length() == 0) || line.equals((char) endOfMessage) || line.startsWith("MSH")) {
				if (message.length() > 0) {
					messages.add(message.toString());
					message = new StringBuilder();
				}

				while ((line.length() == 0) && scanner.hasNextLine()) {
					line = scanner.nextLine();
				}

				if (line.length() > 0) {
					message.append(line);
					message.append((char) endOfRecord);
				}
			} else {
				message.append(line);
				message.append((char) endOfRecord);

				if (!scanner.hasNextLine()) {
					messages.add(message.toString());
					message = new StringBuilder();
				}
			}
		}

		scanner.close();
		return messages;
	}

	/**
	 * Exception tolerant roll back method
	 */
	private boolean rollbackFileMove(File sourceFile, String destinationFilePath) {
		boolean result = false;
		try {
			result = sourceFile.renameTo(new File(destinationFilePath));
		} catch (Throwable t) {
			logger.debug("rollback of file move failed: " + t.getMessage());
		}
		return result;
	}

	/**
	 * Get a list of files to be processed.
	 * 
	 * @return a list of files to be processed.
	 * @throws org.mule.MuleException
	 *             which will wrap any other exceptions or errors.
	 */
	File[] listFiles() throws MuleException {
		File[] todoFiles = new File[0];
		try {
			todoFiles = readDirectory.listFiles(filenameFilter);
		} catch (Exception e) {
			throw new MuleException(new Message("file", 1), e);
		}
		return todoFiles;
	}

}
