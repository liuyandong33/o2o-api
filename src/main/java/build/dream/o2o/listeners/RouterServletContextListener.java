package build.dream.o2o.listeners;

import build.dream.common.listeners.BasicServletContextListener;
import build.dream.o2o.mappers.CommonMapper;

import javax.servlet.ServletContextEvent;
import javax.servlet.annotation.WebListener;

@WebListener
public class RouterServletContextListener extends BasicServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        super.previousInjectionBean(servletContextEvent.getServletContext(), CommonMapper.class);
        super.contextInitialized(servletContextEvent);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
