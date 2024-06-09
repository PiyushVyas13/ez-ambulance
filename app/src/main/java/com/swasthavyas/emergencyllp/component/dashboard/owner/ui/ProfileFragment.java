package com.swasthavyas.emergencyllp.component.dashboard.owner.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.swasthavyas.emergencyllp.AuthActivity;
import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel.OwnerViewModel;
import com.swasthavyas.emergencyllp.databinding.FragmentProfileBinding;
import com.swasthavyas.emergencyllp.util.AppConstants;


public class ProfileFragment extends Fragment {
    FragmentProfileBinding viewBinding;
    OwnerViewModel ownerViewModel;
    FirebaseUser currentUser;


    public ProfileFragment() {
        // Required empty public constructor
    }




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if(currentUser == null) {
            Intent intent = new Intent(requireActivity(), AuthActivity.class);
            startActivity(intent);
            requireActivity().finish();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        viewBinding = FragmentProfileBinding.inflate(getLayoutInflater());
        ownerViewModel = new ViewModelProvider(requireActivity()).get(OwnerViewModel.class);

        ActivityResultLauncher<PickVisualMediaRequest> profileImagePicker = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if(uri != null) {
                viewBinding.profileImage.setImageURI(uri);
                uploadProfileImage(uri);
            }
        });

        viewBinding.signOutBtn.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(requireActivity(), AuthActivity.class);
            startActivity(intent);
            requireActivity().finish();
        });

        viewBinding.changeProfileImage.setOnClickListener(v -> {
            profileImagePicker.launch(new PickVisualMediaRequest.Builder()
                                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                                            .build());
        });

        ownerViewModel.getOwner().observe(getViewLifecycleOwner(), owner -> {
            if(owner != null) {
                viewBinding.ownerName.setText(currentUser.getDisplayName());
                viewBinding.ownerEmail.setText(currentUser.getEmail());
                viewBinding.ownerNameSmall.setText(currentUser.getDisplayName());
                viewBinding.ownerEmailSmall.setText(currentUser.getEmail());
                viewBinding.ownerMobileNumber.setText(currentUser.getPhoneNumber());
                viewBinding.ownerAadhaar.setText(owner.getAadhaarNumber());
            }
        });

        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if(currentUser.getPhotoUrl() != null) {
            Glide.with(requireContext())
                    .load(currentUser.getPhotoUrl())
                    .placeholder(R.drawable.sample_profile)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .dontAnimate()
                    .into(viewBinding.profileImage);
            Toast.makeText(requireActivity(), currentUser.getPhotoUrl().toString(), Toast.LENGTH_SHORT).show();
            Log.d(AppConstants.TAG, "onViewCreated: " + currentUser.getPhotoUrl().toString());
        }
    }

    private void uploadProfileImage(Uri newProfileUri) {
        assert currentUser != null;

        StorageReference rootRef = FirebaseStorage.getInstance().getReference();
        StorageReference profilePicRef = rootRef.child(String.format("/users/owner/%s/profile_image.jpg", currentUser.getUid()));

        profilePicRef.putFile(newProfileUri)
                .addOnCompleteListener(uploadImageTask -> {
                   if(uploadImageTask.isSuccessful()) {

                       profilePicRef.getDownloadUrl()
                               .addOnCompleteListener(downloadUrlTask -> {

                                   if(downloadUrlTask.isSuccessful()) {
                                       UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                                               .setPhotoUri(downloadUrlTask.getResult())
                                               .build();

                                       currentUser.updateProfile(profileChangeRequest)
                                               .addOnCompleteListener(imageUpdateTask -> {
                                                  if(imageUpdateTask.isSuccessful()) {
                                                      Toast.makeText(requireContext(), "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                                                  }
                                                  else {
                                                      Log.d(AppConstants.TAG, "uploadProfileImage: " + imageUpdateTask.getException());
                                                  }

                                               });
                                   }
                                   else {
                                       Log.d(AppConstants.TAG, "uploadProfileImage: " + downloadUrlTask.getException());
                                   }
                               });

                   }
                   else {
                       Log.d(AppConstants.TAG, "uploadProfileImage: " + uploadImageTask.getException());
                   }

                });

    }
}