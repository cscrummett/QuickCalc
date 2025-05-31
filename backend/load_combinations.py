"""
Load combinations module for structural analysis.
Implements ASCE 7 load combinations and custom combinations.
"""
from dataclasses import dataclass, field
from typing import Dict, List, Optional
from enum import Enum
import json

class LoadType(Enum):
    """Types of loads for load combinations."""
    DEAD = "dead"  # Dead load (D)
    LIVE = "live"  # Live load (L)
    ROOF_LIVE = "roof_live"  # Roof live load (Lr)
    SNOW = "snow"  # Snow load (S)
    RAIN = "rain"  # Rain load (R)
    WIND = "wind"  # Wind load (W)
    SEISMIC = "seismic"  # Seismic load (E)
    
    @classmethod
    def get_abbreviation(cls, load_type):
        """Get standard abbreviation for load type."""
        abbrev = {
            cls.DEAD: "D",
            cls.LIVE: "L",
            cls.ROOF_LIVE: "Lr",
            cls.SNOW: "S",
            cls.RAIN: "R",
            cls.WIND: "W",
            cls.SEISMIC: "E"
        }
        return abbrev.get(load_type, "?")

@dataclass
class LoadCase:
    """Represents a load case with a specific type and loads."""
    load_type: LoadType
    point_loads: List = field(default_factory=list)
    distributed_loads: List = field(default_factory=list)
    
    def to_dict(self):
        """Convert to dictionary for JSON serialization."""
        return {
            "load_type": self.load_type.value,
            "point_loads": [
                {
                    "position": load.position,
                    "magnitude": load.magnitude
                } for load in self.point_loads
            ],
            "distributed_loads": [
                {
                    "start_pos": load.start_pos,
                    "end_pos": load.end_pos,
                    "start_magnitude": load.start_magnitude,
                    "end_magnitude": load.end_magnitude
                } for load in self.distributed_loads
            ]
        }
    
    @classmethod
    def from_dict(cls, data, point_load_class, distributed_load_class):
        """Create from dictionary."""
        load_case = cls(
            load_type=LoadType(data["load_type"])
        )
        
        load_case.point_loads = [
            point_load_class(
                position=load["position"],
                magnitude=load["magnitude"]
            ) for load in data.get("point_loads", [])
        ]
        
        load_case.distributed_loads = [
            distributed_load_class(
                start_pos=load["start_pos"],
                end_pos=load["end_pos"],
                start_magnitude=load["start_magnitude"],
                end_magnitude=load["end_magnitude"]
            ) for load in data.get("distributed_loads", [])
        ]
        
        return load_case

@dataclass
class LoadCombination:
    """Represents a load combination with factors for each load type."""
    name: str
    factors: Dict[LoadType, float] = field(default_factory=dict)
    
    def get_factor(self, load_type: LoadType) -> float:
        """Get the factor for a specific load type."""
        return self.factors.get(load_type, 0.0)
    
    def get_description(self) -> str:
        """Get a human-readable description of the load combination."""
        parts = []
        for load_type, factor in self.factors.items():
            if abs(factor) > 1e-6:  # Non-zero factor
                abbrev = LoadType.get_abbreviation(load_type)
                parts.append(f"{factor:.1f}{abbrev}")
        
        return " + ".join(parts) if parts else "No loads"
    
    def to_dict(self):
        """Convert to dictionary for JSON serialization."""
        return {
            "name": self.name,
            "factors": {load_type.value: factor for load_type, factor in self.factors.items()}
        }
    
    @classmethod
    def from_dict(cls, data):
        """Create from dictionary."""
        combination = cls(name=data["name"])
        combination.factors = {LoadType(k): v for k, v in data.get("factors", {}).items()}
        return combination

