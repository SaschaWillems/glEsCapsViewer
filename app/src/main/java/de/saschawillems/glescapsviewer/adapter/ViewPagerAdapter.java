package de.saschawillems.glescapsviewer.adapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import de.saschawillems.glescapsviewer.ui.fragment.CapabilitiesFragment;
import de.saschawillems.glescapsviewer.ui.fragment.DeviceFragment;
import de.saschawillems.glescapsviewer.ui.fragment.ExtensionsFragment;
import de.saschawillems.glescapsviewer.ui.fragment.GLInfoFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull AppCompatActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new DeviceFragment();
            case 1:
                return new GLInfoFragment();
            case 2:
                return new CapabilitiesFragment();
            case 3:
                return new ExtensionsFragment();
            default:
                return new DeviceFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
