// API service for Metal Investment App
const API_BASE_URL = process.env.NODE_ENV === 'production' 
  ? 'https://metal-investment-635786220311.europe-west1.run.app'
  : 'http://localhost:8080';

class ApiService {
  constructor() {
    this.baseURL = API_BASE_URL;
  }

  // Helper method to get auth headers
  getAuthHeaders(token) {
    return {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    };
  }

  // Public API methods
  async registerUser(username, password, email) {
    const response = await fetch(`${this.baseURL}/userRegistration`, {
      method: 'POST',
      headers: {
        'username': username,
        'password': password,
        'email': email
      }
    });
    return response.json();
  }

  async login(username, password) {
    const response = await fetch(`${this.baseURL}/login`, {
      method: 'POST',
      headers: {
        'username': username,
        'password': password
      }
    });
    return response.json();
  }

  async validateAccount(username, code) {
    const response = await fetch(`${this.baseURL}/validateAccount`, {
      method: 'POST',
      headers: {
        'username': username,
        'code': code.toString()
      }
    });
    return response.json();
  }

  // Protected API methods
  async getProfit(token) {
    const response = await fetch(`${this.baseURL}/api/profit`, {
      method: 'GET',
      headers: this.getAuthHeaders(token)
    });
    return response.json();
  }

  async addAlert(token, metalSymbol, expression, frequency) {
    const response = await fetch(`${this.baseURL}/api/addAlert`, {
      method: 'POST',
      headers: {
        ...this.getAuthHeaders(token),
        'metalSymbol': metalSymbol,
        'expression': expression,
        'frequency': frequency
      }
    });
    return response.json();
  }

  async getAlerts(token) {
    const response = await fetch(`${this.baseURL}/api/getAlerts`, {
      method: 'GET',
      headers: this.getAuthHeaders(token)
    });
    return response.json();
  }

  async recordPurchase(token, metalAmount, metalSymbol, cost) {
    const response = await fetch(`${this.baseURL}/api/purchase`, {
      method: 'POST',
      headers: {
        ...this.getAuthHeaders(token),
        'metalAmount': metalAmount.toString(),
        'metalSymbol': metalSymbol,
        'cost': cost.toString()
      }
    });
    return response.json();
  }
}

export default new ApiService();
