package com.webreach.mirth.connectors.file;

import com.webreach.mirth.connectors.ConnectorService;
import com.webreach.mirth.connectors.file.filesystems.FileSystemConnection;
import com.webreach.mirth.connectors.file.filesystems.FileSystemConnectionFactory;
import com.webreach.mirth.util.ConnectionTestResponse;

import java.util.Map;
import java.net.URI;

public class FileWriterService implements ConnectorService {
    public Object invoke(String method, Object object, String sessionsId) throws Exception {
        if (method.equals("testWrite")) {
            Map<String, String> params = (Map<String, String>) object;
            String fileHost = null;
            String scheme = params.get(FileWriterProperties.FILE_SCHEME);
            String host = null;
            int port = 0;
            String dir = null;

            String username = params.get(FileWriterProperties.FILE_USERNAME);
            String password = params.get(FileWriterProperties.FILE_PASSWORD);
	        boolean secure = false;
            boolean passive = false;

            if (scheme.equals(FileWriterProperties.SCHEME_FTP) || scheme.equals(FileWriterProperties.SCHEME_SFTP)) {
	            if ((params.get(FileWriterProperties.FILE_PASSIVE_MODE)).equals("1")) {
		            passive = true;
	            }
            }

            if (scheme.equals(FileWriterProperties.SCHEME_FILE)) {
                fileHost = params.get(FileWriterProperties.FILE_HOST);
                dir = params.get(FileWriterProperties.FILE_HOST);
            } else {
	            URI address;
	            if (scheme.equals(FileWriterProperties.SCHEME_WEBDAV)) {
		            if (params.get(FileWriterProperties.FILE_SECURE_MODE).equals("1")) {
			            secure = true;
			            address = new URI("https://" + params.get(FileWriterProperties.FILE_HOST));
		            } else {
			            address = new URI("http://" + params.get(FileWriterProperties.FILE_HOST));
		            }
	            } else {
		            address = new URI(scheme + "://" + params.get(FileWriterProperties.FILE_HOST));
	            }
	            
                fileHost = address.toString();
                host = address.getHost();
                port = address.getPort();
                dir = address.getPath();
            }

            FileSystemConnectionFactory factory = new FileSystemConnectionFactory(scheme, username, password, host, port, passive, secure);

            try {
                FileSystemConnection connection = (FileSystemConnection) factory.makeObject();

                if (connection.canWrite(dir)) {
                    return new ConnectionTestResponse(ConnectionTestResponse.Type.SUCCESS, "Sucessfully connected to: " + fileHost);
                } else {
                    return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, "Unable to connect to: " + fileHost);
                }
            } catch (Exception e) {
                return new ConnectionTestResponse(ConnectionTestResponse.Type.FAILURE, "Unable to connect to: " + fileHost);
            }
        }

        return null;
    }
}
