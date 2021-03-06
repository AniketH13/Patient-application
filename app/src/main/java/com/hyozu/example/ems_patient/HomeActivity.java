package com.hyozu.example.ems_patient;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.hyozu.example.ems_patient.Common.Common;
import com.hyozu.example.ems_patient.Model.EventBus.ShowNotificationFinishTrip;
import com.hyozu.example.ems_patient.Utils.UserUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class HomeActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1000;
    private AppBarConfiguration mAppBarConfiguration;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private NavController navController;
    private ImageView img_avatar;
    private Uri imageUri;
    private AlertDialog waitingDialog;
    private StorageReference storageReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        init();
    }
    private void init() {

        waitingDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage("Waiting....")
                .create();

        storageReference = FirebaseStorage.getInstance().getReference();

        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_sign_out)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
                builder.setTitle("sign out").setMessage("Do you really want to sign out?")
                        .setNegativeButton("CANCEL", (dialogInterface, i) -> {

                        })
                        .setPositiveButton("Sign Out", (dialogInterface, i) -> {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(HomeActivity.this, SplashActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .setCancelable(false);
                AlertDialog dialog = builder.create();
                dialog.setOnShowListener(dialog1 -> {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(ContextCompat.getColor(HomeActivity.this,android.R.color.holo_red_dark));
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                            .setTextColor(ContextCompat.getColor(HomeActivity.this,R.color.colorAccent));
                });

                dialog.show();

            }
            return true;
        });

        View headerView = navigationView.getHeaderView(0);
        TextView txt_name = (TextView)headerView.findViewById(R.id.txt_name);
        TextView txt_phone = (TextView)headerView.findViewById(R.id.text_phone);
        img_avatar = (ImageView)headerView.findViewById(R.id.image_avatar);

        txt_name.setText(Common.buildWelcomeMessage());
        txt_phone.setText(Common.currentPatient !=null ? Common.currentPatient.getPhoneNumber() : "");

        img_avatar.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent,PICK_IMAGE_REQUEST);
        });

        if (Common.currentPatient != null&& Common.currentPatient.getAvatar() != null &&
                !TextUtils.isEmpty(Common.currentPatient.getAvatar()))
        {
            Glide.with(this)
                    .load(Common.currentPatient.getAvatar())
                    .into(img_avatar);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK)
        {
            if (data != null && data.getData() != null)
            {
                imageUri = data.getData();

                showDialogUpload();
            }
        }
    }

    private void showDialogUpload() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeActivity.this);
        builder.setTitle("Change avatar").setMessage("Do you really want to change your Profile picture?")
                .setNegativeButton("CANCEL", (dialogInterface, i) -> {

                })
                .setPositiveButton("Upload", (dialogInterface, i) -> {
                    if (imageUri != null)
                    {
                        img_avatar.setImageURI(imageUri);
                        waitingDialog.setMessage("Uploading.....");
                        waitingDialog.show();
                        String unique_name = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        StorageReference avatarFolder = storageReference.child("avatars/"+unique_name);

                        avatarFolder.putFile(imageUri)
                                .addOnFailureListener(e -> {
                                    waitingDialog.dismiss();
                                    Snackbar.make(drawer,e.getMessage(),Snackbar.LENGTH_SHORT).show();
                                }).addOnCompleteListener(task -> {
                            if (task.isSuccessful())
                            {
                                avatarFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                                    Map<String,Object> updateData = new HashMap<>();
                                    updateData.put("avatar", uri.toString());
                                    UserUtils.updateUser(drawer,updateData);
                                });
                            }
                        }).addOnProgressListener(taskSnapshot -> {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            waitingDialog.setMessage(new StringBuilder("uploading: ").append(progress).append("%"));
                            waitingDialog.dismiss();
                        });

                    }
                })
                .setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(getResources().getColor(R.color.colorAccent));
        });

        dialog.show();

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

}