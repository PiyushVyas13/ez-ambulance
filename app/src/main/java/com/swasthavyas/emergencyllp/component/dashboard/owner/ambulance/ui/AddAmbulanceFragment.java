package com.swasthavyas.emergencyllp.component.dashboard.owner.ambulance.ui;

import static com.swasthavyas.emergencyllp.util.AppConstants.TAG;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.swasthavyas.emergencyllp.R;
import com.swasthavyas.emergencyllp.component.dashboard.owner.ambulance.domain.model.Ambulance;
import com.swasthavyas.emergencyllp.component.dashboard.owner.viewmodel.OwnerViewModel;
import com.swasthavyas.emergencyllp.component.dashboard.owner.ambulance.worker.AddAmbulanceWorker;
import com.swasthavyas.emergencyllp.databinding.FragmentAddAmbulanceBinding;
import com.swasthavyas.emergencyllp.util.types.AmbulanceType;
import com.swasthavyas.emergencyllp.component.dashboard.owner.ambulance.domain.model.Ambulance.ModelColumns;

public class AddAmbulanceFragment extends Fragment {
    FragmentAddAmbulanceBinding viewBinding;
    OwnerViewModel ownerViewModel;

    private Uri ambulanceUri;


    public AddAmbulanceFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentAddAmbulanceBinding.inflate(getLayoutInflater());

        ownerViewModel = new ViewModelProvider(requireActivity()).get(OwnerViewModel.class);



        ownerViewModel.getOwner().observe(getViewLifecycleOwner(), owner -> {
            if(owner != null) {
                Log.d(TAG, "ManageAmbulanceFragment.onCreateView: " + owner);
                Toast.makeText(requireContext(), owner.getId(), Toast.LENGTH_SHORT).show();
            }
        });

        viewBinding.vehicleType.setOnFocusChangeListener((v, hasFocus) -> viewBinding.vehicleType.setHint(hasFocus ? "Eg. Bolero, Omni, Traveller etc." : ""));

        viewBinding.cancelButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.ownerHomeFragment, null, new NavOptions.Builder().setEnterAnim(android.R.anim.slide_in_left).setExitAnim(android.R.anim.fade_out).build());
        });

        ActivityResultLauncher<PickVisualMediaRequest> ambulancePickerLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri uri) {
                if(uri != null) {
                    ambulanceUri = uri;
                    Log.d(TAG, "onActivityResult: " + uri);

                    Cursor returnCursor = requireActivity().getContentResolver().query(uri, null, null, null, null);

                    int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    returnCursor.moveToFirst();

                    viewBinding.ambulancePhoto.setText(returnCursor.getString(nameIndex));

                }
            }
        });

        viewBinding.ambulancePhotoLayout.setEndIconOnClickListener(v -> {
            ambulancePickerLauncher.launch(
                    new PickVisualMediaRequest.Builder()
                            .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                            .build()
            );
        });

        viewBinding.addAmbulanceBtn.setOnClickListener(v -> {
            if(ownerViewModel.getOwner().getValue() == null) {
                Toast.makeText(requireContext(), "Unauthorized", Toast.LENGTH_SHORT).show();
                return;
            }

            if(viewBinding.ambulanceType.getCheckedRadioButtonId() == -1 || viewBinding.vehicleNumber.getText().toString().isEmpty() || viewBinding.vehicleType.getText().toString().isEmpty() || ambulanceUri == null) {
                Toast.makeText(requireActivity(), "All fields are required", Toast.LENGTH_SHORT).show();
                return;
            }

            String ownerId = ownerViewModel.getOwner().getValue().getId();
            AmbulanceType ambulanceType = AmbulanceType.NONE;

            if(viewBinding.ambulanceType.getCheckedRadioButtonId() == viewBinding.advanceLifeSupport.getId()) {
                ambulanceType = AmbulanceType.ADVANCED;
            }
            else if(viewBinding.ambulanceType.getCheckedRadioButtonId() == viewBinding.basicLifeSupport.getId()) {
                ambulanceType = AmbulanceType.BASIC;
            }
            else if(viewBinding.ambulanceType.getCheckedRadioButtonId() == viewBinding.transport.getId()) {
                ambulanceType = AmbulanceType.MORTUARY;
            }

            String vehicleNumber = viewBinding.vehicleNumber.getText().toString();
            String vehicleType = viewBinding.vehicleType.getText().toString();

            OneTimeWorkRequest addAmbulanceRequest = new OneTimeWorkRequest.Builder(AddAmbulanceWorker.class)
                    .setInputData(new Data.Builder()
                            .putString(ModelColumns.OWNER_ID, ownerId)
                            .putString(ModelColumns.AMBULANCE_TYPE, ambulanceType.name())
                            .putString(ModelColumns.VEHICLE_TYPE, vehicleType)
                            .putString(ModelColumns.VEHICLE_NUMBER, vehicleNumber)
                            .putString("photoUri", ambulanceUri.toString())
                            .putString("userId", ownerViewModel.getOwner().getValue().getUserId())
                            .build())
                    .build();


            WorkManager.getInstance(requireContext())
                    .enqueue(addAmbulanceRequest);

            WorkManager.getInstance(requireContext())
                    .getWorkInfoByIdLiveData(addAmbulanceRequest.getId())
                    .observe(getViewLifecycleOwner(), workInfo -> {
                        if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.SUCCEEDED)) {
                            Toast.makeText(requireActivity(), "Ambulance Added Successfully", Toast.LENGTH_SHORT).show();

                            if(ownerViewModel.getOwner().getValue() == null) {
                                Toast.makeText(requireContext(), "Unauthorized", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            ownerViewModel.getOwner().getValue().addAmbulance(Ambulance.createFromMap(workInfo.getOutputData().getKeyValueMap()));

                            viewBinding.ambulanceType.clearCheck();
                            viewBinding.vehicleType.setText("");
                            viewBinding.vehicleType.setText("");
                            viewBinding.vehicleNumber.setText("");
                            viewBinding.addAmbulanceProgressbar.setVisibility(View.GONE);

                            Navigation.findNavController(v).navigate(R.id.ownerHomeFragment, null, new NavOptions.Builder().setEnterAnim(android.R.anim.slide_in_left).setExitAnim(android.R.anim.slide_out_right).build());
                        }
                        else if(workInfo.getState().isFinished() && workInfo.getState().equals(WorkInfo.State.FAILED)) {
                            Toast.makeText(requireContext(), workInfo.getOutputData().getString("message"), Toast.LENGTH_SHORT).show();
                            viewBinding.ambulanceType.clearCheck();
                            viewBinding.vehicleType.setText("");
                            viewBinding.vehicleType.setText("");
                            viewBinding.vehicleNumber.setText("");
                            viewBinding.addAmbulanceProgressbar.setVisibility(View.GONE);
                        }
                        else if(workInfo.getState().equals(WorkInfo.State.RUNNING)) {
                            viewBinding.addAmbulanceProgressbar.setVisibility(View.VISIBLE);
                        }
                    });

        });

        return viewBinding.getRoot();
    }
}
