package controllers;

import controllers.actions.Authenticated;
import controllers.actions.DcesController;
import controllers.actions.LoadContest;
import controllers.actions.LoadEvent;
import models.Contest;
import models.Event;
import models.User;
import models.data.*;
import play.libs.Akka;
import play.libs.F;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.tables_list;
import views.html.view_table;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: ilya
 * Date: 19.09.13
 * Time: 17:37
 */
@LoadEvent
@Authenticated()
@DcesController
public class Tables extends Controller {

    public static <T> Result evalCsvTable(final String fileName, final TableDescription<T> tableDescription) {
        final Event currentEvent = Event.current();
        final User currentUser = User.current();

        F.Promise<byte[]> promiseOfVoid = Akka.future(
                new Callable<byte[]>() {
                    public byte[] call() throws Exception {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();

                        try (
                                ObjectsProvider<T> objectsProvider = tableDescription.getObjectsProviderFactory().get(currentEvent, currentUser);
                                ZipOutputStream zos = new ZipOutputStream(baos);
                                CsvDataWriter<T> dataWriter = new CsvDataWriter<>(tableDescription.getTable(), zos, "windows-1251", ';', '"')
                        ) {
                            zos.putNextEntry(new ZipEntry(fileName + ".csv"));
                            dataWriter.writeObjects(objectsProvider, new FeaturesContext(currentEvent, false));
                        }

                        return baos.toByteArray();
                    }
                }
        );

        return async(
                promiseOfVoid.map(
                        new F.Function<byte[], Result>() {
                            public Result apply(byte[] file) {
                                //TODO file name should be encode somehow http://stackoverflow.com/questions/93551/how-to-encode-the-filename-parameter-of-content-disposition-header-in-http
                                response().setHeader("Content-Disposition", "attachment; filename=" + fileName + ".zip");
                                return ok(file).as("application/zip");
                            }
                        }
                )
        );
    }

    @SuppressWarnings("unchecked")
    public static Result showTable(final String eventId, final Integer tableIndex) {
        List<TableDescription<?>> tables = User.current().getTables();
        if (tableIndex < 0 || tableIndex >= tables.size())
            return notFound();

        final TableDescription tableDescription = tables.get(tableIndex);

        final Event currentEvent = Event.current();
        final User currentUser = User.current();

        F.Promise<MemoryDataWriter> promiseOfVoid = Akka.future(
                new Callable<MemoryDataWriter>() {
                    public MemoryDataWriter call() throws Exception {

                        try (
                                ObjectsProvider objectsProvider = tableDescription.getObjectsProviderFactory().get(currentEvent, currentUser);
                                MemoryDataWriter dataWriter = new MemoryDataWriter(tableDescription.getTable())
                        ) {
                            dataWriter.writeObjects(objectsProvider, new FeaturesContext(currentEvent, true));

                            return dataWriter;
                        }
                    }
                }
        );

        return async(
                promiseOfVoid.map(
                        new F.Function<MemoryDataWriter, Result>() {
                            public Result apply(MemoryDataWriter dataWriter) {
                                return ok(view_table.render(tableDescription.getTable().getTitles(), dataWriter.getList(), tableIndex));
                            }
                        }
                )
        );
    }

    @SuppressWarnings("UnusedParameters")
    public static Result csvTable(final String eventId, final Integer tableIndex) throws Exception {
        TableDescription tableDescription = Event.current().getTable(tableIndex);

        if (tableDescription == null)
            return notFound("table not found");

        if (!User.currentRole().hasRight(tableDescription.getRight()))
            return forbidden();

        return evalCsvTable("table" + tableIndex + "-" + eventId, tableDescription);
    }

    @SuppressWarnings("UnusedParameters")
    @LoadContest
    public static Result csvTableForContest(final String eventId, final String contestId, final Integer tableIndex) throws Exception {
        TableDescription tableDescription = Contest.current().getTable(tableIndex);

        if (tableDescription == null)
            return notFound("table not found");

        return evalCsvTable("table" + tableIndex + "-" + eventId + "-" + contestId, tableDescription);
    }

    public static Result tablesList(String eventId) {
        int tablesCount = User.current().getTables().size();

        if (tablesCount == 0)
            return forbidden();

        if (tablesCount == 1)
            return redirect(routes.Tables.showTable(eventId, 0));

        return ok(tables_list.render(Event.current()));
    }

}
