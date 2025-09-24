package com.todo.gui;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.time.LocalDateTime;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import com.todo.dao.TodoAppDAO;
import com.todo.model.Todo;

public class TodoAppGUI extends JFrame {
    private TodoAppDAO todoDAO;
    private JTable todoTable;
    private DefaultTableModel tableModel;
    private JTextField titleField;
    private JTextArea descriptionArea;
    private JCheckBox completedCheckBox;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton refreshButton;
    private JComboBox<String> filterComboBox;

    public TodoAppGUI() {
        this.todoDAO = new TodoAppDAO();
        initializeComponents();
        setupLayout();
        setupEventListers();
        // Load data on startup
        filterTodos();
    }
    private void initializeComponents() {
        setTitle("Todo Application");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        String[] columnName = {"ID", "Title", "Description", "Completed"," Created At","Updated At"};
        tableModel = new DefaultTableModel(columnName, 0){
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        todoTable = new JTable(tableModel);
        todoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        todoTable.getSelectionModel().addListSelectionListener(
            (e) -> {
                if (!e.getValueIsAdjusting()){
                    int selectedRow = todoTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        String title = String.valueOf(tableModel.getValueAt(selectedRow, 1));
                        String description = String.valueOf(tableModel.getValueAt(selectedRow, 2));
                        boolean completed = Boolean.parseBoolean(String.valueOf(tableModel.getValueAt(selectedRow, 3)));
                        titleField.setText(title);
                        descriptionArea.setText(description);
                        completedCheckBox.setSelected(completed);
                    }
                }
            }
        );
        titleField = new JTextField(20);
        descriptionArea = new JTextArea(3, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);

        completedCheckBox = new JCheckBox("Completed");

        addButton = new JButton("Add todo");
        updateButton = new JButton("Update todo");
        deleteButton = new JButton("Delete todo");
        refreshButton = new JButton("Refresh todo");
        String[] filterOptions = {"All", "Completed", "Pending"};
        filterComboBox = new JComboBox<>(filterOptions);
        filterComboBox.addActionListener((e) -> {
            filterTodos();
        });
    }
    private void setupLayout(){
        setLayout(new BorderLayout());

        JPanel inputJPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;

        inputJPanel.add(new JLabel("Title: "), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        inputJPanel.add(titleField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        inputJPanel.add(new JLabel("Description"),gbc);
        gbc.gridx = 1;
        inputJPanel.add(new JScrollPane(descriptionArea),gbc);
        
        gbc.gridx = 1;
        gbc.gridy = 2;
        inputJPanel.add(completedCheckBox,gbc);
        
        JPanel ButtoPanel = new JPanel(new FlowLayout());
        ButtoPanel.add(addButton);
        ButtoPanel.add(updateButton);   
        ButtoPanel.add(deleteButton);
        ButtoPanel.add(refreshButton);
        
        add(inputJPanel,BorderLayout.NORTH);

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("Filter:"));
        filterPanel.add(filterComboBox);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(inputJPanel,BorderLayout.CENTER);
        northPanel.add(ButtoPanel,BorderLayout.SOUTH);
        northPanel.add(filterPanel,BorderLayout.NORTH);

        add(northPanel,BorderLayout.NORTH);
        add(new JScrollPane(todoTable),BorderLayout.CENTER);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(new JLabel("seletct the todo to update or delete:"));
        add(statusPanel,BorderLayout.SOUTH);
    }
    private void clearInputFields(){
        titleField.setText("");
        descriptionArea.setText("");
        completedCheckBox.setSelected(false);
    }
    private void setupEventListers(){
        addButton.addActionListener((e) -> {addTodo();});
        updateButton.addActionListener((e) -> {
            updateTodo();
        });
        deleteButton.addActionListener((e) -> {
            deleteTodo();
        });
        refreshButton.addActionListener((e) -> {
            refreshTodo();
        });
    }
    private void addTodo(){ 
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        boolean completed = completedCheckBox.isSelected();

        try{
            Todo todo = new Todo(title, description);
            todo.setCompleted(completed);
            todoDAO.createtodo(todo);
            JOptionPane.showMessageDialog(this, "Todo added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            filterTodos();
            clearInputFields();
        }
        catch (Exception e){
            JOptionPane.showMessageDialog(this, "Error adding todo: "+e.getMessage(), "Failure", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void updateTodo(){ 
        int selectedRow = todoTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a todo to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Object idObj = tableModel.getValueAt(selectedRow, 0);
        if (idObj == null) {
            JOptionPane.showMessageDialog(this, "Selected row does not have a valid ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int id = Integer.parseInt(String.valueOf(idObj));
        String title = titleField.getText().trim();
        String description = descriptionArea.getText().trim();
        boolean completed = completedCheckBox.isSelected();

        if (title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Title cannot be empty.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Todo todo = new Todo();
            todo.setId(id);
            todo.setTitle(title);
            todo.setDescription(description);
            todo.setCompleted(completed);
            todo.setUpdated_at(LocalDateTime.now());

            boolean ok = todoDAO.updateTodo(todo);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Todo updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                filterTodos();
                clearInputFields();
                // keep selection on the same id if possible
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update todo.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error updating todo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void deleteTodo(){ 
        int selectedRow = todoTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a todo to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            clearInputFields();
            return;
        }

        Object idObj = tableModel.getValueAt(selectedRow, 0);
        if (idObj == null) {
            JOptionPane.showMessageDialog(this, "Selected row does not have a valid ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this todo?",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        int id = Integer.parseInt(String.valueOf(idObj));
        try {
            boolean ok = todoDAO.deleteTodo(id);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Todo deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                clearInputFields();
                // Refresh table
                filterTodos();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete todo.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error deleting todo: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void refreshTodo(){ 
        clearInputFields();
        filterComboBox.setSelectedIndex(0);
        loadTodo();
    }
    private void filterTodos(){
        String selected = (String) filterComboBox.getSelectedItem();
        try {
            List<Todo> todos;
            if ("Completed".equals(selected)) {
                todos = todoDAO.filterTodo(true);
            } else if ("Pending".equals(selected)) {
                todos = todoDAO.filterTodo(false);
            } else {
                todos=todoDAO.getAllTodos();
            }
            upadateTable(todos);
            clearInputFields();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error applying filter: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void loadTodo(){
        try{
            List<Todo> todos = todoDAO.getAllTodos();
            upadateTable(todos);
            clearInputFields();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading todos: "+e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private void upadateTable(List<Todo> todos){
        tableModel.setRowCount(0);
        for (Todo todo : todos){
            Object[] rowData = {
                todo.getId(),
                todo.getTitle(),
                todo.getDescription(),
                todo.isCompleted(),
                todo.getCreated_at(),
                todo.getUpdated_at()
            };
            tableModel.addRow(rowData);
        }
    }
}
