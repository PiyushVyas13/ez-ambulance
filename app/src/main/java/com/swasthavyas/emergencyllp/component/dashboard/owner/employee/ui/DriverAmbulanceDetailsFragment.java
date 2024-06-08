package com.swasthavyas.emergencyllp.component.dashboard.owner.employee.ui;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.work.Data;

import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.swasthavyas.emergencyllp.component.dashboard.owner.ambulance.domain.model.Ambulance;
import com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel.OwnerViewModel;
import com.swasthavyas.emergencyllp.databinding.FragmentDriverAmbulanceDetailsBinding;
import com.swasthavyas.emergencyllp.component.dashboard.owner.employee.domain.model.EmployeeDriver.ModelColumns;

import java.util.List;
import java.util.stream.Collectors;


public class DriverAmbulanceDetailsFragment extends Fragment {
    FragmentDriverAmbulanceDetailsBinding viewBinding;
    OwnerViewModel ownerViewModel;

    private Uri aadhaarUri, licenseUri;

    public DriverAmbulanceDetailsFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        viewBinding = FragmentDriverAmbulanceDetailsBinding.inflate(getLayoutInflater());
        ownerViewModel = new ViewModelProvider(requireActivity()).get(OwnerViewModel.class);

        ownerViewModel.getOwner().observe(getViewLifecycleOwner(), owner -> {
            List<Ambulance> ambulances = owner.getAmbulances().getValue();

            List<String> ambulanceNumbers = ambulances.stream()
                    .map(Ambulance::getVehicleNumber)
                    .collect(Collectors.toList());


            viewBinding.ambulanceAutocomplete.setThreshold(1);
            viewBinding.ambulanceAutocomplete.setAdapter(new ArrayAdapter<String>(requireContext(), android.R.layout.simple_dropdown_item_1line, ambulanceNumbers));

        });


        ActivityResultLauncher<PickVisualMediaRequest> aadhaarPicker = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
           aadhaarUri = uri;
           viewBinding.aahdaarFileName.setText(getFileNameFromUri(uri));

        });

        ActivityResultLauncher<PickVisualMediaRequest> licensePicker = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri ->  {
           licenseUri = uri;
           viewBinding.licenseFileName.setText(getFileNameFromUri(uri));
        });



        viewBinding.driverAadhaarPickerButton.setOnClickListener(v -> {
            aadhaarPicker.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
        });

        viewBinding.driverLicencsePickerButton.setOnClickListener(v -> {
            licensePicker.launch(new PickVisualMediaRequest.Builder().setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
        });


        return viewBinding.getRoot();
    }

    public boolean validateData() {

        if(viewBinding.aadhaarnoDriver.getText().toString().isEmpty()) {
            Toast.makeText(requireActivity(), "All fields are required!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if(aadhaarUri == null || licenseUri == null) {
            Toast.makeText(requireActivity(), "Please upload all the documents", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    public Data collectData() {
        Data.Builder dataBuilder = new Data.Builder();

        String ambulanceNumber = viewBinding.ambulanceAutocomplete.getText().toString().isEmpty() ? null : viewBinding.ambulanceAutocomplete.getText().toString();

        dataBuilder.putString(ModelColumns.ASSIGNED_AMBULANCE_NUMBER, ambulanceNumber);
        dataBuilder.putString(ModelColumns.AADHAAR_NUMBER, viewBinding.aadhaarnoDriver.getText().toString());
        dataBuilder.putString("aadhaarUriString", aadhaarUri.toString());
        dataBuilder.putString("licenceUriString", licenseUri.toString());

        return dataBuilder.build();
    }

    private String getFileNameFromUri(Uri uri) {
        Cursor returnCursor = requireActivity().getContentResolver().query(uri, null, null, null, null);

        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

        returnCursor.moveToFirst();

        String fileName = returnCursor.getString(nameIndex);


        return fileName;

    }
}