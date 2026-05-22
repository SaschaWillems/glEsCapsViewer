package de.saschawillems.glescapsviewer.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import de.saschawillems.glescapsviewer.R;
import de.saschawillems.glescapsviewer.util.GLESInfoCollector;

public class DeviceFragment extends Fragment {

    private LinearLayout containerLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_device, container, false);
        containerLayout = root.findViewById(R.id.container_layout);
        updateUI();
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    private void updateUI() {
        containerLayout.removeAllViews();
        GLESInfoCollector collector = GLESInfoCollector.getInstance(requireContext());

        addInfoCard("Device Name", collector.getDeviceName());
        addInfoCard("OS Version", collector.getDeviceOS());
        addInfoCard("CPU Cores", String.valueOf(collector.getDeviceCPUCores()));
        addInfoCard("CPU Speed (MHz)", String.format("%.2f", collector.getDeviceCPUSpeed()));
        addInfoCard("Screen Resolution", collector.getScreenWidth() + " x " + collector.getScreenHeight());
        addInfoCard("Architecture", collector.getDeviceCPUArch());
    }

    private void addInfoCard(String label, String value) {
        View cardView = LayoutInflater.from(requireContext()).inflate(R.layout.item_info_card, containerLayout, false);
        TextView labelView = cardView.findViewById(R.id.label);
        TextView valueView = cardView.findViewById(R.id.value);

        labelView.setText(label);
        valueView.setText(value != null && !value.isEmpty() ? value : "N/A");
        containerLayout.addView(cardView);
    }
}
