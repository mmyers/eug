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
                EUGFileIO.load(configFile, ParserSettings.getDefaults().setPrintTimingInfo(false));
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
    
    private void showDialog() {
        final JComboBox<Mod> modBox = new JComboBox<>(); // referenced in several places

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
                    
                    @SuppressWarnings("UseOfObsoleteCollectionType")
                    Vector<Mod> mods = listMods(new File(modPath), true);
                    modBox.setModel(new DefaultComboBoxModel<>(mods));
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
        
        @SuppressWarnings("UseOfObsoleteCollectionType")
        final JComboBox<GameVersion> gameBox = new JComboBox<>(new Vector<>(GameVersion.getGameVersions()));
        
        gameBox.addItemListener((ItemEvent e) -> {
            if (e.getStateChange() == ItemEvent.DESELECTED)
                return;
            
            modBox.setModel(new DefaultComboBoxModel<>());
            gameDirField.setText("");
            saveGameField.setText("");
            
            if (config.getChild("paths") != null) {
                GameVersion version = (GameVersion) e.getItem();
                GenericObject saved = config.getChild("paths").getChild(version.getName());
                if (saved != null && saved.size() > 0) {
                    String mainDir = saved.getString("main");
                    gameDirField.setText(mainDir);
                    
                    if (!mainDir.isEmpty()) {
                        String modPath = new File(mainDir).getAbsolutePath() + File.separator + "mod";
                        Vector<Mod> mods = listMods(new File(modPath), true);
                        if (version.getModPath() != null) {
                            File documents = new javax.swing.JFileChooser().getFileSystemView().getDefaultDirectory();
                            mods.addAll(listMods(new File(documents.getAbsolutePath() + "/" + version.getModPath() + "/mod"), false));
                        }
                        modBox.setModel(new DefaultComboBoxModel<>(mods));
                        
                        if (!saved.getString("lastmod").isEmpty()) {
                            for (Mod mod : mods) {
                                if (mod.getName().equals(saved.getString("lastmod"))) {
                                    modBox.setSelectedItem(mod);
                                    break;
                                }
                            }
                        }
                        
                        saveGameField.setText(saved.getString("recent"));
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
                log.log(Level.INFO, "**************************************************");
                
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
                else
                    game.setString("recent", "", true);
                Mod mod = (Mod) modBox.getSelectedItem();
                game.setString("lastmod", mod.getName().equals("None") ? "" : mod.getName(), true);

                EUGFileIO.save(config, "config.txt", null, true, Style.AGCEEP);

                dialog.dispose();

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
                startEditor(saveFile, version, resolver, checkForUpdates);
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

    private void startEditor(String saveFile, GameVersion version, FilenameResolver resolver, boolean checkUpdates) {
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
            String modPath = f.getName().substring(0, f.getName().length()-4);
            if (obj.contains("path")) {
                // detect if path is absolute - fixes #28
                if (new java.io.File(obj.getString("path")).isAbsolute())
                    modPath = obj.getString("path");
                else
                    modPath = moddir.getParent() + File.separator + obj.getString("path");
            } else if (obj.contains("archive")) {
                log.log(Level.INFO, "Archive mods are not yet supported. Mod \"{0}\" skipped.", obj.getString("name"));
                continue;
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
