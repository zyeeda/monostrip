package com.zyeeda.framework.web;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import org.ringojs.engine.ModuleObject;
import org.ringojs.jsgi.JsgiServlet;

public class SpringAwareJsgiServlet extends JsgiServlet {

	private static final long serialVersionUID = 987921475622280313L;
	
	private ServletContext servletCtx;
    private ApplicationContext springCtx;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        this.servletCtx = config.getServletContext();
        this.springCtx = WebApplicationContextUtils.getRequiredWebApplicationContext(this.servletCtx);
        String production = config.getInitParameter("production");
        
        Context.instance.setContext(springCtx);
        Context.instance.setProductionMode("true".equalsIgnoreCase(production));
    }

    public ApplicationContext getSpringContext() {
        return this.springCtx;
    }
    
    public Object getBean(String name) {
        return this.springCtx.getBean(name);
    }

    public static class Context {
        
        private static Context instance = new Context();
        private ApplicationContext context = null;
        private boolean productionMode = false;
        
        void setContext(ApplicationContext context) {
            this.context = context;
        }
        
        public boolean isProductionMode() {
            return productionMode;
        }

        public void setProductionMode(boolean productionMode) {
            this.productionMode = productionMode;
        }

        public ApplicationContext getSpringContext() {
            return context;
        }
        
        public Object getBean(String name) {
            return context.getBean(name);
        }
        
        public Object getBeanByClass(Class<?> clazz) {
            return context.getBean(clazz);
        }
        
        private Context() {
        
        }

        public static Context getInstance(ModuleObject module) {
            
            if (module == null) {
                return null;
            }
            
            String id = module.getId().toString();
            if (id == null || !id.startsWith("coala")) {
                return null;
            }
            
            return instance;
        }
    }
}


