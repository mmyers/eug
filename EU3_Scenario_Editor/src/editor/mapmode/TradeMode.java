
package editor.mapmode;

import editor.MapPanel;
import editor.ProvinceData.Province;
import eug.parser.EUGFileIO;
import eug.parser.ParserSettings;
import eug.shared.FilenameResolver;
import eug.shared.GenericList;
import eug.shared.GenericObject;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * For EU4.
 * Actually should be TradeNodeMode, but that's a little too Dr. Seuss.
 * @author Michael Myers
 * @since 0.8.2
 */
public class TradeMode extends ProvincePaintingMode {
    
    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(TradeMode.class.getName());
    
    private Map<Integer, Color> tradeNodes;
    private Map<Integer, Integer> nodeMembers;
    private Map<Integer, List<List<Integer>>> outgoing;
    private Map<Integer, List<Integer>> incoming;

    private static final Color[] defaultColors = {
        Color.LIGHT_GRAY, Color.CYAN, Color.MAGENTA, Color.WHITE, Color.YELLOW,
        Color.GREEN,      Color.PINK, Color.ORANGE,  Color.RED,   Color.DARK_GRAY
    };

    public TradeMode(MapPanel panel, FilenameResolver resolver) {
        super(panel);
        GenericObject nodes = EUGFileIO.loadAll(resolver.listFiles("common/tradenodes"), ParserSettings.getQuietSettings());
        readNodes(nodes);
    }

    private void readNodes(GenericObject nodes) {
        tradeNodes = new HashMap<>();
        nodeMembers = new HashMap<>();
        outgoing = new HashMap<>();
        incoming = new HashMap<>();
        
        for (GenericObject child : nodes.children) {
            int loc = child.getInt("location");
            tradeNodes.put(loc, getColor(child.getList("color")));
            if (child.containsList("members")) {
                for (String id : child.getList("members"))
                    nodeMembers.put(Integer.parseInt(id), loc);
            } else {
                log.log(Level.WARNING, "Trade node {0} has no members", child.name);
            }
            
            if (child.containsChild("outgoing")) {
                for (GenericObject obj : child.getChildren("outgoing")) {
                    List<Integer> path = new ArrayList<>();
                    GenericList pathList = obj.getList("path");

                    if (pathList == null || pathList.size() == 0)
                        continue;

                    for (String str : pathList)
                        path.add(Integer.parseInt(str));

                    String finalTargetName = obj.getString("name");
                    GenericObject targetNode = nodes.getChild(finalTargetName);
                    if (targetNode != null) {
                        int targetLocation = targetNode.getInt("location");
                        if (!path.contains(targetLocation))
                            path.add(targetLocation);
                    }

                    int target = path.get(path.size()-1);
                    if (!incoming.containsKey(target))
                        incoming.put(target, new ArrayList<>());
                    incoming.get(target).add(loc);
                    
                    if (!outgoing.containsKey(loc))
                        outgoing.put(loc, new ArrayList<>());
                    outgoing.get(loc).add(path);
                }
            }
        }
    }

    private static Color getColor(GenericList color) {
        if (color == null || color.size() < 3)
            return defaultColors[(int)(Math.random() * defaultColors.length)];
        int r = Integer.parseInt(color.get(0));
        int g = Integer.parseInt(color.get(1));
        int b = Integer.parseInt(color.get(2));

        return new Color(r, g, b);
    }

    @Override
    protected void paintProvince(Graphics2D g, int provId) {
        Color c = tradeNodes.get(provId);
        if (c != null) {
            mapPanel.paintProvince(g, provId, c);
        } else {
            Integer node = nodeMembers.get(provId);
            if (node != null) {
                mapPanel.paintProvince(g, provId, tradeNodes.get(node).darker());
            } else if (getMap().isWasteland(provId)) {
                // color wasteland specially, otherwise it might look like large inland oceans
                mapPanel.paintProvince(g, provId, Utilities.COLOR_LAND_DEFAULT);
            }
        }
    }

    @Override
    protected void paintSeaZone(Graphics2D g, int id) {
        paintProvince(g, id);
    }

    @Override
    public Object getBorderGroup(final int provId) {
        if (tradeNodes.containsKey(provId))
            return provId;
        Integer node = nodeMembers.get(provId);
        if (node != null)
            return node;
        if (getMap().isWasteland(provId))
            return "WASTELAND";
        if (!getMap().isLand(provId))
            return "SEA_ZONE";
        return "UNKNOWN";
    }

    @Override
    protected void paintingEnded(Graphics2D g) {
        for (Integer node : tradeNodes.keySet()) {
            List<List<Integer>> targets = outgoing.get(node);
            if (targets == null)
                continue;

            int lastProv = node;
            for (List<Integer> target : targets) {
                lastProv = node;
                for (int i = 0; i < target.size(); i++) {
                    drawArrow(lastProv, target.get(i), g);
                    lastProv = target.get(i);
                }
            }
        }
    }

    private void drawArrow(int from, int to, Graphics2D g) {
        mapPanel.drawArrow(from, to, g);
    }

    @Override
    public String getTooltipExtraText(Province current) {
        if (tradeNodes.containsKey(current.getId())) {
            String inflow = null, outflow = null;
            if (incoming.containsKey(current.getId())) {
                List<Integer> others = incoming.get(current.getId());
                StringBuilder sb = new StringBuilder();
                for (Integer i : others) {
                    sb.append(mapPanel.getModel().getProvinceData().getProvByID(i).getName());
                    sb.append(", ");
                }
                sb.delete(sb.length() - 2, sb.length());
                inflow = "Trade flows from: " + sb;
            } else {
                inflow = "No trade inflow";
            }
            if (outgoing.containsKey(current.getId())) {
                List<List<Integer>> others = outgoing.get(current.getId());
                StringBuilder sb = new StringBuilder();
                for (List<Integer> path : others) {
                    sb.append(mapPanel.getModel().getProvinceData().getProvByID(path.get(path.size()-1)).getName());
                    sb.append(", ");
                }
                sb.delete(sb.length() - 2, sb.length());
                outflow = "Trade flows to: " + sb;
            } else {
                outflow = "No trade outflow";
            }
            return "Trade Node<br>" + inflow + "<br>" + outflow;
        } else if (nodeMembers.containsKey(current.getId()))
            return "Trades in " + mapPanel.getModel().getProvinceData().getProvByID(nodeMembers.get(current.getId())).getName();
        else if (getMap().isWasteland(current.getId()))
            return "Wasteland";
        else if(getMap().isLand(current.getId()))
            return "Unknown trade node";
        else
            return super.getTooltipExtraText(current);
    }
}
