package plugins.certificates.kio;

import models.User;
import models.newserialization.*;
import plugins.certificates.Diploma;
import plugins.certificates.DiplomaFactory;

import java.util.ArrayList;
import java.util.List;

public class KioCertificateFactory implements DiplomaFactory {

    private final ListSerializationType<KioProblemDescription> problemDescriptionsListType = SerializationTypesRegistry.list(new SerializableSerializationType<>(KioProblemDescription.class));
    private final ListSerializationType<List<Integer>> intListListType = SerializationTypesRegistry.list(SerializationTypesRegistry.list(int.class));

    private String contestId; //TODO implement: if null then get result from event result
    private List<List<Integer>> problemsByLevels;
    private List<Integer> participantsByLevels = new ArrayList<>();
    private List<KioProblemDescription> problems;
    private int year;

    @Override
    public Diploma getCertificate(User user) {
        return new KioCertificate(user, this);
    }

    public String getContestId() {
        return contestId;
    }

    public List<List<Integer>> getProblemsByLevels() {
        return problemsByLevels;
    }

    public List<Integer> getParticipantsByLevels() {
        return participantsByLevels;
    }

    public List<KioProblemDescription> getProblems() {
        return problems;
    }

    public int getYear() {
        return year;
    }

    @Override
    public void serialize(Serializer serializer) {
        serializer.write("contest", contestId);
        intListListType.write(serializer, "problems by levels", problemsByLevels);
        SerializationTypesRegistry.list(int.class).write(serializer, "participants by levels", participantsByLevels);
        problemDescriptionsListType.write(serializer, "problems", problems);
        serializer.write("year", year);
    }

    @Override
    public void update(Deserializer deserializer) {
        contestId = deserializer.readString("contest");
        problemsByLevels = intListListType.read(deserializer, "problems by levels");
        participantsByLevels = SerializationTypesRegistry.list(int.class).read(deserializer, "participants by levels");
        problems = problemDescriptionsListType.read(deserializer, "problems");
        year = deserializer.readInt("year", 2014);
    }
}