package open.com.permissionsmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static open.com.permissionsmanager.MainActivity.APPLICATION_PACKAGE_NAME;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class ApplicationDetails extends AppCompatActivity {
    private AndroidApplication application;
    private LayoutInflater layoutInflater;
    private ApplicationsDatabase applicationsDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutInflater = getLayoutInflater();
        setContentView(R.layout.activity_application_details);
        Intent intent = getIntent();
        final String packageName = intent.getStringExtra(APPLICATION_PACKAGE_NAME);
        if(packageName == null)
            finish();
        applicationsDatabase = ApplicationsDatabase.getApplicationsDatabase(this);
        application = applicationsDatabase.getApplication(packageName);
        if(application == null)
            finish();
        addApplicationDetails();
        final ListView permissionsList_listView = (ListView) findViewById(R.id.permissions);
        final List<String> warnablePermissions = application.getWarnablePermissions();
        final List<String> nonWarnablePermissions = application.getNonwarnablePermissions();
        final int numberOfWarnablePermissions = warnablePermissions.size();
        permissionsList_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, final View view, final int position, long id) {

                if(position >= numberOfWarnablePermissions)
                    new AlertDialog.Builder(ApplicationDetails.this)
                        .setTitle(R.string.unignore_for)
                        .setItems(new String[]{"All apps", "This app", "Cancel"}, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch(which){
                                    case 0:
                                        applicationsDatabase.unignorePermissionForAllApps(nonWarnablePermissions.get(position - numberOfWarnablePermissions));
                                        recreate();
                                        break;
                                    case 1:
                                        applicationsDatabase.unignorePermissionForSpecificApp(application.getPackageName(), nonWarnablePermissions.get(position - numberOfWarnablePermissions));
                                        recreate();
                                        break;
                                }
                            }
                        }).show();
                else
                    new AlertDialog.Builder(ApplicationDetails.this)//TODO: refactor this block as this looks quite similar to above
                            .setTitle(R.string.ignore_for)
                            .setItems(new String[]{"All apps", "This app", "Cancel"}, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    switch(which){
                                        case 0:
                                            applicationsDatabase.ignorePermissionForAllApps(warnablePermissions.get(position));
                                            recreate();
                                            break;
                                        case 1:
                                            applicationsDatabase.ignorePermissionForSpecificApp(application.getPackageName(), warnablePermissions.get(position));
                                            recreate();
                                            break;
                                    }
                                }
                            }).show();

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Edit permissions granted").setIcon(R.drawable.ic_edit_24dp).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                launchApplicationSpecificSettingsActivity();
                return true;
            }
        }).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    private void launchApplicationSpecificSettingsActivity(){
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromParts("package", application.getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private void addApplicationDetails() {
        final List<String> warnablePermissions = getNameSpaceTruncatedPermissions(application.getWarnablePermissions());
        setTitle(application.getName());
        ListView permissionsList_listView = (ListView) findViewById(R.id.permissions);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.permission_row){
            @NonNull
            @Override
            public View getView(int position, View reusableView, ViewGroup parent) {
                String permission = getItem(position);
                if(reusableView == null)
                    reusableView = layoutInflater.inflate(R.layout.permission_row, parent, false);
                TextView permission_textView = (TextView) reusableView.findViewById(R.id.title);
                ImageView warningImage = (ImageView) reusableView.findViewById(R.id.warning_image);
                if(warnablePermissions.contains(permission))
                    warningImage.setVisibility(View.VISIBLE);
                else
                    warningImage.setVisibility(View.INVISIBLE);
                permission_textView.setText(permission);
                return reusableView;
            }
        };
        for(String permission : warnablePermissions)
            arrayAdapter.add(permission);
        for(String permission : getNameSpaceTruncatedPermissions(application.getNonwarnablePermissions()))
            arrayAdapter.add(permission);
        permissionsList_listView.setAdapter(arrayAdapter);
    }

    private List<String> getNameSpaceTruncatedPermissions(List<String> permissions) {
        List<String> truncatedPermissions = new ArrayList<>(permissions.size());
        for(String permission : permissions){
            truncatedPermissions.add(permission.replace("android.permission.", ""));
        }
        return truncatedPermissions;
    }
}
