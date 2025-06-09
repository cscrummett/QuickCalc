package com.quickcalc.services;

import com.quickcalc.models.BeamModel;
import com.quickcalc.models.Load;
import com.quickcalc.models.Support;

/**
 * Simple test class to verify BeamDataService functionality
 * Note: This is a basic test without JUnit framework
 */
public class BeamDataServiceTest {
    
    public static void main(String[] args) {
        BeamDataServiceTest test = new BeamDataServiceTest();
        test.runTests();
    }
    
    public void runTests() {
        System.out.println("=== BeamDataService Tests ===");
        
        testCreateSampleBeam();
        testCreateEmptyBeam();
        testCreateSimpleBeam();
        testCreateCantileverBeam();
        
        System.out.println("All tests completed!");
    }
    
    private void testCreateSampleBeam() {
        System.out.println("Testing createSampleBeam...");
        
        BeamDataService service = new BeamDataService();
        BeamModel beam = service.createSampleBeam();
        
        assert beam != null : "Sample beam should not be null";
        assert beam.getLength() == 50.0 : "Sample beam length should be 50.0";
        assert beam.getSupports().size() == 3 : "Sample beam should have 3 supports";
        assert beam.getLoads().size() == 5 : "Sample beam should have 5 loads";
        
        System.out.println("✓ createSampleBeam test passed");
    }
    
    private void testCreateEmptyBeam() {
        System.out.println("Testing createEmptyBeam...");
        
        BeamDataService service = new BeamDataService();
        BeamModel beam = service.createEmptyBeam(30.0);
        
        assert beam != null : "Empty beam should not be null";
        assert beam.getLength() == 30.0 : "Empty beam length should be 30.0";
        
        System.out.println("✓ createEmptyBeam test passed");
    }
    
    private void testCreateSimpleBeam() {
        System.out.println("Testing createSimpleBeam...");
        
        BeamDataService service = new BeamDataService();
        BeamModel beam = service.createSimpleBeam(20.0);
        
        assert beam != null : "Simple beam should not be null";
        assert beam.getLength() == 20.0 : "Simple beam length should be 20.0";
        assert beam.getSupports().size() == 2 : "Simple beam should have 2 supports";
        assert beam.getSupports().get(0).getType() == Support.Type.PINNED : "First support should be pinned";
        assert beam.getSupports().get(1).getType() == Support.Type.ROLLER : "Second support should be roller";
        
        System.out.println("✓ createSimpleBeam test passed");
    }
    
    private void testCreateCantileverBeam() {
        System.out.println("Testing createCantileverBeam...");
        
        BeamDataService service = new BeamDataService();
        BeamModel beam = service.createCantileverBeam(15.0);
        
        assert beam != null : "Cantilever beam should not be null";
        assert beam.getLength() == 15.0 : "Cantilever beam length should be 15.0";
        assert beam.getSupports().size() == 1 : "Cantilever beam should have 1 support";
        assert beam.getSupports().get(0).getType() == Support.Type.FIXED : "Support should be fixed";
        
        System.out.println("✓ createCantileverBeam test passed");
    }
}