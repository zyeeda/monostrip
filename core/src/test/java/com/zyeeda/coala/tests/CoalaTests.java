package com.zyeeda.coala.tests;

import java.lang.reflect.Field;
import java.util.List;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.BeforeClass;

import com.zyeeda.coala.test.AbstractTestNGRingoSupportTests;
import com.zyeeda.coala.web.SpringAwareJsgiServlet;

/**
 * @author guyong
 *
 */
@ContextConfiguration("classpath:spring-test-hibernate.xml")
public class CoalaTests extends AbstractTestNGRingoSupportTests {

    @BeforeClass
    public void initContext() throws Exception {
        Class<SpringAwareJsgiServlet.Context> clazz = SpringAwareJsgiServlet.Context.class;
        Field field = clazz.getDeclaredField("instance");
        field.setAccessible(true);
        Object value = field.get(null);
        ReflectionTestUtils.setField(value, "context", applicationContext);
    }

    @Override
    protected boolean isDebugEnabled() {
        return false;
    }

    @Override
    protected List<String> getRingoSystemModules() {
        List<String> result = super.getRingoSystemModules();
        result.add("coala-modules");
        return result;
    }
    
}
