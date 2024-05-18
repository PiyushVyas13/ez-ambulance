package com.swasthavyas.emergencyllp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;


import com.google.firebase.auth.FirebaseUser;
import com.swasthavyas.emergencyllp.component.auth.viewmodel.AuthViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.ui.DriverDashboardFragment;
import com.swasthavyas.emergencyllp.component.dashboard.ui.OwnerDashboardFragment;
import com.swasthavyas.emergencyllp.component.dashboard.worker.FetchRoleWorker;
import com.swasthavyas.emergencyllp.util.types.UserRole;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    public static final String MYAPP = "MYAPP";
    FirebaseUser currentUser;
    AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        AtomicBoolean lock = new AtomicBoolean(false);
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        Log.d(MYAPP, "In MainActivity");


        authViewModel.getCurrentUser().observe(this, firebaseUser -> {


            if (!lock.get()) {
                this.currentUser = firebaseUser;
                Log.d("MYAPP", "MainActivity.Observer: " + this.currentUser);

                if (this.currentUser != null) {
                    OneTimeWorkRequest fetchRoleRequest = new OneTimeWorkRequest.Builder(FetchRoleWorker.class)
                            .setInputData(new Data.Builder()
                                    .putString("userId", currentUser.getUid())
                                    .build())
                            .build();



                        WorkManager.getInstance(getApplicationContext())
                                .enqueueUniqueWork(currentUser.getUid(), ExistingWorkPolicy.REPLACE,fetchRoleRequest);

                        WorkManager.getInstance(getApplicationContext())
                                .getWorkInfosForUniqueWorkLiveData(currentUser.getUid())
                                .observe(this, workInfos -> {
                                    WorkInfo workInfo = workInfos.get(0);
                                    if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.SUCCEEDED)) {
                                        String role = workInfo.getOutputData().getString("role");
                                        if(role == null) {
                                            Log.d(MYAPP, "MainActivity.onCreate: role not received.");
                                            return;

                                        }
                                        Toast.makeText(this, role, Toast.LENGTH_SHORT).show();

                                        switch (UserRole.valueOf(role.toUpperCase(Locale.getDefault()))) {
                                            case OWNER:
                                                fragmentTransaction.replace(R.id.dashboard_container, new OwnerDashboardFragment());
                                                break;
                                            case DRIVER:
                                                fragmentTransaction.replace(R.id.dashboard_container, DriverDashboardFragment.newInstance(UserRole.DRIVER));
                                                break;
                                            case EMPLOYEE_DRIVER:
                                                fragmentTransaction.replace(R.id.dashboard_container, DriverDashboardFragment.newInstance(UserRole.EMPLOYEE_DRIVER));
                                                break;
                                            default:
                                                Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                                Log.d(MYAPP, "MainActivity.onCreate: invalid role - [ " + role + " ]");
                                        }

                                        fragmentTransaction.commit();

                                    }
                                    else if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.FAILED)) {
                                        if(workInfo.getOutputData().getString("exception").equals("NoSuchElementException")) {
                                            // TODO: Redirect to registration UI
    //                                    FirebaseAuth.getInstance().signOut();
                                            Log.d("MYAPP", "fetchRoleObserver: " + workInfo.getId() + ": " +workInfo.getState());
                                            Toast.makeText(this, "Redirecting to registration UI...", Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
                                            startActivity(intent);
                                            finish();

                                        }
                                        else {
                                            Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
                                            Log.d(MYAPP, "onCreate: " + workInfo.getOutputData().getString("message"));
                                        }
                                    }


                                });





                } else {
                    Intent intent = new Intent(this, AuthActivity.class);
                    startActivity(intent);
                    Toast.makeText(this, "You are not logged in", Toast.LENGTH_SHORT).show();
                    Log.d("MYAPP", "Not logged in");
                    finish();
                }

                lock.set(true);
            }
        });


    }


}

