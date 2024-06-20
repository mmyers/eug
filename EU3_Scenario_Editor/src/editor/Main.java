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
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author Michael Myers
 */
public class Main {
    
    private static final Logger log = Logger.getLogger(Main.class.getName());
    
    private final GenericObject config;
    
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
        
        setUpLogging();
        
        log.log(Level.INFO, "Java version: {0}", System.getProperty("java.version"));
        log.log(Level.INFO, "Editor version: {0}", Version.getCurrentVersion());
        
        Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
            log.log(Level.SEVERE, "Uncaught exception", e);
        });
        
        File configFile = new File("config.txt");
        GenericObject config =
                EUGFileIO.loadUTF8(configFile, ParserSettings.getDefaults().setPrintTimingInfo(false));
        if (config == null)
            config = new GenericObject(); // don't crash if no config.txt present
        
        Main main = new Main(config);
        main.showDialog();
    }
    
    private static void setUpLogging() {
        java.util.logging.Handler handler;
        try {
            handler = new java.util.logging.FileHandler("output.log", false);
            handler.setFormatter(new java.util.logging.SimpleFormatter());
            Logger.getLogger("").addHandler(handler);
        } catch (IOException | SecurityException ex) {
            System.err.println("Failed to set up log file. Defaulting to console output.");
        }
    }
    
    private boolean isValidGameDir(String dirName) {
        File f = new File(dirName);
        if (f.isDirectory()) {
            File history = new File(f.getAbsolutePath() + File.separator + "history");
            File common = new File(f.getAbsolutePath() + File.separator + "common");
            if (history.isDirectory() && common.isDirectory())
                return true;
        }
        return false;
    }
    
    private void setValidationLabel(JLabel label, boolean isValid) {
        if (isValid) {
            label.setForeground(Color.GREEN.darker());
            label.setText("\u2705");
            label.setToolTipText("This is a valid game directory");
        } else {
            label.setForeground(Color.RED);
            label.setText("\u274E");
            label.setToolTipText("<html>This is not a valid game directory.<br>"
                    + "For most games, navigate to the directory that holds the game .exe file.<br>"
                    + "For newer games such as Crusader Kings 3, navigate to the /game directory inside the main installation.</html>");
        }
    }
    
    private boolean isValidSaveGame(String fileName) {
        return fileName.isEmpty() || new File(fileName).isFile();
    }
    
    private void setSaveGameValidationLabel(JLabel label, boolean isValid) {
        if (isValid) {
            label.setForeground(Color.GREEN.darker());
            label.setText("\u2705");
        } else {
            label.setForeground(Color.RED);
            label.setText("\u274E");
        }
    }
    
    private void showDialog() {
        final JComboBox<Mod> modBox = new JComboBox<>(); // referenced in several places
        
        @SuppressWarnings("UseOfObsoleteCollectionType")
        final JComboBox<GameVersion> gameBox = new JComboBox<>(new Vector<>(GameVersion.getGameVersions()));

        final JDialog dialog = new JDialog((java.awt.Frame)null, "Choose game", true);
        dialog.setLayout(new BorderLayout());

        JPanel filePanel = new JPanel();
        filePanel.setLayout(new GridLayout(0, 1));

        JPanel gameDirPanel = new JPanel();
        gameDirPanel.setBorder(BorderFactory.createTitledBorder("Game folder (NOT mod folder)"));
        final JLabel validLabel = new JLabel();
        validLabel.setPreferredSize(new Dimension(20, 20));
        setValidationLabel(validLabel, false); // blank is invalid
        
        final JTextField gameDirField = new JTextField();
        gameDirField.setPreferredSize(new Dimension(500, 24));
        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser(gameDirField.getText());
                chooser.setDialogTitle("Select game folder");
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

                int choice = chooser.showOpenDialog(dialog);
                if (choice == JFileChooser.APPROVE_OPTION) {
                    String mainPath = chooser.getSelectedFile().getAbsolutePath();
                    gameDirField.setText(mainPath);
                    setValidationLabel(validLabel, isValidGameDir(mainPath));
                    
                    String modPath = chooser.getSelectedFile().getAbsolutePath() + File.separator + "mod";
                    
                    @SuppressWarnings("UseOfObsoleteCollectionType")
                    Vector<Mod> mods = listMods(new File(modPath), true);
                    modBox.setModel(new DefaultComboBoxModel<>(mods));
                }
            }
        });
        gameDirField.getDocument().addDocumentListener(new DocumentListener() {
            private void checkMods() {
                File dir = new File(gameDirField.getText());
                if (isValidGameDir(dir.getAbsolutePath())) {
                    setValidationLabel(validLabel, true);
                    String modPath = dir.getAbsolutePath() + File.separator + "mod";
                    
                    @SuppressWarnings("UseOfObsoleteCollectionType")
                    Vector<Mod> mods = listMods(new File(modPath), true);
                    GameVersion version = (GameVersion)gameBox.getSelectedItem();
                    if (version != null && version.getModPath() != null) {
                        File documents = new javax.swing.JFileChooser().getFileSystemView().getDefaultDirectory();
                        mods.addAll(listMods(new File(documents.getAbsolutePath() + File.separator + version.getModPath() + File.separator + "mod"), false));
                    }
                    modBox.setModel(new DefaultComboBoxModel<>(mods));
                }
                else
                    setValidationLabel(validLabel, false);
            }
            @Override
            public void insertUpdate(DocumentEvent de) {
                // insertUpdate is called on a paste event
                // maybe the user is pasting the game directory in?
                checkMods();
            }

            @Override
            public void removeUpdate(DocumentEvent de) {
                checkMods();
            }
            
            @Override
            public void changedUpdate(DocumentEvent de) {
                checkMods();
            }
        });

        gameDirPanel.add(gameDirField);
        gameDirPanel.add(validLabel);
        gameDirPanel.add(browseButton);
        filePanel.add(gameDirPanel);

        
        JPanel saveGamePanel = new JPanel();
        saveGamePanel.setBorder(BorderFactory.createTitledBorder("Saved Game"));
        
        final JTextField saveGameField = new JTextField();
        saveGameField.setPreferredSize(new Dimension(500, 24));
        
        final JLabel saveGameValidationLabel = new JLabel();
        saveGameValidationLabel.setPreferredSize(new Dimension(20, 20));
        setSaveGameValidationLabel(saveGameValidationLabel, true); // blank is valid
        
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
                setSaveGameValidationLabel(saveGameValidationLabel, isValidSaveGame(saveGameField.getText()));
            }
        });
        
        saveGameField.getDocument().addDocumentListener(new DocumentListener() {
            private void doCheck() {
                setSaveGameValidationLabel(saveGameValidationLabel, isValidSaveGame(saveGameField.getText()));
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                doCheck();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                doCheck();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                doCheck();
            }
        });

        saveGamePanel.add(saveGameField);
        saveGamePanel.add(saveGameValidationLabel);
        saveGamePanel.add(browseSaveButton);
        filePanel.add(saveGamePanel);

        dialog.add(filePanel, BorderLayout.CENTER);

        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel gameTypePanel = new JPanel();
        
        gameBox.addItemListener((ItemEvent e) -> {
            if (e.getStateChange() == ItemEvent.DESELECTED)
                return;
            
            modBox.setModel(new DefaultComboBoxModel<>());
            gameDirField.setText("");
            saveGameField.setText("");
            
            if (config.getChild(PATHS_KEY) != null) {
                GameVersion version = (GameVersion) e.getItem();
                GenericObject saved = config.getChild(PATHS_KEY).getChild(version.getName());
                if (saved != null && saved.size() > 0) {
                    String mainDir = saved.getString("main");
                    gameDirField.setText(mainDir);
                    
                    if (!mainDir.isEmpty()) {
                        String modPath = new File(mainDir).getAbsolutePath() + File.separator + "mod";
                        Vector<Mod> mods = listMods(new File(modPath), true);
                        if (version.getModPath() != null) {
                            File documents = new javax.swing.JFileChooser().getFileSystemView().getDefaultDirectory();
                            mods.addAll(listMods(new File(documents.getAbsolutePath() + File.separator + version.getModPath() + File.separator + "mod"), false));
                        }
                        modBox.setModel(new DefaultComboBoxModel<>(mods));
                        
                        if (!saved.getString(LAST_MOD_KEY).isEmpty()) {
                            for (Mod mod : mods) {
                                if (mod.getName().equals(saved.getString(LAST_MOD_KEY))) {
                                    modBox.setSelectedItem(mod);
                                    break;
                                }
                            }
                        }
                        
                        saveGameField.setText(saved.getString(RECENT_KEY));
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
                if (config.getChild(RECENT_KEY) != null) {
                    GameVersion version = (GameVersion) e.getItem();
                    GenericList recent = config.getChild(RECENT_KEY).getList(version.getName());
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
                if (!isValidGameDir(gameDirField.getText()) || !isValidSaveGame(saveGameField.getText()))
                    return;
                
                dialog.getRootPane().getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                dialog.getRootPane().getGlassPane().setVisible(true);
                
                log.log(Level.INFO, "**************************************************");
                
                String saveFile = null;
                if (!saveGameField.getText().isEmpty()) {
                    File f = new File(saveGameField.getText());
                    if (!f.isDirectory())
                        saveFile = f.getAbsolutePath();
                }

                GameVersion version = (GameVersion) gameBox.getSelectedItem();
                config.setString("game", version.getName());

                GenericObject paths = config.getChild(PATHS_KEY);
                if (paths == null)
                    paths = config.createChild(PATHS_KEY);
                GenericObject game = paths.getChild(version.getName());
                if (game == null)
                    game = paths.createChild(version.getName());

                game.setString("main", gameDirField.getText(), true);
                if (saveFile != null)
                    game.setString(RECENT_KEY, saveFile, true);
                else
                    game.setString(RECENT_KEY, "", true);
                Mod mod = (Mod) modBox.getSelectedItem();
                game.setString(LAST_MOD_KEY, mod.getName().equals("None") ? "" : mod.getName(), true);

                EUGFileIO.saveUTF8(config, "config.txt", true, Style.AGCEEP, ParserSettings.getQuietSettings());
                
                log.log(Level.INFO, "Loading {0}. Mod: {1}", new Object[]{version.getDisplay(), mod.getName()});
                log.log(Level.INFO, "Game path: {0}", gameDirField.getText());
                log.log(Level.INFO, "Mod path: {0}", (mod.getName().equals("None") ? "(none)" : mod.getModPath()));

                FilenameResolver resolver = new FilenameResolver(gameDirField.getText());
                resolver.setClausewitz2Mod(version.isNewStyleMod());
                if (version.isNewStyleMod()) {
                    resolver.setModDirectory(mod.getName().equals("None") ? "" : mod.getModPath());
                    resolver.setModFileName(mod.getModFilePath());
                } else {
                    resolver.setModName(mod.getName().equals("None") ? "" : mod.getModPath());
                }
                resolver.ignoreFileType(".info");

                boolean checkForUpdates = true;
                if (config.hasString("check_for_updates"))
                    checkForUpdates = config.getBoolean("check_for_updates");
                EditorUI ui = startEditor(saveFile, version, resolver, checkForUpdates);
                
                dialog.dispose();
                ui.setVisible(true);
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
    private static final String RECENT_KEY = "recent";
    private static final String LAST_MOD_KEY = "lastmod";
    private static final String PATHS_KEY = "paths";

    private EditorUI startEditor(String saveFile, GameVersion version, FilenameResolver resolver, boolean checkUpdates) {
        try {
            Text.initText(resolver, version);
        } catch (FileNotFoundException ex) {
            log.log(Level.SEVERE, "Failed to read localization", ex);
        } catch (IOException ex) {
            log.log(Level.SEVERE, "Failed to read localization", ex);
        } catch (OutOfMemoryError er) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Out of memory. Please see readme.txt for information on solving this.",
                    "Out of Memory",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        editor.mapmode.Utilities.init(resolver);

        try {
            EditorUI ui = new EditorUI(saveFile, version, resolver, checkUpdates);
            return ui;
        } catch (OutOfMemoryError er) {
            javax.swing.JOptionPane.showMessageDialog(null,
                    "Out of memory. Please see readme.txt for information on solving this.",
                    "Out of Memory",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        return null;
    }
    
    // returns a Vector to use in a combo box
    @SuppressWarnings("UseOfObsoleteCollectionType")
    private static Vector<Mod> listMods(File moddir, boolean includeNone) {
        log.log(Level.INFO, "Checking for mods in {0}", moddir.getAbsolutePath());
        File[] mods = moddir.listFiles((File pathname) -> {
            if (pathname.isDirectory())
                return false;
            String name = pathname.getName();
            return name.endsWith(".mod");
            //&& new File(pathname.getParentFile().getPath() + File.separator + name.substring(0, name.length()-4)).exists();
        });

        if (mods == null)
            mods = new File[] {};
        
        Vector<Mod> ret = new Vector<>(mods.length + 1);

        if (includeNone) {
            ret.add(new Mod("None", moddir.getParent(), moddir.getParent()));
        }
        
        for (File f : mods) {
            GenericObject obj = EUGFileIO.load(f, ParserSettings.getQuietSettings());
            String modPath;
            if (obj.contains("path")) {
                // detect if path is absolute - fixes #28
                if (new java.io.File(obj.getString("path")).isAbsolute())
                    modPath = obj.getString("path");
                else
                    modPath = moddir.getParent() + File.separator + obj.getString("path");
            } else if (obj.contains("archive")) {
                log.log(Level.INFO, "Archive mods are not yet supported. Mod \"{0}\" skipped.", obj.getString("name"));
                continue;
            } else {
                // if no explicit path, turn /mod/ModName.mod into /mod/ModName
                String fileNameWithoutExtension = f.getName().substring(0, f.getName().length()-4);
                modPath = moddir + File.separator + fileNameWithoutExtension;
            }
            
            Mod mod = new Mod(obj.getString("name"), f.getAbsolutePath(), modPath);
            log.log(Level.INFO, "Mod: {0}; file path: {1}; mod path: {2}", new Object[]{mod.name, mod.modFilePath, mod.modPath});
            ret.add(mod);
        }
        return ret;
    }


    private static class Mod {
        private final String name;
        private final String modFilePath;
        private final String modPath;
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
