"""
Test suite for data module.
"""
import unittest
import os
import json
from ..backend.fea import BeamAnalyzer, Node, Element, PointLoad
from ..backend.data import BeamData

class TestData(unittest.TestCase):
    def setUp(self):
        """Set up test data."""
        # Create test beam analyzer
        self.test_length = 20.0  # 20 feet
        self.test_elements = 4
        self.test_E = 29000.0  # ksi
        self.test_I = 100.0  # in^4
        
        self.beam_analyzer = BeamAnalyzer(
            length=self.test_length,
            num_elements=self.test_elements,
            E=self.test_E,
            I=self.test_I
        )
        
        # Add test nodes
        self.beam_analyzer.nodes = [
            Node(x=0.0, fixed=True),  # Fixed support
            Node(x=120.0),  # Midpoint
            Node(x=240.0, pinned=True)  # Pinned support
        ]
        
        # Add test elements
        self.beam_analyzer.elements = [
            Element(node1_idx=0, node2_idx=1, length=120.0, E=self.test_E, I=self.test_I),
            Element(node1_idx=1, node2_idx=2, length=120.0, E=self.test_E, I=self.test_I)
        ]
        
        # Add test load
        self.beam_analyzer.loads = [
            PointLoad(position=10.0, magnitude=10.0)  # 10 kips at 10 feet
        ]
        
        # Create test filename
        self.test_filename = 'test_beam_data.json'
        
    def tearDown(self):
        """Clean up test files."""
        if os.path.exists(self.test_filename):
            os.remove(self.test_filename)
    
    def test_save_load_cycle(self):
        """Test saving and loading beam data."""
        # Create BeamData instance
        beam_data = BeamData()
        
        # Update from beam analyzer
        beam_data.update_from_beam_analyzer(self.beam_analyzer)
        
        # Save to file
        beam_data.save_to_file(self.test_filename)
        
        # Load from file
        loaded_data = BeamData()
        loaded_data.load_from_file(self.test_filename)
        
        # Create new beam analyzer from loaded data
        loaded_analyzer = loaded_data.create_beam_analyzer()
        
        # Verify properties match
        self.assertEqual(loaded_analyzer.length / 12.0, self.test_length)
        self.assertEqual(loaded_analyzer.num_elements, self.test_elements)
        self.assertEqual(loaded_analyzer.E, self.test_E)
        self.assertEqual(loaded_analyzer.I, self.test_I)
        
        # Verify nodes match
        self.assertEqual(len(loaded_analyzer.nodes), len(self.beam_analyzer.nodes))
        for loaded_node, original_node in zip(loaded_analyzer.nodes, self.beam_analyzer.nodes):
            self.assertEqual(loaded_node.x, original_node.x)
            self.assertEqual(loaded_node.fixed, original_node.fixed)
            self.assertEqual(loaded_node.pinned, original_node.pinned)
        
        # Verify elements match
        self.assertEqual(len(loaded_analyzer.elements), len(self.beam_analyzer.elements))
        for loaded_element, original_element in zip(loaded_analyzer.elements, self.beam_analyzer.elements):
            self.assertEqual(loaded_element.node1_idx, original_element.node1_idx)
            self.assertEqual(loaded_element.node2_idx, original_element.node2_idx)
            self.assertEqual(loaded_element.length, original_element.length)
            self.assertEqual(loaded_element.E, original_element.E)
            self.assertEqual(loaded_element.I, original_element.I)
        
        # Verify loads match
        self.assertEqual(len(loaded_analyzer.loads), len(self.beam_analyzer.loads))
        for loaded_load, original_load in zip(loaded_analyzer.loads, self.beam_analyzer.loads):
            self.assertEqual(loaded_load.position, original_load.position)
            self.assertEqual(loaded_load.magnitude, original_load.magnitude)
    
    def test_json_structure(self):
        """Test JSON structure of saved data."""
        # Create BeamData instance
        beam_data = BeamData()
        
        # Update from beam analyzer
        beam_data.update_from_beam_analyzer(self.beam_analyzer)
        
        # Save to file
        beam_data.save_to_file(self.test_filename)
        
        # Read JSON file and verify structure
        with open(self.test_filename, 'r') as f:
            data = json.load(f)
            
            # Verify top-level keys
            self.assertIn('length', data)
            self.assertIn('num_elements', data)
            self.assertIn('E', data)
            self.assertIn('I', data)
            self.assertIn('nodes', data)
            self.assertIn('elements', data)
            self.assertIn('loads', data)
            
            # Verify nodes structure
            self.assertIsInstance(data['nodes'], list)
            for node in data['nodes']:
                self.assertIn('x', node)
                self.assertIn('fixed', node)
                self.assertIn('pinned', node)
            
            # Verify elements structure
            self.assertIsInstance(data['elements'], list)
            for element in data['elements']:
                self.assertIn('node1_idx', element)
                self.assertIn('node2_idx', element)
                self.assertIn('length', element)
                self.assertIn('E', element)
                self.assertIn('I', element)
            
            # Verify loads structure
            self.assertIsInstance(data['loads'], list)
            for load in data['loads']:
                self.assertIn('position', load)
                self.assertIn('magnitude', load)

if __name__ == '__main__':
    unittest.main()
