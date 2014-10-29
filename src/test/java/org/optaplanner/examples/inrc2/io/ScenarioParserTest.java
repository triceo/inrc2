package org.optaplanner.examples.inrc2.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.optaplanner.examples.inrc2.domain.Scenario;

import com.fasterxml.jackson.core.JsonProcessingException;

@RunWith(Parameterized.class)
public class ScenarioParserTest {

    @Parameters(name = "{0}")
    public static Iterable<Object[]> getScenarios() {
        final List<Object[]> result = new ArrayList<Object[]>();
        final Iterator<File> files = FileUtils.iterateFiles(new File("data/inrc2/input"), new String[]{"json"}, true);
        while (files.hasNext()) {
            final File next = files.next();
            if (next.isDirectory()) {
                continue;
            } else if (!next.canRead()) {
                continue;
            } else if (!next.getName().startsWith("Sc")) {
                continue;
            }
            result.add(new File[]{next});
        }
        return result;
    }

    private final File scenario;

    public ScenarioParserTest(final File scenario) {
        this.scenario = scenario;
    }

    @Test
    public void testParsing() throws JsonProcessingException, IOException {
        final Scenario result = ScenarioParser.parse(this.scenario);
        Assert.assertNotNull(result);
    }

}
