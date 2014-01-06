package com.zyeeda.coala.commons.hibernate.listeners;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.persistence.PostRemove;

import com.zyeeda.coala.commons.resource.entity.Attachment;

/**
 * @author guyong
 *
 */
public class DeleteAttachmentFileEventListener {

    private String path = null;
    
    public DeleteAttachmentFileEventListener() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("settings/coala.properties"));
            this.path = (String)properties.get("coala.upload.path");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @PostRemove
    public void onPostDelete(Attachment atta) {
        File file = new File(path, atta.getPath());
        if (file.exists()) {
            file.delete();
        }
    }

    
}
