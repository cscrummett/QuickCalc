from flask import Flask, jsonify, request

app = Flask(__name__)

@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    return jsonify({"status": "healthy"})

@app.route('/analyze', methods=['POST'])
def analyze_beam():
    """Endpoint for beam analysis"""
    # TODO: Implement beam analysis
    return jsonify({"message": "Analysis endpoint - to be implemented"})

if __name__ == '__main__':
    app.run(debug=True, port=5000) 