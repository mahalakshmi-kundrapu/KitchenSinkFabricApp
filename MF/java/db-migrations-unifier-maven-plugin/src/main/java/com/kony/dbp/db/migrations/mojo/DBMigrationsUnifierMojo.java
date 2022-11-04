package com.kony.dbp.db.migrations.mojo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "unify-db-migrations", defaultPhase = LifecyclePhase.PROCESS_RESOURCES, threadSafe = true)
public class DBMigrationsUnifierMojo extends AbstractMojo {

    private static final String SYSTEM_LINE_SEPERATOR = System.getProperty("line.separator");

    /** Instance of maven logger injected by plexus IOC */
    private Log log;

    @Parameter(required = true)
    private File scriptsDir;

    @Parameter(readonly = true, defaultValue = "${project.build.outputDirectory}/flyway-migrations")
    private File destDir;

    @Override
    public void setLog(Log log) {
        this.log = log;
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

        if (scriptsDir == null || !scriptsDir.exists() || !scriptsDir.isDirectory()) {
            throw new MojoFailureException(
                    "Path to scripts directory doesn't exists or is not a directory");
        }
        log.info("Generating migration scripts using scripts from directory: [" + scriptsDir.getAbsolutePath()
                + "]");

        // Clean up the previous generated migrations
        if (destDir.exists()) {
            log.info("Trying to Cleaning up destination directory: [" + destDir.getAbsolutePath()
                    + "] as it already exists.");
            if (!FileUtils.deleteQuietly(destDir)) {
                throw new MojoFailureException(
                        "Unable to cleanup following directory: [" + destDir.getAbsolutePath() +
                                "]. Re-run the plugin by manually deleting the directory.");
            }
        }

        // generation of migrations starts here
        generateDBMigrations();

        log.info("Successfully completed unifying database migrations");
    }

    private void generateDBMigrations() throws MojoFailureException {
        for (File dbTypeDir : scriptsDir.listFiles()) {
            if (dbTypeDir.isDirectory()) {
                log.info("Start processing of unifying migrations for database: [" + dbTypeDir.getName() + "]");

                File[] versionDirs = dbTypeDir.listFiles();
                for (File versionDir : versionDirs) {
                    // if versionDir is a directory, merge migrations
                    if (versionDir.isDirectory()) {
                        unifyMigrationsOfVersionDir(dbTypeDir, versionDir);
                    }

                    // if versionDir is an sql file, copy the sql file destination as-is. This functionality is to
                    // support older migration files.
                    if (versionDir.isFile() && versionDir.getName().endsWith(".sql")) {
                        copyMigrationFile(dbTypeDir, versionDir);
                    }
                }
                log.info("End of unifying migrations for database: [" + dbTypeDir.getName() + "]");
            }
        }

    }

    private void unifyMigrationsOfVersionDir(File dbTypeDir, File versionDir)
            throws MojoFailureException {
        log.info("Start unifying migrations for version: [" + versionDir.getName() + "]");
        File ddlDBPMigration = new File(versionDir, "ddl_dbp.sql");
        File dmlDBPMigration = new File(versionDir, "dml_dbp.sql");
        File ddlAdminMigration = new File(versionDir, "ddl_admin.sql");
        File dmlAdminMigration = new File(versionDir, "dml_admin.sql");
        File ddlLoansMigration = new File(versionDir, "ddl_loans.sql");
        File dmlLoansMigration = new File(versionDir, "dml_loans.sql");

        List<File> migrations = new ArrayList<>(); // list for preserving the order of sql files
        
        if (ddlDBPMigration.exists() && ddlDBPMigration.isFile()) {
            migrations.add(ddlDBPMigration);
        }

        if (dmlDBPMigration.exists() && dmlDBPMigration.isFile()) {
            migrations.add(dmlDBPMigration);
        }
        
        if (ddlAdminMigration.exists() && ddlAdminMigration.isFile()) {
            migrations.add(ddlAdminMigration);
        }

        if (dmlAdminMigration.exists() && dmlAdminMigration.isFile()) {
            migrations.add(dmlAdminMigration);
        }

        if (ddlLoansMigration.exists() && ddlLoansMigration.isFile()) {
            migrations.add(ddlLoansMigration);
        }

        if (dmlLoansMigration.exists() && dmlLoansMigration.isFile()) {
            migrations.add(dmlLoansMigration);
        }

        String unifiedMigrationFileName = "V" + versionDir.getName() + "__migration.sql";
        File unifiedMigrationVersionDBDir = new File(destDir, dbTypeDir.getName());
        if (!unifiedMigrationVersionDBDir.exists()) {
            unifiedMigrationVersionDBDir.mkdirs();
        }

        // unify version migrations
        if (migrations != null && !migrations.isEmpty()) {
            try (OutputStreamWriter fileWriter =
                    new OutputStreamWriter(new FileOutputStream(new File(unifiedMigrationVersionDBDir, unifiedMigrationFileName), true), 
                    		StandardCharsets.UTF_8)) {
                for (File migration : migrations) {
                    try (FileInputStream fileInputStream = new FileInputStream(migration)) {
                        log.info("Unifying scripts of : [" + migration.getName() + "]" + " in migration: ["
                                + unifiedMigrationFileName);
                        fileWriter.append("-- " + migration.getName() + " starts")
                                .append(SYSTEM_LINE_SEPERATOR);
                        IOUtils.copy(fileInputStream, fileWriter, StandardCharsets.UTF_8);
                        fileWriter.append(SYSTEM_LINE_SEPERATOR).append(SYSTEM_LINE_SEPERATOR);
                    }
                }
            } catch (Exception e) {
                log.error("Failure while unifying migrations for version: [" + versionDir.getName() + "]");
                throw new MojoFailureException("Process failed while unifying migrations", e);
            }
        }
        log.info("End of unifying migrations for version: [" + versionDir.getName() + "]");

    }

    private void copyMigrationFile(File dbTypeDir, File migrationFile)
            throws MojoFailureException {
        log.info("Start copying migration file: [" + migrationFile.getName() + "]");
        File unifiedMigrationVersionDBDir = new File(destDir, dbTypeDir.getName());
        if (!unifiedMigrationVersionDBDir.exists()) {
            unifiedMigrationVersionDBDir.mkdirs();
        }
        try (OutputStreamWriter fileWriter =
                new OutputStreamWriter(new FileOutputStream(new File(unifiedMigrationVersionDBDir, migrationFile.getName())), 
                		StandardCharsets.UTF_8)) {
            try (FileInputStream fileInputStream = new FileInputStream(migrationFile)) {
                IOUtils.copy(fileInputStream, fileWriter, StandardCharsets.UTF_8);
            }
        } catch (Exception e) {
            log.error("Failure while copying migration file: [" + migrationFile.getName() + "]");
            throw new MojoFailureException("Process failed while generating migrations", e);
        }
        log.info("End of copying migration file: [" + migrationFile.getName() + "]");
    }

}
