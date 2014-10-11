package com.zyeeda.cdeio.test;

import org.junit.Test;
import org.ringojs.tools.RingoRunner;

public class AbstractJunitRingoTest {

    protected String[] getArgs() {
        return new String[] {"--modules", "packages:tests", "all.js"};
    }
    
    @Test
    public void testRun() {
        RingoRunner runner = new RingoRunner();
        runner.run(getArgs());
    }
    
}