package com.zyeeda.coala.web;

import java.util.HashMap;
import java.util.Map;

/**
 * @author guyong
 *
 */
public class FrontendSettingsCollector {

    private static Map<String, String> settings = new HashMap<String, String>();
    private static Map<String, String> backup = new HashMap<String, String>();
    private static boolean froze = false;
    
    private static void freeze() {
        froze = true;
    }
    
    private static void unfreeze() {
        froze = false;
        settings.putAll(backup);
        backup.clear();
    }
    
    public static void add(String key, String value) {
        if (froze) {
            backup.put(key, value);
        } else {
            settings.put(key, value);
        }
    }
    
    public synchronized static Map<String, String> getSettings() {
        freeze();
        Map<String, String> result = new HashMap<String, String>(settings);
        unfreeze();
        
        return result;
    }
    
}
