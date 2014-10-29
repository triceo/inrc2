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

import com.fasterxml.jackson.core.JsonProcessingException;

@RunWith(Parameterized.class)
public class WeekParserTest {

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
            } else if (!next.getName().startsWith("W")) {
                continue;
            }
            result.add(new File[]{next});
        }
        return result;
    }

    private final File week;

    public WeekParserTest(final File week) {
        this.week = week;
    }

    @Test
    public void testParsing() throws JsonProcessingException, IOException {
        Assert.assertNotNull(WeekParser.parse(this.week));
    }

}
