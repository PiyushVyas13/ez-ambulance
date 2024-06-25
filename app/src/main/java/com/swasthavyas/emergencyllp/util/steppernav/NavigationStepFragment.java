package com.swasthavyas.emergencyllp.util.steppernav;

import androidx.fragment.app.Fragment;
import androidx.work.Data;

public abstract class NavigationStepFragment extends Fragment {

    protected boolean isIntegerNumeric(String s) {
        try {
            Integer.parseInt(s);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    protected boolean isDoubleNumeric(String s) {
        try {
            double x = Double.parseDouble(s);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    public abstract boolean validateData();
    public abstract Data collectData();
}
