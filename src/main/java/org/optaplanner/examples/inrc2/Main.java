package org.optaplanner.examples.inrc2;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;
import org.optaplanner.core.api.solver.SolverFactory;
import org.optaplanner.core.config.solver.random.RandomType;
import org.optaplanner.core.impl.solver.DefaultSolver;
import org.optaplanner.core.impl.solver.random.DefaultRandomFactory;
import org.optaplanner.examples.inrc2.domain.Roster;
import org.optaplanner.examples.inrc2.io.Inrc2SolutionFileIO;
import org.optaplanner.persistence.common.api.domain.solution.SolutionFileIO;

public class Main {

    private static final Option OPTION_CUSTOM_INPUT_FILE = new Option("cusIn", true, "Custom input file");
    private static final Option OPTION_CUSTOM_OUTPUT_FILE = new Option("cusOut", true, "Custom output file");
    private static final Option OPTION_INITIAL_HISTORY_FILE = new Option("his", true, "Initial history file");
    private static final Option OPTION_RANDOM_SEED = new Option("rand", true, "Random seed");
    private static final Option OPTION_SCENARIO_FILE = new Option("sce", true, "Scenario file");
    private static final Option OPTION_SOLUTION_FILE = new Option("sol", true, "Solution file");
    private static final Option OPTION_WEEK_DATA_FILE = new Option("week", true, "Week data file");

    private static File getFile(final CommandLine line, final Option o) {
        return Main.getFile(line, o, true);
    }

    private static File getFile(final CommandLine line, final Option o, final boolean required) {
        if (line.hasOption(o.getOpt())) {
            return new File(line.getOptionValue(o.getOpt()));
        } else {
            return null;
        }
    }

    public static void main(final String[] args) {
        // prepare command-line
        final Options o = new Options();
        Main.OPTION_SCENARIO_FILE.setRequired(true);
        Main.OPTION_INITIAL_HISTORY_FILE.setRequired(true);
        Main.OPTION_WEEK_DATA_FILE.setRequired(true);
        Main.OPTION_SOLUTION_FILE.setRequired(true);
        o.addOption(Main.OPTION_SCENARIO_FILE);
        o.addOption(Main.OPTION_INITIAL_HISTORY_FILE);
        o.addOption(Main.OPTION_WEEK_DATA_FILE);
        o.addOption(Main.OPTION_SOLUTION_FILE);
        o.addOption(Main.OPTION_CUSTOM_INPUT_FILE);
        o.addOption(Main.OPTION_CUSTOM_OUTPUT_FILE);
        o.addOption(Main.OPTION_RANDOM_SEED);

        // parse options
        CommandLine line = null;
        try {
            final CommandLineParser parser = new GnuParser();
            line = parser.parse(o, args, true);
        } catch (final ParseException e) {
            System.out.println("Invalid invocation: " + e.getMessage());
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("java -jar optaplanner-delirum.jar -server", o);
            System.exit(1);
        }

        // read provided values
        final File scenario = Main.getFile(line, Main.OPTION_SCENARIO_FILE);
        final File history = Main.getFile(line, Main.OPTION_INITIAL_HISTORY_FILE);
        final File week = Main.getFile(line, Main.OPTION_WEEK_DATA_FILE);
        final File solution = Main.getFile(line, Main.OPTION_SOLUTION_FILE);
        final File customInput = Main.getFile(line, Main.OPTION_CUSTOM_INPUT_FILE);
        final File customOutput = Main.getFile(line, Main.OPTION_CUSTOM_OUTPUT_FILE);
        final long seed = Integer.valueOf(line.getOptionValue(Main.OPTION_RANDOM_SEED.getOpt(), "0"));
        if (!scenario.exists() || !history.exists() || !week.exists()) {
            throw new IllegalStateException("Some of the input files do not exist.");
        }

        // prepare files for the solver
        final File tempFolder = FileUtils.getTempDirectory();
        final File plannerTempFolder = new File(tempFolder, "inrc2-" + new Random().nextInt(Integer.MAX_VALUE));
        try {
            FileUtils.forceMkdir(plannerTempFolder);
            FileUtils.copyFile(scenario, new File(plannerTempFolder, Inrc2SolutionFileIO.SCENARIO_FILENAME));
            FileUtils.copyFile(history, new File(plannerTempFolder, Inrc2SolutionFileIO.HISTORY_FILENAME));
            FileUtils.copyFile(week, new File(plannerTempFolder, Inrc2SolutionFileIO.WEEK_DATA_FILENAME));
            FileUtils.copyFile(customInput, new File(plannerTempFolder, Inrc2SolutionFileIO.CUSTOM_INPUT_FILENAME));
        } catch (final IOException e) {
            throw new IllegalStateException("Failed preparing environment for the solver.", e);
        }

        // kick off the solver
        final SolutionFileIO fileIo = new Inrc2SolutionFileIO();
        final Roster roster = (Roster) fileIo.read(plannerTempFolder);
        final SolverFactory solverFactory = SolverFactory.createFromXmlResource("org/optaplanner/examples/inrc2/solver/inrc2SolverConfig.xml");
        final DefaultSolver solver = (DefaultSolver) solverFactory.buildSolver();
        solver.setRandomFactory(new DefaultRandomFactory(RandomType.JDK, seed));
        solver.solve(roster);
        final Roster bestSolution = (Roster) solver.getBestSolution();
        System.out.println("Score achieved: " + bestSolution.getScore());

        // write the solution
        final File tempSolutionFile = new File(plannerTempFolder, Inrc2SolutionFileIO.SOLUTION_FILENAME);
        fileIo.write(bestSolution, tempSolutionFile);
        try {
            FileUtils.copyFile(tempSolutionFile, solution);
            FileUtils.copyFile(new File(plannerTempFolder, Inrc2SolutionFileIO.CUSTOM_OUTPUT_FILENAME), customOutput);
        } catch (final IOException e) {
            throw new IllegalStateException("Failed storing result of the solver.", e);
        }
    }

}
