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

public class ExtensionsFragment extends Fragment {

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

        // OpenGL ES Extensions
        addSectionHeader("OpenGL ES Extensions");
        String[] extensions = collector.getExtensions().split(" ");
        for (String ext : extensions) {
            if (!ext.isEmpty()) {
                addExtensionItem(ext);
            }
        }

        // Compressed Formats
        addSectionHeader("Compressed Texture Formats");
        for (String format : collector.getCompressedFormats()) {
            addExtensionItem(format);
        }

        // Binary Shader Formats
        addSectionHeader("Shader Binary Formats");
        for (String format : collector.getShaderBinaryFormats()) {
            addExtensionItem(format);
        }

        // Program Binary Formats
        addSectionHeader("Program Binary Formats");
        for (String format : collector.getProgramBinaryFormats()) {
            addExtensionItem(format);
        }

        // EGL Extensions
        addSectionHeader("EGL Extensions");
        String[] eglExts = collector.getEGLExtensions().split(" ");
        for (String ext : eglExts) {
            if (!ext.isEmpty()) {
                addExtensionItem(ext);
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

    private void addExtensionItem(String text) {
        TextView itemView = new TextView(requireContext());
        itemView.setText("• " + text);
        itemView.setTextAppearance(requireContext(), com.google.android.material.R.style.TextAppearance_Material3_BodySmall);
        itemView.setPadding(16, 4, 0, 4);
        containerLayout.addView(itemView);
    }
}
