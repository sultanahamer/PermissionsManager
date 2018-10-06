package open.com.permissionsmanager;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ApplicationDetails extends AppCompatActivity {
    private AndroidApplication application;
    private LayoutInflater layoutInflater;
    private ApplicationsDatabase applicationsDatabase;
    public static final String APPLICATION_INDEX = "APPLICATION_INDEX";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutInflater = getLayoutInflater();
        setContentView(R.layout.activity_application_details);
        Intent intent = getIntent();
        int applicationIndex = intent.getIntExtra(APPLICATION_INDEX, -1);
        if(applicationIndex == -1)
            finish();
        applicationsDatabase = ApplicationsDatabase.getApplicationsDatabase(ApplicationDetails.this);
        application = ApplicationsDatabase.getApplicationsDatabase(this).getACopyOfApplications().get(applicationIndex);
        addApplicationDetails();
        final ListView permissionsList_listView = (ListView) findViewById(R.id.permissions);
        final List<String> warnablePermissions = application.getWarnablePermissions();
        final int numberOfWarnablePermissions = warnablePermissions.size();
        permissionsList_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                if(position >= numberOfWarnablePermissions)
                    return;
                AlertDialog.Builder builder = new AlertDialog.Builder(ApplicationDetails.this);
                builder.setTitle(R.string.ignore_for)
                        .setItems(new String[]{"All apps", "This app", "Cancel"}, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch(which){
                                    case 0:
                                        applicationsDatabase.ignorePermissionForAllApps(warnablePermissions.get(position));
                                        break;
                                    case 1:
                                        applicationsDatabase.ignorePermissionForSpecificApp(application.getPackageName(), warnablePermissions.get(position));
                                        break;
                                }
                            }
                        }).show();

            }
        });
    }

    private void addApplicationDetails() {
        final List<String> warnablePermissions = getNameSpaceTruncatedPermissions(application.getWarnablePermissions());
        setTitle(application.getName());
        ListView permissionsList_listView = (ListView) findViewById(R.id.permissions);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.application_info_row){
            @NonNull
            @Override
            public View getView(int position, View reusableView, ViewGroup parent) {
                String permission = getItem(position);
                if(reusableView == null)
                    reusableView = layoutInflater.inflate(R.layout.application_info_row, parent, false);
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
