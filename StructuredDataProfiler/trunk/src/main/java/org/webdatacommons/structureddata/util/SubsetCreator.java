package org.webdatacommons.structureddata.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import org.webdatacommons.structureddata.model.Entity;
import org.webdatacommons.structureddata.model.EntityFileLoader;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;
import com.beust.jcommander.converters.FileConverter;

import de.dwslab.dwslib.framework.Processor;
import de.dwslab.dwslib.util.io.InputUtil;
import ldif.entity.NodeTrait;
import ldif.local.datasources.dump.QuadFileLoader;
import ldif.runtime.Quad;

/**
 * @author Anna Primpeli
 */
@Parameters(commandDescription = "Creates subsets of the original dataset, based on a given set of classes.")
public class SubsetCreator extends Processor<File> {

    @Parameter(names = {"-out",
            "-outputDir"}, required = true, description = "Folder where the outputfile(s) are written to.", converter = FileConverter.class)
    private File outputDirectory;

    @Parameter(names = {"-in",
            "-inputDir"}, required = true, description = "Folder where the input is read from.", converter = FileConverter.class)
    private File inputDirectory;

    @Parameter(names = {"-p",
            "-prefix"}, description = "Prefix of files in the input folder which will be processed.")
    private String filePrefix = "";

    @Parameter(names = "-threads", required = true, description = "Number of threads.")
    private Integer threads;

    @Parameter(names = {"-cff",
            "classFilterFile"}, required = true, description = "File containing the class names, class prefixes and number of entities per file.")
    private String classFilterFile = null;

    @Parameter(names = "-sep", description = "Separator for class filter file. (Default \t)")
    private String sep = "\t";

    @Parameter(names = "-global", description = "Defines if one global writer per class is used, or mutiple per class per input file per thread. (Defaul: false)")
    private boolean globalWriter = false;

    @Parameter(names = "-logerrors", description = "Defines if errors while processing files are logged and shown or not. (Defaul: false)")
    private boolean logErrors = false;

    private Map<String, BufferedWriter> writer = new HashMap<>();

    private Map<String, String> names = new HashMap<>();
    private int errorCount = 0;
    private int parsedLines = 0;

    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";

    private static final Logger LOGGER = Logger.getLogger(

            Thread.currentThread().getStackTrace()[0].getClassName()
    );

    @Override
    protected int getNumberOfThreads() {

        return this.threads;
    }

    @Override
    protected void beforeProcess() {

        try (BufferedReader br = InputUtil.getBufferedReader(new File(classFilterFile))) {

            while (br.ready()) {

                String line = br.readLine();
                String[] tok = line.split(sep);

                int buffer = 1024 * 8 * 1024;

                if (globalWriter) {

                    writer.put(tok[0].toLowerCase(),
                            new BufferedWriter(new OutputStreamWriter(
                                    new GZIPOutputStream(
                                            new FileOutputStream(new File(this.outputDirectory,
                                                    tok[1] + ".gz"))),
                                    StandardCharsets.UTF_8), buffer));

                } else {

                    names.put(tok[0].toLowerCase(), tok[1]);
                }
            }
        } catch (Exception e) {

            LOGGER.log(Level.SEVERE, "Could not read class filter file", e);
            System.exit(0);
        }
    }

    @Override
    protected List<File> fillListToProcess() {

        List<File> files = new ArrayList<>();
        Collections.sort(files);

        File[] inputDirectoryFiles = inputDirectory.listFiles();

        if (inputDirectoryFiles != null) {

            for (File f : inputDirectoryFiles) {

                if (!f.isDirectory()) {

                    if (filePrefix.length() > 0 && !f.getName().startsWith(filePrefix)) {

                        continue;
                    }

                    files.add(f);
                }
            }
        }

        return files;
    }

