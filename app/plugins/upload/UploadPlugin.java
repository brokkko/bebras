package plugins.upload;

import models.Event;
import models.User;
import models.newserialization.*;
import org.bson.types.ObjectId;
import play.core.j.JavaResults;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Result;
import play.mvc.Results;
import plugins.Plugin;
import views.Menu;
import views.html.error;
import views.html.upload.uploader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static play.mvc.Controller.request;
import static play.mvc.Results.*;

public class UploadPlugin extends Plugin {

    private List<FileDescription> files = Collections.emptyList();
    private String menuItemTitle;
    private String pageTitle;
    private String uploadRight;
    private String downloadRight;

    @Override
    public void initPage() {
        Menu.addMenuItem(menuItemTitle, getCall(), uploadRight);
    }

    @Override
    public void initEvent(Event event) {
        //do nothing
    }

    @Override
    public F.Promise<Result> doGet(String action, String params) {
        switch (action) {
            case "go": return getUploadPage();
            case "download":
                return doDownload(params);
        }

        return F.Promise.pure(notFound());
    }

    private F.Promise<Result> getUploadPage() {
        if (!User.currentRole().hasRight(uploadRight))
            return F.Promise.pure(Results.forbidden());

        Event event = Event.current();
        User user = User.current();

        List<FileInfo> fileInfos = files.stream()
                .map(fd -> new FileInfo(event, user, fd))
                .collect(Collectors.toList());

        return F.Promise.pure(
                ok(uploader.render(this, fileInfos, pageTitle))
        );
    }

    private F.Promise<Result> doDownload(String params) {
        String[] arrayParams = params.split("/");
        if (arrayParams.length == 0)
            return F.Promise.pure(badRequest());

        String fileId = arrayParams[0];

        User fileOwnerUser;

        boolean downloadForOtherUser = arrayParams.length == 2;
        if (downloadForOtherUser) {
            String userId = arrayParams[1];
            try {
                fileOwnerUser = User.getUserById(new ObjectId(userId));
            } catch (Exception e) {
                return F.Promise.pure(badRequest());
            }

            User adminUser = User.current();
            if (adminUser == null || !adminUser.hasRight(downloadRight) || (!adminUser.isUpper(fileOwnerUser) && !adminUser.hasEventAdminRight()))
                return F.Promise.pure(forbidden());
        } else //download for self
            fileOwnerUser = User.current();

        FileDescription fd = searchFileDescription(fileId);
        if (fd == null)
            return F.Promise.pure(notFound());


        FileInfo fileInfo = new FileInfo(Event.current(), fileOwnerUser, fd);

        if (!fileInfo.isUploaded())
            return F.Promise.pure(notFound());

        return F.Promise.pure(
                ok(fileInfo.getFile())
        );
    }

    @Override
    public F.Promise<Result> doPost(String action, String params) {
        if (!User.currentRole().hasRight(uploadRight))
            return F.Promise.pure(Results.forbidden());

        //noinspection UnnecessaryLocalVariable
        String id = params;
        FileDescription fd = searchFileDescription(id);
        if (fd == null)
            return F.Promise.pure(notFound());

        User user = User.current();
        Event event = Event.current();

        FileInfo fi = new FileInfo(event, user, fd);
        //try to remove file if it is uploaded
        if (fi.isUploaded())
            if (!fi.getFile().delete())
                return F.Promise.pure(internalServerError("failed to remove previously uploaded file, try again."));

        //now upload the file
        Http.MultipartFormData body = request().body().asMultipartFormData();
        Http.MultipartFormData.FilePart uploadedFile = body.getFile("user-file");
        if (uploadedFile == null)
            return F.Promise.pure(badRequest(error.render("Не выбран файл для загрузки", new String[]{}))); //TODO show this info better

        File uploadFolder = fd.getFolderWithFile(event, user);
        uploadFolder.mkdirs();
        File destFile = new File(uploadFolder, uploadedFile.getFilename());
        Path destPath = destFile.toPath();

        try {
            Files.move(
                    uploadedFile.getFile().toPath(),
                    destPath,
                    StandardCopyOption.REPLACE_EXISTING
            );
        } catch (IOException e) {
            return F.Promise.pure(internalServerError("failed to move file to destination"));
        }

        return F.Promise.pure(redirect(getCall()));
    }

    private FileDescription searchFileDescription(String id) {
        for (FileDescription file : files) {
            if (file.getId().equals(id))
                return file;
        }
        return null;
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        SerializationTypesRegistry.list(new SerializableSerializationType<>(FileDescription.class)).write(serializer, "files", files);
        serializer.write("menu title", menuItemTitle);
        serializer.write("page title", pageTitle);
        serializer.write("upload right", uploadRight);
        serializer.write("download right", downloadRight);
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);
        files = SerializationTypesRegistry.list(new SerializableSerializationType<>(FileDescription.class)).read(deserializer, "files");
        menuItemTitle = deserializer.readString("menu title", "Загрузка файлов");
        pageTitle = deserializer.readString("page title", "Загрузка файлов");
        uploadRight = deserializer.readString("upload right");
        downloadRight = deserializer.readString("download right", "event admin");
    }
}
