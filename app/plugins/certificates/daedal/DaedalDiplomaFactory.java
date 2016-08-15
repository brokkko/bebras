package plugins.certificates.daedal;

import models.ServerConfiguration;
import models.User;
import models.newserialization.Deserializer;
import models.newserialization.SerializationTypesRegistry;
import models.newserialization.Serializer;
import plugins.certificates.Diploma;
import plugins.certificates.DiplomaFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Project: dces2
 * Created by ilya on 13.08.16, 20:58.
 */
public class DaedalDiplomaFactory extends DiplomaFactory {

    public static final String PLUGIN_NAME = "DaedalDiploma";

    private List<String> inlinedDefinition;
    private String importFile;
    private LinkedHashMap<String, String> vars;

    private String definition;

    @Override
    public Diploma getDiploma(User user) {
        return new DaedalDiploma(user, this);
    }

    public LinkedHashMap<String, String> getVars() {
        return vars;
    }

    public String getDefinition() {
        return definition;
    }

    @Override
    public void serialize(Serializer serializer) {
        super.serialize(serializer);
        if (inlinedDefinition != null)
            SerializationTypesRegistry.list(String.class).write(serializer, "definition", inlinedDefinition);
        else if (importFile != null)
            serializer.write("import", importFile);

        if (vars != null && !vars.isEmpty())
            SerializationTypesRegistry.map(String.class).write(serializer, "vars", vars);
    }

    @Override
    public void update(Deserializer deserializer) {
        super.update(deserializer);

        inlinedDefinition = SerializationTypesRegistry.list(String.class).read(deserializer, "definition");
        if (inlinedDefinition != null && inlinedDefinition.isEmpty())
            inlinedDefinition = null;

        importFile = deserializer.readString("import");
        vars = SerializationTypesRegistry.map(String.class).read(deserializer, "vars");

        updateDefinition();
    }

    private void updateDefinition() {
        if (inlinedDefinition != null)
            definition = inlinedDefinition.stream().collect(Collectors.joining("\n"));
        else if (importFile != null)
            try {
                definition = new String(Files.readAllBytes(
                        ServerConfiguration.getInstance().getPluginFile(PLUGIN_NAME, importFile).toPath()
                ));
            } catch (IOException e) {
                definition = errorDefinition("Error loading file: " + e.getMessage());
            }
        else
            definition = errorDefinition("No definition specified");
    }

    private String errorDefinition(String message) {
        return ""; /*TODO specify definition*/
    }
}
