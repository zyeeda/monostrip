package com.zyeeda.cdeio.testbone;

import org.junit.Test;
import org.ringojs.tools.RingoRunner;

public class AbstractJunitRingoSupport {
    
    protected String getScript() {
        return "main.js";
    }
    
    /**
     * args: {"--modules", "packages:tests", "all.js"}
     * @return
     */
    protected String[] getArgs() {
        return new String[] {"--modules", "sinon", getScript()};
    }
    
    @Test
    public void testRun() {
        RingoRunner runner = new RingoRunner();
        runner.run(getArgs());
    }
    
}