package com.github.manolo8.darkbot.gui.utils.tree;

import com.github.manolo8.darkbot.config.ConfigEntity;
import eu.darkbot.api.config.ConfigSetting;
import eu.darkbot.api.config.annotations.Visibility;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

public class ConfigSettingTreeModel implements TreeModel {

    private ConfigSetting.Parent<?> root;
    private final List<TreeModelListener> listeners = new ArrayList<>();

    private final TreeFilter filter;

    public ConfigSettingTreeModel() {
        this(new TreeFilter());
    }

    public ConfigSettingTreeModel(TreeFilter filter) {
        this.filter = filter;
    }

    public void setRoot(ConfigSetting.Parent<?> root) {
        this.root = root;
        updateListeners();
    }

    public void setSearch(String search) {
        this.filter.setSearch(search);
    }

    public void setVisibility(Visibility.Level level) {
        this.filter.setVisibility(level);
    }

    public boolean isSearching() {
        return this.filter.isSearching();
    }

    public void updateListeners() {
        this.filter.invalidate();
        TreeModelEvent event = new TreeModelEvent(this, (TreeNode[]) null, null, null);
        for (TreeModelListener listener : listeners) {
            listener.treeStructureChanged(event);
        }
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public Object getChild(Object parent, int index) {
        return ((ConfigSetting.Parent<?>) parent)
                .getChildren()
                .values()
                .stream()
                .filter(filter)
                .skip(index)
                .findFirst()
                .orElse(null);
    }

    @Override
    public int getChildCount(Object parent) {
        return isLeaf(parent) ? 0 :
                (int) ((ConfigSetting.Parent<?>) parent)
                        .getChildren()
                        .values()
                        .stream()
                        .filter(filter)
                        .count();
    }

    @Override
    public boolean isLeaf(Object node) {
        return !(node instanceof ConfigSetting.Parent);
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        ConfigSetting<Object> setting = (ConfigSetting<Object>) path.getLastPathComponent();
        setting.setValue(newValue);

        TreeModelEvent event = new TreeModelEvent(this, path);
        for (TreeModelListener listener : listeners) {
            listener.treeNodesChanged(event);
        }
        ConfigEntity.changed();
    }

    @Override
    public int getIndexOfChild(Object parentObj, Object child) {
        ConfigSetting.Parent<?> parent = (ConfigSetting.Parent<?>) parentObj;
        int idx = -1;
        for (ConfigSetting<?> ch : parent.getChildren().values()) {
            if (filter.test(ch)) {
                idx++;
                if (ch == child) return idx;
            }
        }
        return -1;
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        listeners.remove(l);
    }

    public void clearListeners() {
        listeners.clear();
    }
}
