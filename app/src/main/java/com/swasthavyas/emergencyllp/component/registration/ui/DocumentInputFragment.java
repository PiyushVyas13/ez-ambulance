package com.swasthavyas.emergencyllp.component.registration.ui;

import static android.text.format.Formatter.formatFileSize;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Toast;

import com.swasthavyas.emergencyllp.component.registration.viewmodel.RegistrationViewModel;
import com.swasthavyas.emergencyllp.databinding.FragmentDocumentInputBinding;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;


public class DocumentInputFragment extends Fragment {
    FragmentDocumentInputBinding viewBinding;
    private static final int AADHAAR_PICKER_REQUEST_CODE = 0;
    private Uri aadhaarUri = null;

    public DocumentInputFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        RegistrationViewModel viewModel = new ViewModelProvider(requireActivity()).get(RegistrationViewModel.class);
        viewBinding = FragmentDocumentInputBinding.inflate(getLayoutInflater());


        viewBinding.finishButton.setOnClickListener(v -> {
            if(viewBinding.aadhaarNo.getText().toString().isEmpty()) {
                Toast.makeText(requireActivity(), "Please provide aadhaar number.", Toast.LENGTH_SHORT).show();
                return;
            }

            if(viewBinding.aadhaarNo.getText().length() != 12) {
                Toast.makeText(requireActivity(), "Provide a valid aadhaar number.", Toast.LENGTH_SHORT).show();
                return;
            }

            if(aadhaarUri == null) {
                Toast.makeText(requireActivity(), "Please upload your aadhaar photo", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.setAadhaarNumber(viewBinding.aadhaarNo.getText().toString());
            viewModel.setAadhaarUri(aadhaarUri);
        });

        ActivityResultLauncher<String[]> getAdhaarUri = registerForActivityResult(new ActivityResultContracts.OpenDocument(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        if(uri == null) {
                            Toast.makeText(requireActivity(), "Something went wrong! Please try again 2", Toast.LENGTH_SHORT).show();
                            viewBinding.aadhaarUploadButton.setEnabled(true);
                            return;
                        }


                        Cursor returnCursor = requireActivity().getContentResolver().query(uri, null, null, null, null);

                        int nameIndex =returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
                        returnCursor.moveToFirst();

//                        Log.d("MYAPP", "onActivityResult: name: " + returnCursor.getString(nameIndex));
//                        Log.d("MYAPP", "onActivityResult: size: " + returnCursor.getLong(sizeIndex));


                        String fileSize = formatFileSize(requireContext(), returnCursor.getLong(sizeIndex));

                        viewBinding.previewFileName.setText(returnCursor.getString(nameIndex));
                        viewBinding.previewFileSize.setText(fileSize);
                        viewBinding.filePreviewCard.setVisibility(View.VISIBLE);
                        aadhaarUri = uri;

                    }
                });



            viewBinding.previewCancelBtn.setOnClickListener(v -> {
                aadhaarUri = null;
                viewBinding.aadhaarUploadButton.setEnabled(true);
                viewBinding.filePreviewCard.setVisibility(View.GONE);
            });


        viewBinding.aadhaarUploadButton.setOnClickListener(v -> {
            getAdhaarUri.launch(new String[] {"application/pdf"});
            viewBinding.aadhaarUploadButton.setEnabled(false);
        });


        // Inflate the layout for this fragment
        return viewBinding.getRoot();
    }
}