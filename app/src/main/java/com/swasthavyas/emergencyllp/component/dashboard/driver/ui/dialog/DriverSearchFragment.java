package com.swasthavyas.emergencyllp.component.dashboard.driver.ui.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.swasthavyas.emergencyllp.component.dashboard.driver.adapter.DriverSearchListAdapter;
import com.swasthavyas.emergencyllp.component.dashboard.owner.component.employee.domain.model.EmployeeDriver;
import com.swasthavyas.emergencyllp.databinding.SearchDriverDialogBinding;

import java.util.ArrayList;
import java.util.List;


public class DriverSearchFragment extends DialogFragment {
    SearchDriverDialogBinding viewBinding;
    private final List<EmployeeDriver> employeeList;
    DriverSearchListAdapter searchListAdapter;
    private  Dialog dialog;

    public interface DriverSearchDialogListener {
        void onDriverSelected(Dialog dialog, EmployeeDriver driver);
    }

    private DriverSearchDialogListener dialogListener;

    private final DriverSearchListAdapter.ItemClickListener itemClickListener = (position, driver) -> {
        if(dialog != null) {
            dialogListener.onDriverSelected(dialog, driver);
        }
    };


    public DriverSearchFragment(List<EmployeeDriver> employees, DriverSearchDialogListener listener) {
        this.employeeList = employees;
        this.dialogListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewBinding = SearchDriverDialogBinding.inflate(inflater);
        searchListAdapter = new DriverSearchListAdapter(requireContext(), itemClickListener, employeeList);
        viewBinding.driverSearchResultList.setLayoutManager(new LinearLayoutManager(requireContext()));
        viewBinding.driverSearchResultList.setAdapter(searchListAdapter);

        viewBinding.driverSearchView.requestFocus();
        viewBinding.driverSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterEmployeeList(newText);
                return true;
            }
        });

        return viewBinding.getRoot();
    }

    private void filterEmployeeList(String newText) {
        List<EmployeeDriver> filteredList = new ArrayList<>();

        for(EmployeeDriver driver: employeeList) {
            if(driver.getName().toLowerCase().contains(newText.toLowerCase())) {
                filteredList.add(driver);
            }
        }

        if(!filteredList.isEmpty()) {
            searchListAdapter.setFilteredList(filteredList);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.dialog = dialog;
        return dialog;
    }
}
