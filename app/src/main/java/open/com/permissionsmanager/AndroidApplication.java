package open.com.permissionsmanager;

import android.graphics.drawable.Drawable;

import java.util.List;

/**
 * Created by sultanm on 12/28/17.
 */

public class AndroidApplication {

    private String packageName;
    private String name;
    private List<String> nonwarnablePermissions;
    private List<String> warnablePermissions;
    private Drawable icon;
    private boolean ignoredTemporarily = false;

    public AndroidApplication(String packageName){
        this.packageName = packageName;
    }

    public boolean isIgnoredTemporarily() {
        return ignoredTemporarily;
    }

    private void setIgnoredTemporarily(boolean ignoredTemporarily) {
        this.ignoredTemporarily = ignoredTemporarily;
    }

    public String getPackageName() {
        return packageName;
    }

    private void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<String> getNonwarnablePermissions() {
        return nonwarnablePermissions;
    }

    private void setNonwarnablePermissions(List<String> nonwarnablePermissions) {
        this.nonwarnablePermissions = nonwarnablePermissions;
    }

    public String getName() {
        return name;
    }

    private void setName(String name) {
        this.name = name;
    }
    public List<String> getWarnablePermissions() {
        return warnablePermissions;
    }

    private void setWarnablePermissions(List<String> warnablePermissions) {
        this.warnablePermissions = warnablePermissions;
    }

    public Drawable getIcon() {
        return icon;
    }

    private void setIcon(Drawable icon) {
        this.icon = icon;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AndroidApplication that = (AndroidApplication) o;

        return packageName.equals(that.packageName);
    }

    public static class Builder{
        private AndroidApplication androidApplication;

        public Builder(String packageName){
            androidApplication = new AndroidApplication(packageName);
        }

        public Builder withName(String name){
            androidApplication.setName(name);
            return this;
        }

        public Builder withWarnablePermissions(List<String> warnablePermissions){
            androidApplication.setWarnablePermissions(warnablePermissions);
            return this;
        }

        public Builder withNonWarnablePermissions(List<String> nonWarnablePermissions){
            androidApplication.setNonwarnablePermissions(nonWarnablePermissions);
            return this;
        }

        public Builder withIcon(Drawable drawable){
            androidApplication.setIcon(drawable);
            return this;
        }

        public Builder withIgnoredTemporarily(boolean ignored){
            androidApplication.setIgnoredTemporarily(ignored);
            return this;
        }

        public AndroidApplication build() {
            return androidApplication;
        }
    }
}
