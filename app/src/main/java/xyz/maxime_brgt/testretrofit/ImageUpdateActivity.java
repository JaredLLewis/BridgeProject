package xyz.maxime_brgt.testretrofit;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static xyz.maxime_brgt.testretrofit.Constants.PICK_IMAGE_REQUEST;
import static xyz.maxime_brgt.testretrofit.Constants.READ_WRITE_EXTERNAL;

public class ImageUpdateActivity extends AppCompatActivity {

    private File chosenFile;
    private Uri returnUri;
    private ImageView imageView;
    private EditText description;
    private EditText name;
    private TextView uploadAsTextView;
    private Button uploadButton;
    private Button removeButton;
    private Button saveButton;
    MyDBHandler dbHandler = new MyDBHandler(this, null, null, 1);
    public static int lineNumber = 0;
    public static int position;
    public static String fileLocation = "";
    public static String ourImageURL = "";
    //private String uploadUserName = HomeActivity.enteredUserName;

    public static String formattedDate = "";

    private String imagePath = "";

    public static String displayTitle;
    public static String displayDescription;
    File imgFile;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_update);

        imageView = (ImageView) findViewById(R.id.imageView);
        name = (EditText) findViewById(R.id.name);
        description = (EditText) findViewById(R.id.description);
        uploadAsTextView = (TextView) findViewById(R.id.uploadAsTextView);
        //uploadButton = (Button) findViewById(R.id.uploadButton);
        removeButton = (Button)findViewById(R.id.removeButton);
        removeButton.setOnClickListener(removeListener);
        saveButton = (Button)findViewById(R.id.saveButton);
        saveButton.setOnClickListener(saveListener);
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        formattedDate = df.format(c);
        Log.d("ourDate", formattedDate.toString());
        Log.d("testing123", fileLocation);

        imgFile = new File(fileLocation);

        if (imgFile.exists()) {
            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            imageView.setImageBitmap(myBitmap);
        }

        name.setText(displayTitle);
        description.setText(displayDescription);


    }

    private View.OnClickListener removeListener = new View.OnClickListener() {
        public void onClick(View v) {

            //dbHandler.updateHandler(fileLocation, name.getText().toString(), description.getText().toString());
            Log.d("696969", fileLocation);
            dbHandler.deleteHandler(fileLocation);

            Intent i = new Intent(getApplicationContext(), ReadyActivity.class);
            startActivity(i);
        }
    };

    private View.OnClickListener saveListener = new View.OnClickListener() {
        public void onClick(View v) {

            Log.d("696969", fileLocation);
            Photo photo = dbHandler.findHandler(fileLocation);
            dbHandler.updateHandler(photo, name.getText().toString(), description.getText().toString());

            Toast.makeText(getApplicationContext(), "Information Saved!",
                    Toast.LENGTH_SHORT).show();

            Intent i = new Intent(getApplicationContext(), ReadyActivity.class);
            startActivity(i);
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            returnUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), returnUri);

                ImageView imageView = (ImageView) findViewById(R.id.imageView);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }

            super.onActivityResult(requestCode, resultCode, data);

            Log.d(this.getLocalClassName(), "Before check");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                final List<String> permissionsList = new ArrayList<String>();
                addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE);
                addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if (!permissionsList.isEmpty())
                    ActivityCompat.requestPermissions(ImageUpdateActivity.this,
                            permissionsList.toArray(new String[permissionsList.size()]),
                            READ_WRITE_EXTERNAL);
                else
                    getFilePath();
            } else {
                getFilePath();
            }
        }
    }

    private void getFilePath() {
        String filePath = DocumentHelper.getPath(this, this.returnUri);
        imagePath = filePath;
        if (filePath == null || filePath.isEmpty()) return;
        chosenFile = new File(filePath);
    }

    public void addFilePath(View v) {
        //String bridgeID = BridgeSelectActivity.bridgeID;
        String bridgeName = name.getText().toString();
        String bridgeDescription = description.getText().toString();
        //String bridgeUserID = HomeActivity.enteredUserName;
        //return;

        imageView.setImageResource(android.R.color.transparent);
        name.setText("");
        description.setText("");

        //GridActivity.myImageAdapter.remove(position);

        Intent goToLoginIntent = new Intent(getApplicationContext(), GridActivity.class);
        startActivity(goToLoginIntent);
    }

    private void addPermission(List<String> permissionsList, String permission) {
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            shouldShowRequestPermissionRationale(permission);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case READ_WRITE_EXTERNAL: {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                        && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(ImageUpdateActivity.this, "All Permission are granted.", Toast.LENGTH_SHORT)
                            .show();
                    getFilePath();
                } else {
                    Toast.makeText(ImageUpdateActivity.this, "Some permissions are denied", Toast.LENGTH_SHORT)
                            .show();
                }
                return;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void backButton(View v) {
        Intent goToLoginIntent = new Intent(getApplicationContext(), ReadyActivity.class);
        startActivity(goToLoginIntent);
    }
}