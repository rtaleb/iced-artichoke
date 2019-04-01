package ca.uqac.lif.artichoke;

import jdk.nashorn.internal.scripts.JO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class CommitChangesDialog extends JDialog {


    private final static String[] groups = new String[]{"group0", "group1", "group2"};
    private final static Object[] columnNames = new Object[]{"Key", "Old value", "New value", "Group"};
    private final static int GROUP_COLUMN_INDEX = 3;

    private static final Logger LOGGER = Logger.getLogger(CommitChangesDialog.class.getCanonicalName());
    private CommitChangesController controller;

    private JPanel dialogPanel;
    private JPanel btnPanel;
    private JScrollPane tableScrollPane;
    private JTable changesTable;
    private JComboBox<String> groupComboBox;
    private JButton commitBtn;
    private JButton cancelBtn;
    private JLabel chooseGroupsLbl;


    public CommitChangesDialog(Frame frame, CommitChangesController controller, List<String> groupNames) {
        super(frame, "Confirm changes to commit", true);
        this.controller = controller;
        controller.setView(this);

        addWindowListener(controller);

        buildDialogPanel(groupNames);
    }


    protected void buildDialogPanel(List<String> groupNames) {
        dialogPanel = new JPanel();
        dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));

        chooseGroupsLbl = new JLabel(
                "Choose the groups you wish to perform the changes on behalf of:",
                SwingConstants.LEFT
        );
        dialogPanel.add(chooseGroupsLbl);


        // ComboxBox containing potential groups
        String[] groups = new String[groupNames.size()];
        groupNames.toArray(groups);
        groupComboBox = new JComboBox<>(groups);

        // tables containing new changes, non-editable except for last column
        changesTable = new JTable(new DefaultTableModel(columnNames, 0)) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == GROUP_COLUMN_INDEX;
            }
        };

        changesTable.getColumnModel().getColumn(GROUP_COLUMN_INDEX).setCellEditor(new DefaultCellEditor(groupComboBox));

        // Making table scrollable
        tableScrollPane = new JScrollPane(changesTable);
        changesTable.setFillsViewportHeight(true);
        this.add(tableScrollPane);

        commitBtn = new JButton("Commit");
        cancelBtn = new JButton("Cancel");

        btnPanel = new JPanel();
        btnPanel.add(commitBtn);
        btnPanel.add(cancelBtn);

        dialogPanel.add(tableScrollPane);
        dialogPanel.add(btnPanel);

        controller.setCommitButton(commitBtn);
        controller.setCancelButton(cancelBtn);
        controller.setChangesTable(changesTable);

        this.setContentPane(dialogPanel);
    }


    public void changesRetrieved(List<FormData.Change> changes) {
        if(changes == null || changes.size() == 0) {
            onNoChangesDetected();
            return;
        }

        for(FormData.Change change : changes) {
            LOGGER.info(change.toString());
            DefaultTableModel dtm = (DefaultTableModel) changesTable.getModel();

            dtm.addRow(new Object[]{
                    change.getKey(),
                    change.getOldValue(),
                    change.getNewValue()
            });
        }
    }

    public void onNoChangesDetected() {
        JOptionPane.showMessageDialog(
                this,
                "There is no modification to commit",
                "No modification",
                JOptionPane.WARNING_MESSAGE
        );
        changesTable.setEnabled(false);
        commitBtn.setEnabled(false);
        close();
    }

    public void onFieldKeyNotProvided(int rowIndex) {
        JOptionPane.showMessageDialog(
                this,
                "Element at row " + rowIndex + " does not have a key",
                "Field key not found",
                JOptionPane.ERROR_MESSAGE
        );
    }

    public void onGroupNotProvided(String key) {
        JOptionPane.showMessageDialog(
                this,
                "No group specified for field \"" + key + "\"",
                "Missing group",
                JOptionPane.WARNING_MESSAGE
        );
    }


    public void close() {
        this.dispose();
    }
}
