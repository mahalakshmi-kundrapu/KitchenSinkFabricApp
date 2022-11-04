package com.kony.adminconsole.commons.tools;

import java.util.UUID;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.kony.adminconsole.commons.crypto.EncryptionUtils;

/**
 * Command Line Interface (CLI) utility for crypto operations
 * 
 * @author Venkateswara Rao Alla
 *
 */

public class EncryptionCLI {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public static void main(String[] args) {
        Options options = null;
        try {
            options = getCLIDefinitions();

            CommandLineParser commandLineParser = new DefaultParser();
            CommandLine commandLine = commandLineParser.parse(options, args);

            if (commandLine.getOptions().length == 0 || commandLine.hasOption("h")
                    || !(commandLine.hasOption("e") || commandLine.hasOption("d"))
                    || (commandLine.hasOption("e") && !commandLine.hasOption("t"))
                    || (commandLine.hasOption("d") && (!commandLine.hasOption("k") || !commandLine.hasOption("t")))) {
                System.err.println("Error. Invalid arguments.");
                printCLIHelp(options);
                System.exit(1);
            }

            if (commandLine.hasOption("e") && commandLine.hasOption("d")) {
                System.err.println("Error. Invalid mix of modes.");
                printCLIHelp(options);
                System.exit(1);
            }

            if (commandLine.hasOption("e")) {
                String secretKey = UUID.randomUUID().toString();
                if (!commandLine.hasOption("k")) {
                    System.out.println("Secret key not provided, defaulting on random UUID: " + secretKey);
                } else {
                    secretKey = commandLine.getOptionValue("k");
                }
                String encryptedText = EncryptionUtils.encrypt(commandLine.getOptionValue("t"), secretKey);
                System.out.println(encryptedText);
                System.exit(0);
            }

            if (commandLine.hasOption("d")) {
                String decryptedText = EncryptionUtils.decrypt(commandLine.getOptionValue("t"),
                        commandLine.getOptionValue("k"));
                System.out.println(decryptedText);
                System.exit(0);
            }

        } catch (ParseException pe) {
            System.err.println("Error. Invalid arguments.");
            printCLIHelp(options);
            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            printCLIHelp(options);
            System.exit(1);
        }

    }

    private static Options getCLIDefinitions() {

        Options options = new Options();

        options.addOption(Option.builder("h").longOpt("help").hasArg(false).desc("print help").build());

        options.addOption("e", "encrypt", false, "encrypt mode");
        options.addOption("d", "decrypt", false, "decrypt mode");
        options.addOption("k", "key", true, "secret key [optional with -e switch. Defaults to random UUID]");
        options.addOption("t", "text", true, "text to encrypt/decrypt");

        return options;
    }

    private static void printCLIHelp(Options options) {
        HelpFormatter helpFormatter = new HelpFormatter();

        helpFormatter.printHelp(
                LINE_SEPARATOR + "encrypt mode:" + LINE_SEPARATOR + " -e -k <secretKey> -t <plaintext>" + LINE_SEPARATOR
                        + "decrypt mode:" + LINE_SEPARATOR
                        + " -d -k <secretKey used for encryption> -t <encryptedText>",
                "Command line help of this utility. Utility used for encryption and decryption.", options,
                "------------------End of help-------------------", false);
    }

}
