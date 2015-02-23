package org.minig;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.minig.config.MvcConfig;
import org.minig.config.ResourceConfig;
import org.minig.config.SecurityConfig;
import org.minig.config.ServiceConfig;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * @author Kamill Sokol
 */
@Import({SecurityConfig.class, ServiceConfig.class})
@EnableAutoConfiguration(exclude = FreeMarkerAutoConfiguration.class)
@Configuration
public class Starter implements ServletContextInitializer {

    private static final Logger LOG = getLogger(Starter.class);

    @Override
    public void onStartup(final ServletContext servletContext) throws ServletException {
        final File tmpDir = new File("tmp");
        LOG.info(String.format("temp directory: %s", tmpDir.getAbsolutePath()));
        servletContext.setAttribute(ServletContext.TEMPDIR, tmpDir);
    }

    @Bean
    public ServletRegistrationBean api() {
        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
        applicationContext.register(ResourceConfig.class);
        dispatcherServlet.setApplicationContext(applicationContext);
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(dispatcherServlet, "/api/*");
        servletRegistrationBean.setName("api");
        return servletRegistrationBean;
    }

    @Bean
    public ServletRegistrationBean dispatcherServlet() {
        DispatcherServlet dispatcherServlet = new DispatcherServlet();
        AnnotationConfigWebApplicationContext applicationContext = new AnnotationConfigWebApplicationContext();
        applicationContext.register(MvcConfig.class);
        dispatcherServlet.setApplicationContext(applicationContext);
        ServletRegistrationBean servletRegistrationBean = new ServletRegistrationBean(dispatcherServlet, "/");
        servletRegistrationBean.setName("dispatcherServlet");
        return servletRegistrationBean;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Starter.class, args);
    }
}
