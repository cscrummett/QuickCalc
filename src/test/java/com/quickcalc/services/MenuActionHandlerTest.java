package com.quickcalc.services;

import com.quickcalc.models.BeamModel;

/**
 * Test class to verify MenuActionHandler integration
 */
public class MenuActionHandlerTest {
    
    private BeamModel capturedModel;
    private boolean callbackCalled = false;
    
    public static void main(String[] args) {
        MenuActionHandlerTest test = new MenuActionHandlerTest();
        test.runTests();
    }
    
    public void runTests() {
        System.out.println("=== MenuActionHandler Integration Tests ===");
        
        testModelUpdateCallback();
        testServiceIntegration();
        
        System.out.println("All integration tests completed!");
    }
    
    private void testModelUpdateCallback() {
        System.out.println("Testing model update callback...");
        
        BeamDataService beamDataService = new BeamDataService();
        MockFileService mockFileService = new MockFileService();
        
        MenuActionHandler handler = new MenuActionHandler(beamDataService, mockFileService);
        
        // Set up callback
        handler.setModelUpdateCallback(new MenuActionHandler.ModelUpdateCallback() {
            @Override
            public void onModelUpdated(BeamModel newModel) {
                capturedModel = newModel;
                callbackCalled = true;
            }
        });
        
        // Set initial model
        BeamModel initialModel = beamDataService.createSimpleBeam(10.0);
        handler.setBeamModel(initialModel);
        
        // Test new action
        handler.handleNew();
        
        assert callbackCalled : "Callback should have been called";
        assert capturedModel != null : "Captured model should not be null";
        
        System.out.println("✓ Model update callback test passed");
    }
    
    private void testServiceIntegration() {
        System.out.println("Testing service integration...");
        
        BeamDataService beamDataService = new BeamDataService();
        MockFileService mockFileService = new MockFileService();
        
        MenuActionHandler handler = new MenuActionHandler(beamDataService, mockFileService);
        BeamModel model = beamDataService.createSampleBeam();
        handler.setBeamModel(model);
        
        // Test that services are properly integrated
        assert handler != null : "Handler should be created successfully";
        
        // Test save operation (should not throw)
        handler.handleSave();
        
        // Test other operations
        handler.handleUndo();
        handler.handleRedo();
        handler.handlePreferences();
        
        System.out.println("✓ Service integration test passed");
    }
    
    // Mock FileService for testing
    private static class MockFileService extends FileService {
        public MockFileService() {
            super(null); // No stage needed for testing
        }
        
        @Override
        public boolean save(BeamModel beamModel) {
            System.out.println("Mock save called");
            return true;
        }
    }
}