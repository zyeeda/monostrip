package com.zyeeda.framework.common.dao;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.NamedQueryDefinition;

/**
 * @author guyong
 *
 */
public class BaseDaoHelper {
    
    public static Long[] getLastModified(String...orms) {
        Long[] modifies = new Long[orms.length];
        for( int i = 0; i < orms.length; i ++ ) {
            String orm = orms[i];
            URL url = BaseDaoHelper.class.getClassLoader().getResource(orm);
            modifies[i] = new File(url.getPath()).lastModified();
        }
        return modifies;
    }
    
    public static Map<String, String> getNamedQueries(String...orms) {
        Configuration config = new Configuration();
        for( String orm : orms ) {
            config.addFile(orm);
        }
        config.buildMappings();
        Map<String, NamedQueryDefinition> queries = config.getNamedQueries();
        
        Map<String, String> result = new HashMap<String, String>();
        for( String key : queries.keySet() ) {
            result.put(key, queries.get(key).getQuery());
        }
        return result;
    }
}