    @Override
    protected void process(File object) throws Exception {

        Map<String, BufferedWriter> writerLocal = null;

        if (!globalWriter) {

            writerLocal = new HashMap<>();

            // init thread based writers
            for (Entry<String, String> entry : this.names.entrySet()) {

                writerLocal.put(entry.getKey(),
                        new BufferedWriter(new OutputStreamWriter(
                                new GZIPOutputStream(
                                        new FileOutputStream(new File(this.outputDirectory,
                                                this.names.get(entry.getKey()) + "_" + object
                                                        .getName()
                                                        .replace(".gz", "") + ".gz"))),
                                StandardCharsets.UTF_8)));
            }
        }

        QuadFileLoader qfl = new QuadFileLoader();
        EntityFileLoader etl = new EntityFileLoader();
        BufferedReader br = InputUtil.getBufferedReader(object);
        String currentURL = "";
        NodeTrait currentSubject = null;
        List<Entity> entities = new ArrayList<>();
        List<Quad> quads = new ArrayList<>();

        while (br.ready()) {

            try {

                Quad q = qfl.parseQuadLine(br.readLine());
                parsedLines++;

                if (q.graph().equals(currentURL)) {

                    if (q.subject().equals(currentSubject)) {

                        quads.add(q);

                    } else {

                        // create entity
                        if (!quads.isEmpty()) {

                            Entity e = etl.loadEntityFromQuads(quads);
                            entities.add(e);
                        }

                        // clear list
                        quads.clear();
                        quads.add(q);
                        currentSubject = q.subject();
                    }

                } else {

                    // create entity
                    if (!quads.isEmpty()) {

                        Entity e = etl.loadEntityFromQuads(quads);
                        entities.add(e);
                    }

                    quads.clear();
                    quads.add(q);
                    currentSubject = q.subject();

                    // create entity
                    if (!entities.isEmpty()) {

                        if (!globalWriter) {

                            processEntities(entities, writerLocal);

                        } else {

                            processEntities(entities, writer);
                        }
                    }

                    // clear list
                    entities.clear();
                    currentURL = q.graph();
                }

            } catch (Exception e) {

                errorCount++;

                if (logErrors) {

                    LOGGER.log(Level.WARNING, e.getMessage(), e);
                }
            }
        }

        // one final time:
        // create entity
        if (!quads.isEmpty()) {

            Entity e = etl.loadEntityFromQuads(quads);
            entities.add(e);
        }

        // create entity
        if (!entities.isEmpty()) {

            if (!globalWriter) {

                processEntities(entities, writerLocal);

            } else {

                processEntities(entities, writer);
            }
        }

        br.close();

        if (!globalWriter) {

            for (BufferedWriter w : writerLocal.values()) {

                w.close();
            }
        }
    }

    private void processEntities(List<Entity> entities, Map<String, BufferedWriter> writer) {

        Set<String> types = new HashSet<>();

        for (Entity e : entities) {

            if (e.getType() != null) {

                String type = e.getType().value().toLowerCase();

                if (writer.containsKey(type)) {

                    types.add(type);

                } else if (type.contains(HTTP) &&
                        writer.containsKey(type.replace(HTTP, HTTPS))) {

                    types.add(type.replace(HTTP, HTTPS));

                } else if (type.contains(HTTPS) &&
                        writer.containsKey(type.replace(HTTPS, HTTP))) {

                    types.add(type.replace(HTTPS, HTTP));
                }
            }
        }

        for (String type : types) {

            for (Entity e : entities) {

                try {

                    writer.get(type).write(e.toLines());

                } catch (IOException e1) {

                    LOGGER.log(Level.SEVERE, e1.getMessage(), e1);
                }
            }
        }
    }

    @Override
    protected void afterProcess() {

        if (globalWriter) {

            for (Entry<String, BufferedWriter> entry : writer.entrySet()) {

                try {

                    writer.get(entry.getKey()).close();

                } catch (IOException e) {

                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }

        } else {

            // we need combine the data
            for (String s : names.values()) {

                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
                        new GZIPOutputStream(new FileOutputStream(
                                new File(this.outputDirectory, s + ".gz")))))) {

                    File[] outputDirectoryFiles = outputDirectory.listFiles();

                    if (outputDirectoryFiles != null) {

                        for (File f : outputDirectoryFiles) {

                            if (!f.isDirectory() &&
                                    filePrefix.length() > 0 &&
                                    f.getName().startsWith(s)) {

                                BufferedReader br = InputUtil.getBufferedReader(f);

                                while (br.ready()) {

                                    bw.write(br.readLine() + "\n");
                                }

                                br.close();

                                Files.delete(f.toPath());
                            }
                        }
                    }

                } catch (IOException e) {

                    LOGGER.log(Level.SEVERE, e.getMessage(), e);
                }
            }
        }

        LOGGER.log(Level.INFO, "Error Lines: {0}", errorCount);
        LOGGER.log(Level.INFO, "Parsed Lines: {0}", parsedLines);
    }

    public static void main(String[] args) {

        SubsetCreator cal = new SubsetCreator();

        try {

            new JCommander(cal, args);
            cal.process();

        } catch (ParameterException pe) {

            LOGGER.log(Level.SEVERE, pe.getMessage(), pe);
            new JCommander(cal).usage();
        }
    }

}
