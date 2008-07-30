package com.webreach.mirth.connectors.file.filesystems;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.mule.MuleException;
import org.mule.config.i18n.Message;
import org.mule.config.i18n.Messages;

import com.webreach.mirth.connectors.file.filters.FilenameWildcardFilter;

/** The FileSystemConnection class for local files
 * 
 * @author Erik Horstkotte
 *
 */
public class FileConnection implements FileSystemConnection {

	public class FileFileInfo implements FileInfo {

		private File theFile;
		
		public FileFileInfo(File theFile) {
			this.theFile = theFile;
		}

		/** Gets the name of the file relative to the folder searched */
		public String getName() {
			
			return this.theFile.getName();
		}
		
		/** Gets the absolute pathname of the file */
		public String getAbsolutePath() {
			
			return this.theFile.getAbsolutePath();
		}
		
		/** Gets the absolute pathname of the directory holding the file */
		public String getParent() {
			
			return this.theFile.getParent();
		}
		
		/** Gets the size of the file in bytes */
		public long getSize() {
			
			return this.theFile.length();
		}
		
		/** Gets the date and time the file was last modified, in milliseconds since the epoch */
		public long getLastModified() {
			
			return this.theFile.lastModified();
		}
		
		/** Tests if the file is a directory */
		public boolean isDirectory() {
			
			return this.theFile.isDirectory();
			
		}
		
		/** Tests if the file is a plain file */
		public boolean isFile() {
			return this.theFile.isFile();
		}

		/** Tests if the file exists and is readable */
		public boolean isReadable() {
			
			return this.theFile.canRead();
		}
	}

	public FileConnection() {
		
		// That was easy
	}

	public List<FileInfo> listFiles(String fromDir, String filenamePattern)
		throws Exception
	{
	    FilenameFilter filenameFilter = new FilenameWildcardFilter(filenamePattern);	    
		File readDirectory = null;
		try {
			readDirectory = new File(fromDir);
		}
		catch (Exception e) {
			throw new MuleException(new Message(Messages.FILE_X_DOES_NOT_EXIST, fromDir), e);
		}

		try {
			File[] todoFiles = readDirectory.listFiles(filenameFilter);
			if (todoFiles == null) {

				return new ArrayList<FileInfo>();
			}
			else {
				List<FileInfo> result = new ArrayList<FileInfo>(todoFiles.length);
				for (File f : todoFiles) {
					result.add(new FileFileInfo(f));
				}
			return result;
			}
		}
		catch (Exception e) {
			throw new MuleException(new Message("file", 1), e);
		}
	}

	public InputStream readFile(String file, String fromDir)
		throws MuleException
	{
		try {
			File src = new File(fromDir, file);
			return new FileInputStream(src);
		}
		catch (Exception e) {
			throw new MuleException(new Message("file", 1), e);
		}
	}

	/** Must be called after readFile when reading is complete */
	public void closeReadFile() throws Exception {
		// nothing
	}

	public boolean canAppend() {

		return true;
	}
	
	public void writeFile(String file, String toDir, boolean append, byte[] message)
		throws Exception
	{
		OutputStream os = null;
		File dst = new File(toDir, file);
		try {
			os = new FileOutputStream(dst, append);
			os.write(message);
		}
		finally {
			if (os != null) {
				os.close();
			}
		}
	}

	public void delete(String file, String fromDir, boolean mayNotExist)
		throws MuleException
	{
		File src = new File(fromDir, file);

		if (!src.delete()) {
			
			if (!mayNotExist) {
				throw new MuleException(new Message("file", 3, src.getAbsolutePath()));
			}
		}
	}

	public void move(String fromName, String fromDir,
					 String toName, String toDir)
		throws MuleException
	{
		File src = new File(fromDir, fromName);
		File dst = new File(toDir, toName);

		dst.delete();

		// File.renameTo operation doesn't work across file systems. So we will
		// attempt to do a File.renameTo for efficiency and atomicity, if this
		// fails then we will use the Commons-IO moveFile operation which
		// does a "copy and delete"
		if (!src.renameTo(dst)) {
		    try {
		        FileUtils.moveFile(src, dst);    
		    } catch (IOException e) {
		        throw new MuleException(new Message("file", 4, src.getAbsolutePath(), dst.getAbsolutePath()), e);    
		    }
		}
	}

	public boolean isConnected() {
		return true;
	}

	public void activate() {
	}

	public void passivate() {
	}

	public void destroy() {
	}

	public boolean isValid() {
		return true;
	}
}