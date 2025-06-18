package com.quickcalc.services;

import com.quickcalc.models.BeamModel;
import com.quickcalc.models.Load;
import com.quickcalc.models.Support;

public class BeamDataService {
    
    public BeamModel createSampleBeam() {
        BeamModel beamModel = new BeamModel(50.0);
        
        beamModel.getSupports().clear();
        beamModel.addSupport(new Support(10.0, Support.Type.PINNED));
        beamModel.addSupport(new Support(30.0, Support.Type.ROLLER));
        beamModel.addSupport(new Support(45.0, Support.Type.ROLLER));
        
        beamModel.addLoad(new Load(5.0, -8.0, Load.Type.POINT));
        beamModel.addLoad(new Load(18.0, -10.0, Load.Type.POINT));
        beamModel.addLoad(new Load(25.0, -7.5, Load.Type.POINT));
        beamModel.addLoad(new Load(30.0, 45.0, -1.5));
        beamModel.addLoad(new Load(50.0, 25.0, Load.Type.MOMENT));
        
        return beamModel;
    }
    
    public BeamModel createEmptyBeam(double length) {
        return new BeamModel(length);
    }
    
    public BeamModel createSimpleBeam(double length) {
        BeamModel beamModel = new BeamModel(length);
        
        beamModel.getSupports().clear();
        beamModel.addSupport(new Support(0.0, Support.Type.PINNED));
        beamModel.addSupport(new Support(length, Support.Type.ROLLER));
        
        return beamModel;
    }
    
    public BeamModel createCantileverBeam(double length) {
        BeamModel beamModel = new BeamModel(length);
        
        beamModel.getSupports().clear();
        beamModel.addSupport(new Support(0.0, Support.Type.FIXED));
        
        return beamModel;
    }
}