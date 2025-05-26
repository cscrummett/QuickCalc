# QuickCalc

A cross-platform desktop application for structural engineers to quickly analyze and design single and multi-span beams. Built with Python (backend) and Electron (frontend).

## Features

- Finite element analysis for single and multi-span beams (up to 5 spans)
- Compliance with AISC 360, ASCE 7, NDS, or custom design criteria
- Modern, trustworthy UI with vibrant 2D diagrams
- Project-based file saving
- PDF/CSV report export

## Development Setup

### Prerequisites

- Python 3.10+
- Node.js 16+
- Git

### Installation

1. Clone the repository:
```bash
git clone [repository-url]
cd quickcalc
```

2. Install Python dependencies:
```bash
pip install -r requirements.txt
```

3. Install Node.js dependencies:
```bash
npm install
```

### Running the Application

1. Start the Python backend:
```bash
python backend/app.py
```

2. In a new terminal, start the Electron frontend:
```bash
npm start
```

## License

This project is licensed under the MIT License - see the LICENSE file for details. 