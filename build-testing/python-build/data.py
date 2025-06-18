"""
Data handling module for beam analysis project.
Handles JSON serialization/deserialization of beam data.
"""
import json
from typing import Dict, List, Optional
from pathlib import Path
from fea import Node, Element, PointLoad

class BeamData:
    """Class for handling beam data serialization and deserialization."""
    
    def __init__(self):
        self.beam_data = {
            'length': 0.0,  # Beam length in feet
            'num_elements': 0,  # Number of elements
            'E': 29000.0,  # Default Young's modulus in ksi
            'I': 100.0,  # Default moment of inertia in in^4
            'nodes': [],  # List of Node objects
            'elements': [],  # List of Element objects
            'loads': [],  # List of PointLoad objects
        }
    
    def to_dict(self) -> Dict:
        """Convert beam data to a dictionary for JSON serialization."""
        return {
            'length': self.beam_data['length'],
            'num_elements': self.beam_data['num_elements'],
            'E': self.beam_data['E'],
            'I': self.beam_data['I'],
            'nodes': [{
                'x': node.x,
                'fixed': node.fixed,
                'pinned': node.pinned
            } for node in self.beam_data['nodes']],
            'elements': [{
                'node1_idx': element.node1_idx,
                'node2_idx': element.node2_idx,
                'length': element.length,
                'E': element.E,
                'I': element.I
            } for element in self.beam_data['elements']],
            'loads': [{
                'position': load.position,
                'magnitude': load.magnitude
            } for load in self.beam_data['loads']]
        }
    
    def from_dict(self, data: Dict) -> None:
        """Load beam data from a dictionary."""
        self.beam_data = {
            'length': data.get('length', 0.0),
            'num_elements': data.get('num_elements', 0),
            'E': data.get('E', 29000.0),
            'I': data.get('I', 100.0),
            'nodes': [
                Node(
                    x=node['x'],
                    fixed=node.get('fixed', False),
                    pinned=node.get('pinned', False)
                ) for node in data.get('nodes', [])
            ],
            'elements': [
                Element(
                    node1_idx=element['node1_idx'],
                    node2_idx=element['node2_idx'],
                    length=element['length'],
                    E=element['E'],
                    I=element['I']
                ) for element in data.get('elements', [])
            ],
            'loads': [
                PointLoad(
                    position=load['position'],
                    magnitude=load['magnitude']
                ) for load in data.get('loads', [])
            ]
        }
    
    def save_to_file(self, filename: str) -> None:
        """Save beam data to a JSON file."""
        with open(filename, 'w') as f:
            json.dump(self.to_dict(), f, indent=4)
    
    def load_from_file(self, filename: str) -> None:
        """Load beam data from a JSON file."""
        try:
            with open(filename, 'r') as f:
                data = json.load(f)
                self.from_dict(data)
        except FileNotFoundError:
            print(f"Error: File '{filename}' not found.")
        except json.JSONDecodeError:
            print(f"Error: File '{filename}' is not a valid JSON file.")

    def update_from_beam_analyzer(self, beam_analyzer) -> None:
        """Update beam data from a BeamAnalyzer instance."""
        self.beam_data = {
            'length': beam_analyzer.length / 12.0,  # Convert back to feet
            'num_elements': beam_analyzer.num_elements,
            'E': beam_analyzer.E,
            'I': beam_analyzer.I,
            'nodes': beam_analyzer.nodes,
            'elements': beam_analyzer.elements,
            'loads': beam_analyzer.loads
        }

    def create_beam_analyzer(self) -> 'BeamAnalyzer':
        """Create a BeamAnalyzer instance from beam data."""
        from fea import BeamAnalyzer
        analyzer = BeamAnalyzer(
            length=self.beam_data['length'],
            num_elements=self.beam_data['num_elements'],
            E=self.beam_data['E'],
            I=self.beam_data['I']
        )
        
        # Add nodes
        for node in self.beam_data['nodes']:
            analyzer.nodes.append(node)
        
        # Add elements
        for element in self.beam_data['elements']:
            analyzer.elements.append(element)
        
        # Add loads
        for load in self.beam_data['loads']:
            analyzer.loads.append(load)
        
        return analyzer
