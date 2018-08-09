package plugins.upload;

import models.Event;
import models.User;

import java.io.File;
import java.util.Date;

public class FileInfo {

    private FileDescription description;
    private File file;

    public FileInfo(Event event, User user, FileDescription description) {
        this(event, user.getId().toString(), description);
    }

    public FileInfo(Event event, String userId, FileDescription description) {
        this.description = description;
        File folderWithFile = description.getFolderWithFile(event, userId);

        File[] files = folderWithFile.listFiles();
        if (files == null)
            file = null;
        else if (files.length == 0)
            file = null;
        else
            file = files[0];
    }

    public FileDescription getDescription() {
        return description;
    }

    public Date getUploadDate() {
        return new Date(file.lastModified());
    }

    public boolean isUploaded() {
        return file != null;
    }

    public File getFile() {
        return file;
    }
}
