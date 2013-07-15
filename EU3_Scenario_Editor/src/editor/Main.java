/*
 * Main.java
 *
 * Created on January 25, 2007, 4:40 PM
 */

package editor;

import eug.parser.EUGFileIO;
import eug.parser.ParserSettings;
import eug.shared.FilenameResolver;
import eug.shared.GenericList;
import eug.shared.GenericObject;
import eug.shared.Style;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Michael Myers
 */
public class Main {
    
    private GenericObject config;
    
    /** Creates a new instance of Main */
    private Main(GenericObject config) {
        this.config = config;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        try {
//            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
        
        File configFile = new File("config.txt");
        GenericObject config =
                EUGFileIO.load(configFile, ParserSettings.getDefaults().setPrintTimingInfo(false));

        Main main = new Main(config);
        main.showDialog();
    }
    
    private void showDialog() {
        final JComboBox modBox = new JComboBox(); // referenced in several places

        final JDialog dialog = new JDialog((java.awt.Frame)null, "Choose game", true);
        dialog.setLayout(new BorderLayout());

        JPanel filePanel = new JPanel();
        filePanel.setLayout(new GridLayout(0, 1));

        JPanel gameDirPanel = new JPanel();
        gameDirPanel.setBorder(BorderFactory.createTitledBorder("Game folder"));
        final JTextField gameDirField = new JTextField();
        gameDirField.setPreferredSize(new Dimension(400, 24));
        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser(gameDirField.getText());
                chooser.setDialogTitle("Select game folder");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                int choice = chooser.showOpenDialog(dialog);
                if (choice == JFileChooser.APPROVE_OPTION) {
                    gameDirField.setText(chooser.getSelectedFile().getAbsolutePath());
                    String modPath = chooser.getSelectedFile().getAbsolutePath() + File.separator + "mod";
                    Vector<Mod> mods = listMods(new File(modPath));
                    modBox.setModel(new DefaultComboBoxModel(mods));
                }
            }
        });

        gameDirPanel.add(gameDirField);
        gameDirPanel.add(browseButton);
        filePanel.add(gameDirPanel);

        JPanel saveGamePanel = new JPanel();
        saveGamePanel.setBorder(BorderFactory.createTitledBorder("Saved Game"));
        final JTextField saveGameField = new JTextField();
        saveGameField.setPreferredSize(new Dimension(400, 24));
        JButton browseSaveButton = new JButton("Browse...");
        browseSaveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameDirField.getText().isEmpty())
                    return;

                String initialPath = saveGameField.getText();
                if (initialPath.isEmpty()) {
                    initialPath = ((Mod)modBox.getSelectedItem()).getModPath() + File.separator + "save games";
                }
                JFileChooser chooser = new JFileChooser(initialPath);
                chooser.setDialogTitle("Select saved game");
                chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

                int choice = chooser.showOpenDialog(dialog);
                if (choice == JFileChooser.APPROVE_OPTION) {
                    saveGameField.setText(chooser.getSelectedFile().getAbsolutePath());
                }
            }
        });

        saveGamePanel.add(saveGameField);
        saveGamePanel.add(browseSaveButton);
        filePanel.add(saveGamePanel);

        dialog.add(filePanel, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel gameTypePanel = new JPanel();
        final JComboBox gameBox = new JComboBox(GameVersion.getGameVersions().toArray());
        
        gameBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                modBox.setModel(new DefaultComboBoxModel());
                gameDirField.setText("");
                saveGameField.setText("");

                if (config.getChild("paths") != null) {
                    GameVersion version = (GameVersion) e.getItem();
                    GenericObject saved = config.getChild("paths").getChild(version.getName());
                    if (saved != null && saved.size() > 0) {
                        String recent = saved.getString("main");
                        gameDirField.setText(recent);

                        if (!recent.isEmpty()) {
                            String modPath = new File(recent).getAbsolutePath() + File.separator + "mod";
                            Vector<Mod> mods = listMods(new File(modPath));
                            modBox.setModel(new DefaultComboBoxModel(mods));

                            if (!saved.getString("lastmod").isEmpty()) {
                                for (Mod mod : mods) {
                                    if (mod.getName().equals(saved.getString("lastmod"))) {
                                        modBox.setSelectedItem(mod);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        });

        gameTypePanel.add(new JLabel("Game type:"));
        gameTypePanel.add(gameBox);
        topPanel.add(gameTypePanel, BorderLayout.WEST);
        
        JPanel modPanel = new JPanel();
        //final JComboBox modBox = new JComboBox(); // declaration above
        
        modBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                saveGameField.setText("");
                if (config.getChild("recent") != null) {
                    GameVersion version = (GameVersion) e.getItem();
                    GenericList recent = config.getChild("recent").getList(version.getName());
                    if (recent != null && recent.size() > 0)
                        gameDirField.setText(recent.get(0));
                }
            }
        });

        modPanel.add(new JLabel("Select mod:"));
        modPanel.add(modBox);
        topPanel.add(modPanel, BorderLayout.EAST);
        
        dialog.add(topPanel, BorderLayout.NORTH);

        JPanel lowerPanel = new JPanel(new BorderLayout());

        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("  OK  ");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (gameDirField.getText().isEmpty())
                    return;
                
                String saveFile = null;
                if (!saveGameField.getText().isEmpty()) {
                    File f = new File(saveGameField.getText());
                    if (!f.isDirectory())
                        saveFile = f.getAbsolutePath();
                }

                GameVersion version = (GameVersion) gameBox.getSelectedItem();
                config.setString("game", version.getName());

                GenericObject paths = config.getChild("paths");
                if (paths == null)
                    paths = config.createChild("paths");
                GenericObject game = paths.getChild(version.getName());
                if (game == null)
                    game = paths.createChild(version.getName());

                game.setString("main", gameDirField.getText(), true);
                if (saveFile != null)
                    game.setString("recent", saveFile, true);
                Mod mod = (Mod) modBox.getSelectedItem();
                game.setString("lastmod", mod.getName().equals("None") ? "" : mod.getName(), true);

                EUGFileIO.save(config, "config.txt", null, true, Style.AGCEEP);

                dialog.dispose();

                FilenameResolver resolver = new FilenameResolver(gameDirField.getText(), mod.getName().equals("None") ? "" : mod.getModPath());

                startEditor(saveFile, version, resolver);
            }
        });
        buttonPanel.add(okButton);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
        buttonPanel.add(cancelButton);
        lowerPanel.add(buttonPanel, BorderLayout.EAST);
        
        dialog.add(lowerPanel, BorderLayout.SOUTH);

        // pre-fill fields
        String version = config.getString("game");
        if (version.length() > 0) {
            gameBox.setSelectedItem(GameVersion.getByName(version));

//            if (config.getChild("recent") != null) {
//                GenericList recent = config.getChild("recent").getList(version);
//                if (recent != null && recent.size() > 0)
//                    fileField.setText(recent.get(0));
//            }
        }

        dialog.pack();
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private void startEditor(String saveFile, GameVersion version, FilenameResolver resolver) {
        try {
            Text.initText(resolver);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (OutOfMemoryError er) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Out of memory. Please see readme.txt for information on solving this.",
                    "Out of Memory",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        editor.mapmode.Utilities.init(resolver);

        try {
            EditorUI ui = new EditorUI(saveFile, version, resolver);
            ui.setVisible(true);
        } catch (OutOfMemoryError er) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Out of memory. Please see readme.txt for information on solving this.",
                    "Out of Memory",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
    
    // returns a Vector to use in a combo box
    private static Vector<Mod> listMods(File moddir) {
        File[] mods = moddir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                if (pathname.isDirectory())
                    return false;
                String name = pathname.getName();
                return name.endsWith(".mod")
                        && new File(pathname.getParentFile().getPath() + File.separator + name.substring(0, name.length()-4)).exists();
            }
        });

        if (mods == null)
            mods = new File[] {};
        
        Vector<Mod> ret = new Vector<Mod>(mods.length + 1);
        ret.add(new Mod("None", moddir.getParent(), moddir.getParent()));
        
        for (File f : mods) {
            GenericObject obj = EUGFileIO.load(f, ParserSettings.getQuietSettings());
            String modPath = f.getName().substring(0, f.getName().length()-4);
            Mod mod = new Mod(obj.getString("name"), f.getAbsolutePath(), modPath);
            ret.add(mod);
        }
        return ret;
    }


    private static class Mod {
        private String name;
        private String modFilePath;
        private String modPath;
        public Mod(String name, String modFilePath, String modPath) {
            this.name = name;
            this.modFilePath = modFilePath;
            this.modPath = modPath;
        }
        public String getName() {
            return name;
        }
        public String getModFilePath() {
            return modFilePath;
        }
        public String getModPath() {
            return modPath;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
