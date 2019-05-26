package com.github.manolo8.darkbot.gui;

import com.github.manolo8.darkbot.config.Config;
import com.github.manolo8.darkbot.config.tree.ConfigTree;
import com.github.manolo8.darkbot.gui.tree.TreeEditor;
import com.github.manolo8.darkbot.gui.tree.TreeRenderer;
import com.github.manolo8.darkbot.gui.tree.components.JBoolField;
import com.github.manolo8.darkbot.gui.tree.components.JCharField;
import com.github.manolo8.darkbot.gui.tree.components.JNumberField;
import com.github.manolo8.darkbot.gui.tree.components.JStringField;
import com.github.manolo8.darkbot.gui.utils.SimpleTreeListener;

import javax.swing.*;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

public class AdvancedConfig extends JPanel {

    private Config config;
    private ConfigTree treeModel;

    public AdvancedConfig() {
        setBorder(BorderFactory.createEmptyBorder());
        setLayout(new BorderLayout());
    }

    void setEditingConfig(Config config) {
        removeAll();
        this.config = config;
        add(setupUI());
    }

    public void setCustomConfig(String name, Object config) {
        treeModel.setCustom(name, config);
    }

    private JComponent setupUI() {
        JTree configTree = new JTree(this.treeModel = new ConfigTree(config));
        configTree.setEditable(true);
        configTree.setFocusable(false);
        configTree.setRootVisible(false);
        configTree.setShowsRootHandles(true);
        configTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        ToolTipManager.sharedInstance().registerComponent(configTree);

        TreeRenderer renderer = new TreeRenderer();
        configTree.setCellRenderer(renderer);
        TreeEditor editor = new TreeEditor(configTree, renderer);
        editor.addEditor(new JCharField(), char.class, Character.class);
        editor.addEditor(new JBoolField(), boolean.class);
        editor.addEditor(new JNumberField(), double.class, int.class);
        editor.addEditor(new JStringField(), String.class);

        configTree.setCellEditor(editor);

        treeModel.addTreeModelListener((SimpleTreeListener) e -> unfoldTopLevelTree(configTree));
        unfoldTopLevelTree(configTree);

        JScrollPane scrollPane = new JScrollPane(configTree);
        scrollPane.setBorder(null);
        return scrollPane;
    }

    private void unfoldTopLevelTree(JTree configTree) {
        for (int i = configTree.getRowCount() - 1; i >= 0; i--) configTree.expandRow(i);
    }

}
