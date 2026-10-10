package com.fittrack;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class FitnessApp extends JFrame {
    private long userId;
    private String userName;
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    public FitnessApp() {
        super("FitTrack — Online Fitness Tracking");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(980, 680);
        setMinimumSize(new Dimension(820, 560));
        setLocationRelativeTo(null);
        root.add(buildAuthPanel(), "auth");
        add(root);
        cards.show(root, "auth");
    }

    private JPanel buildAuthPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(239, 246, 255));
        JPanel box = new JPanel(new GridBagLayout());
        box.setBorder(new EmptyBorder(28, 32, 28, 32));
        box.setBackground(Color.WHITE);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(7, 7, 7, 7); c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        JLabel title = new JLabel("FitTrack", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 30));
        c.gridx=0; c.gridy=0; c.gridwidth=2; box.add(title,c);
        JLabel subtitle = new JLabel("Track workouts. Build healthy habits.", SwingConstants.CENTER);
        c.gridy++; box.add(subtitle,c);
        JTextField name = new JTextField(22);
        JTextField email = new JTextField(22);
        JPasswordField password = new JPasswordField(22);
        c.gridwidth=1; c.gridy++; c.gridx=0; box.add(new JLabel("Name (registration)"),c);
        c.gridx=1; box.add(name,c);
        c.gridy++; c.gridx=0; box.add(new JLabel("Email"),c);
        c.gridx=1; box.add(email,c);
        c.gridy++; c.gridx=0; box.add(new JLabel("Password"),c);
        c.gridx=1; box.add(password,c);
        JButton register = new JButton("Create account");
        JButton login = new JButton("Login");
        c.gridy++; c.gridx=0; box.add(register,c); c.gridx=1; box.add(login,c);
        JLabel hint = new JLabel("New users: enter a name, email and password, then Create account.", SwingConstants.CENTER);
        hint.setFont(new Font("SansSerif", Font.PLAIN, 11)); c.gridy++; c.gridx=0; c.gridwidth=2; box.add(hint,c);
        panel.add(box);

        register.addActionListener(e -> {
            String n=name.getText().trim(), em=email.getText().trim().toLowerCase();
            char[] pw=password.getPassword();
            if (n.isBlank() || em.isBlank() || pw.length < 8) {
                message("Enter your name and email, and use a password of at least 8 characters."); return;
            }
            try {
                String hash=PasswordUtil.hash(pw);
                try (Connection con=Database.connect();
                     PreparedStatement ps=con.prepareStatement("INSERT INTO users(name,email,password_hash) VALUES(?,?,?)")) {
                    ps.setString(1,n); ps.setString(2,em); ps.setString(3,hash); ps.executeUpdate();
                }
                message("Account created. You can now log in.");
            } catch (SQLIntegrityConstraintViolationException ex) { message("That email is already registered."); }
              catch (Exception ex) { error(ex); }
            finally { java.util.Arrays.fill(pw, '\0'); }
        });
        login.addActionListener(e -> {
            String em=email.getText().trim().toLowerCase(); char[] pw=password.getPassword();
            try (Connection con=Database.connect();
                 PreparedStatement ps=con.prepareStatement("SELECT id,name,password_hash FROM users WHERE email=?")) {
                ps.setString(1,em);
                try (ResultSet rs=ps.executeQuery()) {
                    if (rs.next() && PasswordUtil.verify(pw,rs.getString("password_hash"))) {
                        userId=rs.getLong("id"); userName=rs.getString("name"); showDashboard();
                    } else message("Invalid email or password.");
                }
            } catch (Exception ex) { error(ex); }
            finally { java.util.Arrays.fill(pw, '\0'); }
        });
        return panel;
    }

    private void showDashboard() {
        JPanel page = new JPanel(new BorderLayout(12,12));
        page.setBorder(new EmptyBorder(16,16,16,16));
        JPanel top = new JPanel(new BorderLayout());
        JLabel welcome = new JLabel("Welcome, " + userName + " — your fitness dashboard");
        welcome.setFont(new Font("SansSerif",Font.BOLD,20));
        JButton logout = new JButton("Log out");
        logout.addActionListener(e -> { userId=0; userName=null; root.removeAll(); root.add(buildAuthPanel(),"auth"); cards.show(root,"auth"); root.revalidate(); root.repaint(); });
        top.add(welcome,BorderLayout.WEST); top.add(logout,BorderLayout.EAST);
        page.add(top,BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Overview", overviewPanel());
        tabs.addTab("Log activity", activityPanel());
        tabs.addTab("Goals", goalsPanel());
        tabs.addTab("Progress history", historyPanel());
        page.add(tabs,BorderLayout.CENTER);
        root.add(page,"dashboard"); cards.show(root,"dashboard"); root.revalidate(); root.repaint();
    }

    private JPanel overviewPanel() {
        JPanel p=new JPanel(new BorderLayout(10,10)); p.setBorder(new EmptyBorder(18,18,18,18));
        JLabel intro=new JLabel("<html><h2>Your activity at a glance</h2>Log activities to see your totals.</html>");
        p.add(intro,BorderLayout.NORTH);
        JTextArea stats=new JTextArea(); stats.setEditable(false); stats.setFont(new Font("SansSerif",Font.PLAIN,16));
        try (Connection con=Database.connect();
             PreparedStatement ps=con.prepareStatement(
               "SELECT COUNT(*) sessions, COALESCE(SUM(duration_minutes),0) minutes, COALESCE(SUM(calories),0) calories, COALESCE(SUM(steps),0) steps FROM activities WHERE user_id=? AND activity_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)")) {
            ps.setLong(1,userId);
            try(ResultSet rs=ps.executeQuery()){ if(rs.next()) stats.setText(
                "Last 7 days\n\nWorkout sessions: "+rs.getInt("sessions")+
                "\nTotal workout time: "+rs.getInt("minutes")+" minutes"+
                "\nCalories logged: "+rs.getInt("calories")+
                "\nSteps logged: "+rs.getInt("steps")+
                "\n\nTip: use Log activity to record today's workout, steps and calories."); }
        } catch(Exception ex){ stats.setText("Could not load overview: "+ex.getMessage()); }
        p.add(stats,BorderLayout.CENTER); return p;
    }

    private JPanel activityPanel() {
        JPanel p=new JPanel(new BorderLayout(12,12)); p.setBorder(new EmptyBorder(18,18,18,18));
        JPanel form=new JPanel(new GridLayout(0,2,8,8));
        JTextField date=new JTextField(LocalDate.now().toString());
        JTextField type=new JTextField("Workout");
        JSpinner duration=new JSpinner(new SpinnerNumberModel(30,0,1440,5));
        JSpinner calories=new JSpinner(new SpinnerNumberModel(200,0,10000,25));
        JSpinner steps=new JSpinner(new SpinnerNumberModel(0,0,100000,500));
        JTextField notes=new JTextField();
        form.add(new JLabel("Date (YYYY-MM-DD)"));form.add(date);
        form.add(new JLabel("Activity type"));form.add(type);
        form.add(new JLabel("Duration (minutes)"));form.add(duration);
        form.add(new JLabel("Calories"));form.add(calories);
        form.add(new JLabel("Steps"));form.add(steps);
        form.add(new JLabel("Notes (optional)"));form.add(notes);
        JButton save=new JButton("Save activity");
        save.addActionListener(e -> {
            try {
                LocalDate parsed=LocalDate.parse(date.getText().trim());
                if(type.getText().trim().isBlank()) { message("Activity type is required."); return; }
                try(Connection con=Database.connect(); PreparedStatement ps=con.prepareStatement(
                  "INSERT INTO activities(user_id,activity_date,activity_type,duration_minutes,calories,steps,notes) VALUES(?,?,?,?,?,?,?)")){
                    ps.setLong(1,userId);ps.setDate(2,Date.valueOf(parsed));ps.setString(3,type.getText().trim());
                    ps.setInt(4,(Integer)duration.getValue());ps.setInt(5,(Integer)calories.getValue());
                    ps.setInt(6,(Integer)steps.getValue());ps.setString(7,notes.getText().trim());ps.executeUpdate();
                }
                message("Activity saved."); date.setText(LocalDate.now().toString()); type.setText("Workout"); notes.setText("");
            } catch(Exception ex){ error(ex); }
        });
        p.add(form,BorderLayout.NORTH);p.add(save,BorderLayout.SOUTH);
        return p;
    }

    private JPanel goalsPanel() {
        JPanel p=new JPanel(new BorderLayout(10,10));p.setBorder(new EmptyBorder(18,18,18,18));
        JPanel form=new JPanel(new GridLayout(0,2,8,8));
        JComboBox<String> kind=new JComboBox<>(new String[]{"Steps","Calories","Workout minutes","Workout sessions"});
        JSpinner target=new JSpinner(new SpinnerNumberModel(10000,1,1000000,500));
        JTextField due=new JTextField(LocalDate.now().plusMonths(1).toString());
        form.add(new JLabel("Goal type"));form.add(kind);form.add(new JLabel("Target value"));form.add(target);
        form.add(new JLabel("Target date (YYYY-MM-DD)"));form.add(due);
        JButton add=new JButton("Add goal");
        add.addActionListener(e -> {
            try(Connection con=Database.connect();PreparedStatement ps=con.prepareStatement(
              "INSERT INTO goals(user_id,goal_type,target_value,target_date) VALUES(?,?,?,?)")){
                ps.setLong(1,userId);ps.setString(2,(String)kind.getSelectedItem());ps.setInt(3,(Integer)target.getValue());
                String d=due.getText().trim(); if(d.isBlank()) ps.setNull(4,Types.DATE); else ps.setDate(4,Date.valueOf(LocalDate.parse(d)));
                ps.executeUpdate();message("Goal added.");
            }catch(Exception ex){error(ex);}
        });
        JTable table=new JTable();
        JButton refresh=new JButton("Refresh goals");
        refresh.addActionListener(e -> loadGoals(table));
        JPanel south=new JPanel(new BorderLayout());south.add(add,BorderLayout.WEST);south.add(refresh,BorderLayout.EAST);
        p.add(form,BorderLayout.NORTH);p.add(new JScrollPane(table),BorderLayout.CENTER);p.add(south,BorderLayout.SOUTH);
        loadGoals(table); return p;
    }

    private void loadGoals(JTable table) {
        try(Connection con=Database.connect();PreparedStatement ps=con.prepareStatement(
          "SELECT goal_type,target_value,current_value,target_date,created_at FROM goals WHERE user_id=? ORDER BY created_at DESC")){
            ps.setLong(1,userId);
            try(ResultSet rs=ps.executeQuery()){
                DefaultTableModel m=new DefaultTableModel(new String[]{"Goal","Target","Current","Due date","Created"},0);
                while(rs.next())m.addRow(new Object[]{rs.getString(1),rs.getInt(2),rs.getInt(3),rs.getDate(4),rs.getTimestamp(5)});
                table.setModel(m);
            }
        }catch(Exception ex){error(ex);}
    }

    private JPanel historyPanel() {
        JPanel p=new JPanel(new BorderLayout(10,10));p.setBorder(new EmptyBorder(18,18,18,18));
        JTable table=new JTable(); JButton refresh=new JButton("Refresh history");
        refresh.addActionListener(e -> loadHistory(table));
        p.add(new JLabel("Your logged activities"),BorderLayout.NORTH);p.add(new JScrollPane(table),BorderLayout.CENTER);p.add(refresh,BorderLayout.SOUTH);
        loadHistory(table); return p;
    }

    private void loadHistory(JTable table) {
        try(Connection con=Database.connect();PreparedStatement ps=con.prepareStatement(
          "SELECT activity_date,activity_type,duration_minutes,calories,steps,notes FROM activities WHERE user_id=? ORDER BY activity_date DESC,id DESC")){
            ps.setLong(1,userId);
            try(ResultSet rs=ps.executeQuery()){
                DefaultTableModel m=new DefaultTableModel(new String[]{"Date","Activity","Minutes","Calories","Steps","Notes"},0);
                while(rs.next())m.addRow(new Object[]{rs.getDate(1),rs.getString(2),rs.getInt(3),rs.getInt(4),rs.getInt(5),rs.getString(6)});
                table.setModel(m);
            }
        }catch(Exception ex){error(ex);}
    }

    private void message(String text){JOptionPane.showMessageDialog(this,text,"FitTrack",JOptionPane.INFORMATION_MESSAGE);}
    private void error(Exception ex){ex.printStackTrace();JOptionPane.showMessageDialog(this,
      "Something went wrong. Check the database setup and console output.\n"+ex.getMessage(),"FitTrack error",JOptionPane.ERROR_MESSAGE);}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch(Exception ignored) {}
            new FitnessApp().setVisible(true);
        });
    }
}
