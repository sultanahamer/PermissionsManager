package open.com.permissionsmanager;

import java.util.List;

public interface ApplicationDatabaseChangeListener {
    void applicationPermissionsUpdated(AndroidApplication androidApplication);
    void applicationsDatabaseUpdated(List<AndroidApplication> androidApplications);
    void applicationAddedToIgnoreList(AndroidApplication application);
    void applicationRemovedFromIgnoredList(AndroidApplication androidApplication);
}
