package com.webreach.mirth.plugins.dicomviewer;

import ij.plugin.DICOM;

import java.awt.Dimension;
import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.util.List;

import org.apache.commons.codec.binary.Base64;

import com.webreach.mirth.model.MessageObject;
import com.webreach.mirth.plugins.AttachmentViewer;

public class DICOMViewer extends AttachmentViewer {

    public DICOMViewer(String name) {
        super(name);
    }

    public String getViewerType() {
        return "DICOM";
    }

    public boolean handleMultiple() {
        return true;
    }

    public void viewAttachments(List attachmentIds) {
        // do viewing code
        try {
            String messageId = parent.mirthClient.getAttachment((String) attachmentIds.get(0)).getMessageId();
            MessageObject message = parent.messageBrowser.getMessageObjectById(messageId);
            byte[] rawImage = new Base64().decode(parent.mirthClient.getDICOMMessage(message).getBytes());
            ByteArrayInputStream bis = new ByteArrayInputStream(rawImage);
            DICOM dcm = new DICOM(bis);
            dcm.run(message.getType());
            dcm.show();
            Dimension dlgSize = dcm.getWindow().getSize();
            Dimension frmSize = parent.getSize();
            Point loc = parent.getLocation();

            if ((frmSize.width == 0 && frmSize.height == 0) || (loc.x == 0 && loc.y == 0)) {
                dcm.getWindow().setLocationRelativeTo(null);
            } else {
                dcm.getWindow().setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
            }

        } catch (Exception e) {
            parent.alertException(parent, e.getStackTrace(), e.getMessage());
        }

    }
}
