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

public class GLInfoFragment extends Fragment {

    private LinearLayout containerLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gl_info, container, false);
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

        // OpenGL ES Info
        addSectionHeader("OpenGL ES");
        addInfoCard("Vendor", collector.getRenderer());
        addInfoCard("Renderer", collector.getRenderer());
        addInfoCard("Version", collector.getVersion());
        addInfoCard("Shading Language", collector.getShadingLanguageVersion());

        // EGL Info
        addSectionHeader("EGL");
        addInfoCard("EGL Vendor", collector.getEGLVendor());
        addInfoCard("EGL Version", collector.getEGLVersion());
        addInfoCard("Client APIs", collector.getEGLClientAPIs());
    }

    private void addSectionHeader(String title) {
        View headerView = LayoutInflater.from(requireContext()).inflate(R.layout.item_section_header, containerLayout, false);
        TextView titleView = headerView.findViewById(R.id.section_title);
        titleView.setText(title);
        containerLayout.addView(headerView);
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
