package com.webreach.mirth.connectors.file.filesystems;

import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.UserInfo;

public class SftpConnection implements FileSystemConnection {

	public class SftpFileInfo implements FileInfo {

		ChannelSftp.LsEntry theFile;

		public SftpFileInfo(ChannelSftp.LsEntry theFile) {
			this.theFile = theFile;
		}

		public long getLastModified() {
			return theFile.getAttrs().getMTime();
		}

		public String getName() {
			return theFile.getFilename();
		}

		public long getSize() {
			return theFile.getAttrs().getSize();
		}

		public boolean isDirectory() {
			return theFile.getAttrs().isDir();
		}

		public boolean isFile() {
			SftpATTRS attrs = theFile.getAttrs();
			return !attrs.isDir() && !attrs.isLink();
		}

		public boolean isReadable() {
			return true;
			// return (theFile.getAttrs().getPermissions() & MASK) != 0;
		}
	}
	
	private static transient Log logger = LogFactory.getLog(SftpConnection.class);

	/** The JSch SFTP client instance */
	private ChannelSftp client = null;

	public SftpConnection(String host, int port, String username, String password) throws Exception {
		
		JSch jsch = new JSch();
		client = new ChannelSftp();

		try {
			Session session = null;

			if (port > 0) {
				session = jsch.getSession(username, host, port);
			} else {
				session = jsch.getSession(username, host);
			}

			UserInfo userInfo = new SftpUserInfo(password);
			session.setUserInfo(userInfo);
			session.connect();

			Channel channel = session.openChannel("sftp");
			channel.connect();
			client = (ChannelSftp) channel;
		}
		catch (Exception e) {
			
			if (client.isConnected()) {
				client.disconnect();
			}

			throw e;
		}
	}

	public List<FileInfo> listFiles(String fromDir, FilenameFilter filenameFilter)
		throws Exception
	{
		Vector entries = client.ls(".");
		List<FileInfo> files = new ArrayList<FileInfo>(entries.size());

		for (Iterator iter = entries.iterator(); iter.hasNext();) {
			ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) iter.next();

			if (!entry.getAttrs().isDir() && !entry.getAttrs().isLink()) {
				if ((filenameFilter == null) || (filenameFilter.accept(null, entry.getFilename()))) {
					files.add(new SftpFileInfo(entry));
				}
			}
		}

		return files;
	}

	public InputStream readFile(String file, String fromDir) throws Exception {
		client.cd(fromDir);
		return client.get(file);
	}

	public boolean canAppend() {

		return true;
	}
	
	public OutputStream writeFile(String file, String toDir, boolean append)
		throws Exception
	{
		cdmake(toDir);
		int mode = 0;
		if (append)
			mode = ChannelSftp.APPEND;
		else
			mode = ChannelSftp.OVERWRITE;
		return client.put(file, mode);
	}

	public void delete(String file, String fromDir) throws Exception {
		client.cd(fromDir);
		client.rm(file);
	}

	private void cdmake(String dir) throws Exception {
		
		try {
			client.cd(dir);
		}
		catch (Exception e) {
			String toDir = dir;
			if (toDir.startsWith("/")) {
				toDir = toDir.substring(1);
			}
			String[] dirs = toDir.split("/");
			if (dirs.length > 0) {
				for (int i = 0; i < dirs.length; i++) {
					try {
						client.cd(dirs[i]);
					} catch (Exception ex) {
						logger.debug("Making directory: " + dirs[i]);
						client.mkdir(dirs[i]);
						client.cd(dirs[i]);
					}
				}
			}
		}
	}

	public void move(String fromName, String fromDir, String toName, String toDir)
		throws Exception
	{
		// Create any missing directories in the toDir path
		cdmake(toDir);

		try {
			client.rm(toName);
		}
		catch (Exception e) {
			logger.info("Unable to delete destination file");
		}

		client.cd(client.getHome());
		client.cd(fromDir.substring(1) + "/"); // remove the first slash
		client.rename(fromName.replaceAll("//", "/"), (toDir + "/" + toName).replaceAll("//", "/"));
	}

	public boolean isConnected() {
		return client.isConnected();
	}

	// **************************************************
	// Lifecycle methods
	
	public void activate() {
		// Nothing
	}

	public void passivate() {
		// Nothing
	}

	public void destroy() {
		client.quit();
		client.disconnect();
	}

	public boolean isValid() {
		return client.isConnected();
	}
}