class LoadCombinationManager:
    """Manages load combinations for structural analysis."""
    
    def __init__(self):
        self.load_cases = {}
        self.combinations = []
        self._initialize_default_combinations()
    
    def _initialize_default_combinations(self):
        """Initialize default ASCE 7 load combinations."""
        # ASCE 7-16 basic load combinations (Section 2.3.1)
        self.combinations = [
            # Default (service) load combination
            LoadCombination(
                name="Service",
                factors={
                    LoadType.DEAD: 1.0,
                    LoadType.LIVE: 1.0,
                    LoadType.ROOF_LIVE: 1.0,
                    LoadType.SNOW: 1.0,
                    LoadType.RAIN: 1.0,
                    LoadType.WIND: 1.0,
                    LoadType.SEISMIC: 1.0
                }
            ),
            # ASCE 7-16 combinations
            LoadCombination(
                name="ASCE 7: 1.4D",
                factors={LoadType.DEAD: 1.4}
            ),
            LoadCombination(
                name="ASCE 7: 1.2D + 1.6L + 0.5(Lr/S/R)",
                factors={
                    LoadType.DEAD: 1.2,
                    LoadType.LIVE: 1.6,
                    LoadType.ROOF_LIVE: 0.5,
                    LoadType.SNOW: 0.5,
                    LoadType.RAIN: 0.5
                }
            ),
            LoadCombination(
                name="ASCE 7: 1.2D + 1.6(Lr/S/R) + (L/0.5W)",
                factors={
                    LoadType.DEAD: 1.2,
                    LoadType.LIVE: 1.0,
                    LoadType.ROOF_LIVE: 1.6,
                    LoadType.SNOW: 1.6,
                    LoadType.RAIN: 1.6,
                    LoadType.WIND: 0.5
                }
            ),
            LoadCombination(
                name="ASCE 7: 1.2D + 1.0W + L + 0.5(Lr/S/R)",
                factors={
                    LoadType.DEAD: 1.2,
                    LoadType.LIVE: 1.0,
                    LoadType.ROOF_LIVE: 0.5,
                    LoadType.SNOW: 0.5,
                    LoadType.RAIN: 0.5,
                    LoadType.WIND: 1.0
                }
            ),
            LoadCombination(
                name="ASCE 7: 1.2D + 1.0E + L + 0.2S",
                factors={
                    LoadType.DEAD: 1.2,
                    LoadType.LIVE: 1.0,
                    LoadType.SNOW: 0.2,
                    LoadType.SEISMIC: 1.0
                }
            ),
            LoadCombination(
                name="ASCE 7: 0.9D + 1.0W",
                factors={
                    LoadType.DEAD: 0.9,
                    LoadType.WIND: 1.0
                }
            ),
            LoadCombination(
                name="ASCE 7: 0.9D + 1.0E",
                factors={
                    LoadType.DEAD: 0.9,
                    LoadType.SEISMIC: 1.0
                }
            )
        ]
    
    def add_load_case(self, load_type: LoadType, load_case):
        """Add a load case for a specific load type."""
        self.load_cases[load_type] = load_case
    
    def add_custom_combination(self, name: str, factors: Dict[LoadType, float]):
        """Add a custom load combination."""
        combination = LoadCombination(name=name, factors=factors)
        self.combinations.append(combination)
        return combination
    
    def get_combined_loads(self, combination_name: str):
        """
        Get combined loads for a specific combination.
        
        Returns:
            Tuple of (point_loads, distributed_loads) with factors applied
        """
        from copy import deepcopy
        from backend.fea import PointLoad, DistributedLoad
        
        # Find the requested combination
        combination = next((c for c in self.combinations if c.name == combination_name), None)
        if not combination:
            raise ValueError(f"Load combination '{combination_name}' not found")
        
        combined_point_loads = []
        combined_distributed_loads = []
        
        # Apply factors to each load case
        for load_type, load_case in self.load_cases.items():
            factor = combination.get_factor(load_type)
            if abs(factor) < 1e-6:  # Skip if factor is essentially zero
                continue
            
            # Apply factor to point loads
            for load in load_case.point_loads:
                new_load = PointLoad(
                    position=load.position,
                    magnitude=load.magnitude * factor
                )
                combined_point_loads.append(new_load)
            
            # Apply factor to distributed loads
            for load in load_case.distributed_loads:
                new_load = DistributedLoad(
                    start_pos=load.start_pos,
                    end_pos=load.end_pos,
                    start_magnitude=load.start_magnitude * factor,
                    end_magnitude=load.end_magnitude * factor
                )
                combined_distributed_loads.append(new_load)
        
        return combined_point_loads, combined_distributed_loads
    
    def to_dict(self):
        """Convert to dictionary for JSON serialization."""
        return {
            "load_cases": {
                load_type.value: load_case.to_dict() 
                for load_type, load_case in self.load_cases.items()
            },
            "combinations": [combo.to_dict() for combo in self.combinations]
        }
    
    def save_to_file(self, filename: str):
        """Save load combinations to a JSON file."""
        with open(filename, 'w') as f:
            json.dump(self.to_dict(), f, indent=4)
    
    @classmethod
    def from_dict(cls, data, point_load_class, distributed_load_class):
        """Create from dictionary."""
        manager = cls()
        
        # Load cases
        manager.load_cases = {}
        for load_type_str, load_case_data in data.get("load_cases", {}).items():
            load_type = LoadType(load_type_str)
            load_case = LoadCase.from_dict(
                load_case_data, 
                point_load_class, 
                distributed_load_class
            )
            manager.load_cases[load_type] = load_case
        
        # Load combinations
        manager.combinations = []
        for combo_data in data.get("combinations", []):
            combination = LoadCombination.from_dict(combo_data)
            manager.combinations.append(combination)
        
        return manager
    
    @classmethod
    def load_from_file(cls, filename: str, point_load_class, distributed_load_class):
        """Load load combinations from a JSON file."""
        try:
            with open(filename, 'r') as f:
                data = json.load(f)
                return cls.from_dict(data, point_load_class, distributed_load_class)
        except FileNotFoundError:
            print(f"Warning: Load combinations file '{filename}' not found. Using defaults.")
            return cls()
        except json.JSONDecodeError:
            print(f"Error: File '{filename}' is not a valid JSON file. Using defaults.")
            return cls()
