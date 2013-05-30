/*
 * Copyright (c) Mirth Corporation. All rights reserved.
 * http://www.mirthcorp.com
 * 
 * The software in this package is published under the terms of the MPL
 * license a copy of which has been included with this distribution in
 * the LICENSE.txt file.
 */

package com.mirth.connect.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.mirth.connect.donkey.model.channel.MetaDataColumn;
import com.mirth.connect.donkey.model.message.attachment.AttachmentHandlerProperties;
import com.mirth.connect.donkey.util.migration.Migratable;
import com.mirth.connect.model.attachments.AttachmentHandlerFactory;
import com.mirth.connect.model.attachments.AttachmentHandlerType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("channelProperties")
public class ChannelProperties implements Serializable, Migratable {
    private boolean clearGlobalChannelMap;
    private MessageStorageMode messageStorageMode;
    private boolean encryptData;
    private boolean removeContentOnCompletion;
    private boolean removeAttachmentsOnCompletion;
    private boolean initialStateStarted;
    private boolean storeAttachments;
    private Set<String> tags;
    private List<MetaDataColumn> metaDataColumns;
    private AttachmentHandlerProperties attachmentProperties;
    private Integer pruneMetaDataDays;
    private Integer pruneContentDays;
    private boolean archiveEnabled;

    public ChannelProperties() {
        clearGlobalChannelMap = true;
        messageStorageMode = MessageStorageMode.DEVELOPMENT;
        encryptData = false;
        initialStateStarted = true;
        tags = new LinkedHashSet<String>();
        metaDataColumns = new ArrayList<MetaDataColumn>();
        attachmentProperties = AttachmentHandlerFactory.getDefaults(AttachmentHandlerType.NONE);
        archiveEnabled = true;
    }

    public boolean isClearGlobalChannelMap() {
        return clearGlobalChannelMap;
    }

    public void setClearGlobalChannelMap(boolean clearGlobalChannelMap) {
        this.clearGlobalChannelMap = clearGlobalChannelMap;
    }

    public MessageStorageMode getMessageStorageMode() {
        return messageStorageMode;
    }

    public void setMessageStorageMode(MessageStorageMode messageStorageMode) {
        this.messageStorageMode = messageStorageMode;
    }

    public boolean isEncryptData() {
        return encryptData;
    }

    public void setEncryptData(boolean encryptData) {
        this.encryptData = encryptData;
    }

    public boolean isRemoveContentOnCompletion() {
        return removeContentOnCompletion;
    }

    public void setRemoveContentOnCompletion(boolean removeContentOnCompletion) {
        this.removeContentOnCompletion = removeContentOnCompletion;
    }

    public boolean isRemoveAttachmentsOnCompletion() {
        return removeAttachmentsOnCompletion;
    }

    public void setRemoveAttachmentsOnCompletion(boolean removeAttachmentsOnCompletion) {
        this.removeAttachmentsOnCompletion = removeAttachmentsOnCompletion;
    }

    public boolean isInitialStateStarted() {
        return initialStateStarted;
    }

    public void setInitialStateStarted(boolean initialStateStarted) {
        this.initialStateStarted = initialStateStarted;
    }

    public boolean isStoreAttachments() {
        return storeAttachments;
    }

    public void setStoreAttachments(boolean storeAttachments) {
        this.storeAttachments = storeAttachments;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public List<MetaDataColumn> getMetaDataColumns() {
        return metaDataColumns;
    }

    public void setMetaDataColumns(List<MetaDataColumn> metaDataColumns) {
        this.metaDataColumns = metaDataColumns;
    }

    public AttachmentHandlerProperties getAttachmentProperties() {
        return attachmentProperties;
    }

    public void setAttachmentProperties(AttachmentHandlerProperties attachmentProperties) {
        this.attachmentProperties = attachmentProperties;
    }

    public Integer getPruneMetaDataDays() {
        return pruneMetaDataDays;
    }

    public void setPruneMetaDataDays(Integer pruneMetaDataDays) {
        this.pruneMetaDataDays = pruneMetaDataDays;
    }

    public Integer getPruneContentDays() {
        return pruneContentDays;
    }

    public void setPruneContentDays(Integer pruneContentDays) {
        this.pruneContentDays = pruneContentDays;
    }

    public boolean isArchiveEnabled() {
        return archiveEnabled;
    }

    public void setArchiveEnabled(boolean archiveEnabled) {
        this.archiveEnabled = archiveEnabled;
    }
}
