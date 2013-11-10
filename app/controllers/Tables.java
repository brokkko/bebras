package controllers;

import controllers.actions.Authenticated;
import controllers.actions.DcesController;
import controllers.actions.LoadContest;
import controllers.actions.LoadEvent;
import models.Contest;
import models.Event;
import models.User;
import models.data.*;
import models.forms.RawForm;
import play.libs.Akka;
import play.libs.F;
import play.mvc.Call;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.tables_list;
import views.html.view_table;
import views.html.view_table_print;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
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

    public static <T> Result evalCsvTable(final String defaultFileName, final TableDescription<T> tableDescription, final Call currentCall) {
        final Event currentEvent = Event.current();
        final User currentUser = User.current();

        String fileName = tableDescription.getFilename();
        if (fileName == null)
            fileName = defaultFileName;
        final String finalFileName = fileName;

        final List<String> el = Collections.emptyList();
        final FeaturesContext context = new FeaturesContext(currentEvent, FeaturesContestType.CSV, currentCall);

        F.Promise<byte[]> promiseOfVoid = Akka.future(
                new Callable<byte[]>() {
                    public byte[] call() throws Exception {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();

                        try (
                                ObjectsProvider<T> objectsProvider = tableDescription.getObjectsProviderFactory().get(currentEvent, currentUser, el, el);
                                ZipOutputStream zos = new ZipOutputStream(baos);
                                CsvDataWriter<T> dataWriter = new CsvDataWriter<>(tableDescription.getTable(context), zos, "windows-1251", ';', '"')
                        ) {
                            zos.putNextEntry(new ZipEntry(finalFileName + ".csv"));
                            dataWriter.writeObjects(objectsProvider);
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
                                response().setHeader("Content-Disposition", "attachment; filename=" + finalFileName + ".zip");
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

        final ObjectsProviderFactory objectsProviderFactory = tableDescription.getObjectsProviderFactory();

        final List<String> searchFields = new ArrayList<>();
        final List<String> searchValues = new ArrayList<>();
        final List<String> allSearchValues;

        final List<String> allSearchFields = objectsProviderFactory.getFields();

        String fullTextSearch = null; //null means no full text search
        boolean inside = true;

        if (request().method().equals("POST")) {
            allSearchValues = new ArrayList<>();
            RawForm form = new RawForm();
            form.bindFromRequest();

            for (String searchField : allSearchFields) {
                String searchValue = form.get(searchField);
                allSearchValues.add(searchValue == null ? "" : searchValue);
                if (searchValue == null || searchValue.isEmpty())
                    continue;
                searchFields.add(searchField);
                searchValues.add(searchValue);
            }

            fullTextSearch = form.get("-full-text-search");
            inside = !"1".equals(form.get("-full-text-search-inside"));
            if ("".equals(fullTextSearch))
                fullTextSearch = null;
        } else
            allSearchValues = Collections.nCopies(allSearchFields.size(), "");

        final String finalFullTextSearch = fullTextSearch;
        final boolean finalInside = inside;

        final FeaturesContext context = new FeaturesContext(currentEvent, FeaturesContestType.INTERFACE, controllers.routes.Tables.showTable(eventId, tableIndex));
        final Table table = tableDescription.getTable(context);

        F.Promise<MemoryDataWriter> promiseOfVoid = Akka.future(
                new Callable<MemoryDataWriter>() {
                    public MemoryDataWriter call() throws Exception {

                        try (
                                ObjectsProvider objectsProvider = objectsProviderFactory.get(currentEvent, currentUser, searchFields, searchValues);
                                MemoryDataWriter dataWriter = new MemoryDataWriter(table, finalFullTextSearch, finalInside)
                        ) {
                            dataWriter.writeObjects(objectsProvider, context);

                            return dataWriter;
                        }
                    }
                }
        );

        return async(
                promiseOfVoid.map(
                        new F.Function<MemoryDataWriter, Result>() {
                            public Result apply(MemoryDataWriter dataWriter) {
                                return ok(view_table.render(
                                        tableDescription.getTitle(),
                                        tableDescription.isShowSearch(),
                                        tableDescription.getComment(),
                                        table.getTitles(), dataWriter.getList(), tableIndex, tableDescription.isShowAsTable(),
                                        objectsProviderFactory.getTitles(),
                                        objectsProviderFactory.getFields(),
                                        allSearchValues,
                                        finalFullTextSearch == null ? "" : finalFullTextSearch,
                                        finalInside
                                ));
                            }
                        }
                )
        );
    }

    @SuppressWarnings("unchecked")
    public static Result showTablePrint(final String eventId, final Integer tableIndex) {
        List<TableDescription<?>> tables = User.current().getTables();
        if (tableIndex < 0 || tableIndex >= tables.size())
            return notFound();

        final TableDescription tableDescription = tables.get(tableIndex);

        final Event currentEvent = Event.current();
        final User currentUser = User.current();

        final ObjectsProviderFactory objectsProviderFactory = tableDescription.getObjectsProviderFactory();

        final FeaturesContext context = new FeaturesContext(currentEvent, FeaturesContestType.PRINT, routes.Tables.showTablePrint(eventId, tableIndex));
        final Table table = tableDescription.getTable(context);

        F.Promise<MemoryDataWriter> promiseOfVoid = Akka.future(
                new Callable<MemoryDataWriter>() {
                    public MemoryDataWriter call() throws Exception {

                        try (
                                    ObjectsProvider objectsProvider = objectsProviderFactory.get(currentEvent, currentUser, null, null);
                                    MemoryDataWriter dataWriter = new MemoryDataWriter(table, null, false)
                        ) {
                            dataWriter.writeObjects(objectsProvider, context);

                            return dataWriter;
                        }
                    }
                }
        );

        return async(
                    promiseOfVoid.map(
                             new F.Function<MemoryDataWriter, Result>() {
                                 public Result apply(MemoryDataWriter dataWriter) {
                                     return ok(view_table_print.render(
                                                  tableDescription.getTitle(),
                                                  table.getTitles(),
                                                  dataWriter.getList(),
                                                  tableDescription.isShowAsTable()
                                     ));
                                 }
                             }
                    )
        );
    }

    public static Result showTableSearch(String eventId, Integer tableIndex) {
        return showTable(eventId, tableIndex);
    }

    @SuppressWarnings("UnusedParameters")
    public static Result csvTable(final String eventId, final Integer tableIndex) throws Exception {
        TableDescription tableDescription = User.current().getTables().get(tableIndex);

        if (tableDescription == null)
            return notFound("table not found");

        if (!User.currentRole().hasRight(tableDescription.getRight()))
            return forbidden();

        return evalCsvTable("table" + tableIndex + "-" + eventId, tableDescription, routes.Tables.csvTable(eventId, tableIndex));
    }

    @SuppressWarnings("UnusedParameters")
    @LoadContest
    public static Result csvTableForContest(final String eventId, final String contestId, final Integer tableIndex) throws Exception {
        TableDescription tableDescription = Contest.current().getTable(tableIndex);

        if (tableDescription == null)
            return notFound("table not found");

        return evalCsvTable(
                "table" + tableIndex + "-" + eventId + "-" + contestId,
                tableDescription,
                routes.Tables.csvTableForContest(eventId, contestId, tableIndex)
        );
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
