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

import java.util.Set;

public class ApplicationDetails extends AppCompatActivity {
    AndroidApplication application;
    LayoutInflater layoutInflater;
    ApplicationsDatabase applicationsDatabase;
    public static final String APPLICATION_INDEX = "APPLICATION_INDEX";
    Set<String> exclusivePermissionsIgnoreList;

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
        application = ApplicationsDatabase.getApplicationsDatabase(this).applications.get(applicationIndex);
        exclusivePermissionsIgnoreList = application.getExclusiveIgnoredPermissionsList();
        addApplicationDetails();
        ListView permissionsList_listView = (ListView) findViewById(R.id.permissions);
        permissionsList_listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ApplicationDetails.this);
                builder.setTitle(R.string.ignore_for)
                        .setItems(new String[]{"All apps", "This app", "Cancel"}, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                switch(which){
                                    case 0:
                                        applicationsDatabase.ignorePermissionForAllApps(application.getPermissions().get(position));
                                        break;
                                    case 1:
                                        applicationsDatabase.ignorePermissionForSpecificApp(application.getPackageName(), application.getPermissions().get(position));
                                        break;
                                }
                            }
                        }).show();

            }
        });
    }

    private void addApplicationDetails() {
        final ApplicationsDatabase applicationsDatabase = ApplicationsDatabase.getApplicationsDatabase(ApplicationDetails.this);
        setTitle(application.getName());
        ListView permissionsList_listView = (ListView) findViewById(R.id.permissions);
        ArrayAdapter arrayAdapter = new ArrayAdapter<String>(this, R.layout.application_info_row){
            @NonNull
            @Override
            public View getView(int position, View reusableView, ViewGroup parent) {
                String permission = getItem(position);
                if(reusableView == null)
                    reusableView = layoutInflater.inflate(R.layout.application_info_row, parent, false);
                TextView permission_textView = (TextView) reusableView.findViewById(R.id.title);
                ImageView warningImage = (ImageView) reusableView.findViewById(R.id.warning_image);
                if(!applicationsDatabase.ignoredPermissionsForAllApps.contains(permission) && !exclusivePermissionsIgnoreList.contains(permission))
                    warningImage.setVisibility(View.VISIBLE);
                else
                    warningImage.setVisibility(View.INVISIBLE);
                permission_textView.setText(permission);
                return reusableView;
            }
        };
        for(String permission : application.getPermissions())
            arrayAdapter.add(permission);
        permissionsList_listView.setAdapter(arrayAdapter);
    }
}
