package us.kbase.narrativemethodstore;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class AppEventListener implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        // do nothing
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
        try {
            NarrativeMethodStoreServer.getLocalGitDB().stopRefreshingThread();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }
}
