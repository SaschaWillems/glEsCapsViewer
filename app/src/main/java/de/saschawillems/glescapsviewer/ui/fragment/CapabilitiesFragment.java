package de.saschawillems.glescapsviewer.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import de.saschawillems.glescapsviewer.R;
import de.saschawillems.glescapsviewer.util.GLESInfoCollector;

public class CapabilitiesFragment extends Fragment {

    private LinearLayout containerLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ScrollView root = new ScrollView(requireContext());
        containerLayout = new LinearLayout(requireContext());
        containerLayout.setOrientation(LinearLayout.VERTICAL);
        containerLayout.setPadding(16, 16, 16, 16);
        root.addView(containerLayout);
        
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

        // GLES 2.0 Capabilities
        addSectionHeader("OpenGL ES 2.0 Capabilities");
        for (int i = 0; i < collector.getGLES20CapsNames().size(); i++) {
            String capName = collector.getGLES20CapsNames().get(i);
            String capValue = collector.getGLES20CapsValues().get(i);
            addCapabilityItem(capName, capValue);
        }

        // GLES 3.0 Capabilities
        if (collector.getMajorVersion() >= 3) {
            addSectionHeader("OpenGL ES 3.0 Capabilities");
            for (int i = 0; i < collector.getGLES30CapsNames().size(); i++) {
                String capName = collector.getGLES30CapsNames().get(i);
                String capValue = collector.getGLES30CapsValues().get(i);
                addCapabilityItem(capName, capValue);
            }
        }

        // GLES 3.1 Capabilities
        if (collector.getMajorVersion() >= 3 && collector.getMinorVersion() >= 1) {
            addSectionHeader("OpenGL ES 3.1 Capabilities");
            for (int i = 0; i < collector.getGLES31CapsNames().size(); i++) {
                String capName = collector.getGLES31CapsNames().get(i);
                String capValue = collector.getGLES31CapsValues().get(i);
                addCapabilityItem(capName, capValue);
            }
        }

        // GLES 3.2 Capabilities
        if (collector.getMajorVersion() >= 3 && collector.getMinorVersion() >= 2) {
            addSectionHeader("OpenGL ES 3.2 Capabilities");
            for (int i = 0; i < collector.getGLES32CapsNames().size(); i++) {
                String capName = collector.getGLES32CapsNames().get(i);
                String capValue = collector.getGLES32CapsValues().get(i);
                addCapabilityItem(capName, capValue);
            }
        }
    }

    private void addSectionHeader(String title) {
        TextView headerView = new TextView(requireContext());
        headerView.setText(title);
        headerView.setTextAppearance(requireContext(), com.google.android.material.R.style.TextAppearance_Material3_LabelLarge);
        headerView.setPadding(0, 16, 0, 8);
        containerLayout.addView(headerView);
    }

    private void addCapabilityItem(String name, String value) {
        LinearLayout itemLayout = new LinearLayout(requireContext());
        itemLayout.setOrientation(LinearLayout.VERTICAL);
        itemLayout.setPadding(8, 8, 8, 8);

        TextView nameView = new TextView(requireContext());
        nameView.setText(name);
        nameView.setTextAppearance(requireContext(), com.google.android.material.R.style.TextAppearance_Material3_LabelMedium);

        TextView valueView = new TextView(requireContext());
        valueView.setText(value);
        valueView.setTextAppearance(requireContext(), com.google.android.material.R.style.TextAppearance_Material3_BodySmall);
        valueView.setTextColor(getResources().getColor(android.R.color.darker_gray, null));

        itemLayout.addView(nameView);
        itemLayout.addView(valueView);
        containerLayout.addView(itemLayout);
    }
}
