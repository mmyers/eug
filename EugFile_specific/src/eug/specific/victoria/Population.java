package eug.specific.victoria;

import eug.shared.GenericObject;

public class Population extends VicSpecificObject {
    
    public Population(GenericObject o, VicScenario t) {
        super(o, t);
    }
    
    public double getSize() {
        return go.getDouble("size");
    }
    
    public void setSize(double s) {
        go.setDouble("size", s);
    }
    
    public int getId() {
        return go.getInt("id");
    }
}
