/*
 * PositionsEditor.java
 *
 * Created on May 14, 2007, 11:02 AM
 */

package posed;

//import javax.swing.UIManager;

import eug.parser.EUGFileIO;
import eug.parser.ParserSettings;
import eug.shared.GenericList;
import eug.shared.GenericObject;
import eug.shared.Style;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;


/**
 *
 * @author Michael Myers
 */
public class PositionsEditor {

    private GenericObject config;
    
    /** Creates a new instance of PositionsEditor */
    public PositionsEditor(GenericObject config) {
        if (config == null)
            config = new GenericObject();
        this.config = config;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
//        if (UIManager.getSystemLookAndFeelClassName().contains("windows")) {
//            try {
//                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//            } catch (Exception ex) {
//                // just don't do anything
//            }
//        }
        
        //PositionsEditorUI ui = null;
        
        File configFile = new File("config.txt");
        GenericObject config =
                EUGFileIO.load(configFile, ParserSettings.getDefaults().setPrintTimingInfo(false));

        PositionsEditor editor = new PositionsEditor(config);
        editor.showDialog();
    }

    private void showDialog() {
        final JDialog dialog = new JDialog((java.awt.Frame)null, "Choose game", true);
        dialog.setLayout(new BorderLayout());

        JPanel filePanel = new JPanel();
        filePanel.setBorder(BorderFactory.createTitledBorder("Map file"));

        final JTextField fileField = new JTextField();
        fileField.setPreferredSize(new Dimension(400, 24));
        JButton browseButton = new JButton("Browse...");
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser(fileField.getText());
                chooser.setDialogTitle("Select map file");
                chooser.setFileFilter(new MapFileFilter());

                int choice = chooser.showOpenDialog(dialog);
                if (choice == JFileChooser.APPROVE_OPTION) {
                    fileField.setText(chooser.getSelectedFile().getAbsolutePath());
                }
            }
        });

        filePanel.add(fileField);
        filePanel.add(browseButton);
        dialog.add(filePanel, BorderLayout.CENTER);

        JPanel lowerPanel = new JPanel(new BorderLayout());
        JPanel gameTypePanel = new JPanel();
        final JComboBox gameBox = new JComboBox(GameVersion.getGameVersions().toArray());
        
        gameBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (config.getChild("recent") != null) {
                    GameVersion version = (GameVersion) e.getItem();
                    GenericList recent = config.getChild("recent").getList(version.getName());
                    if (recent != null && recent.size() > 0)
                        fileField.setText(recent.get(0));
                }
            }
        });

        gameTypePanel.add(new JLabel("Game type:"));
        gameTypePanel.add(gameBox);
        lowerPanel.add(gameTypePanel, BorderLayout.WEST);

        final JCheckBox useLocCheckBox = new JCheckBox("Use localized names", config.getBoolean("use_localization"));
        lowerPanel.add(useLocCheckBox);

        JPanel buttonPanel = new JPanel();
        JButton okButton = new JButton("  OK  ");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String mapFile = new File(fileField.getText()).getAbsolutePath();

                GameVersion version = (GameVersion) gameBox.getSelectedItem();
                config.setString("game", version.getName());
                
                config.setBoolean("use_localization", useLocCheckBox.isSelected());

                GenericObject recent = config.getChild("recent");
                if (recent == null)
                    recent = config.createChild("recent");
                GenericList gameRecent = recent.getList(version.getName());
                if (gameRecent == null)
                    gameRecent = recent.createList(version.getName());
                if (!gameRecent.contains(mapFile))
                    gameRecent.insert(mapFile, false, 0);

                EUGFileIO.save(config, "config.txt", null, true, Style.EU3_SAVE_GAME);

                dialog.dispose();
                
                PositionsEditorUI ui = new PositionsEditorUI(mapFile, version, useLocCheckBox.isSelected());
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

    private static final class MapFileFilter extends FileFilter {

        public MapFileFilter() {
        }

        @Override
        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase(Locale.ENGLISH).endsWith(".map");
        }

        @Override
        public String getDescription() {
            return "Map files (*.map)";
        }
    }
}
