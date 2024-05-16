package com.swasthavyas.emergencyllp.util;

import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.swasthavyas.emergencyllp.AuthActivity;

public class FirebaseAuthUtil {
    public static FirebaseUser signInWithPhoneNumber(PhoneAuthCredential credential) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseUser[] user = {null};
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isComplete()) {
                            user[0] = task.getResult().getUser();
                            Log.d("MYAPP", "signInWithCredential:success");
                        }
                    }
                });

        return user[0];
    }
}
