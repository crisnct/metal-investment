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
    try {
      const response = await fetch(`${this.baseURL}/userRegistration`, {
        method: 'POST',
        headers: {
          'username': username,
          'password': password,
          'email': email,
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        mode: 'cors',
        credentials: 'include'
      });
      
      if (!response.ok) {
        let errorData = null;
        let errorMessage = `HTTP error! status: ${response.status}`;
        
        try {
          // Try to parse as JSON first
          errorData = await response.json();
          console.log('Backend JSON response for registerUser:', errorData); // Debug log
          if (errorData.message) {
            errorMessage = errorData.message;
          } else if (errorData.error) {
            errorMessage = errorData.error;
          } else {
            // If no specific error field, show the entire response
            errorMessage = JSON.stringify(errorData);
          }
        } catch (jsonError) {
          console.log('JSON parsing failed for registerUser, trying text response'); // Debug log
          // If JSON parsing fails, try to get text response
          try {
            const textResponse = await response.text();
            console.log('Backend text response for registerUser:', textResponse); // Debug log
            errorMessage = textResponse || errorMessage;
            errorData = { message: textResponse };
          } catch (textError) {
            console.error('Failed to parse error response:', textError);
          }
        }
        
        const error = new Error(errorMessage);
        error.response = response;
        error.data = errorData;
        error.status = response.status;
        throw error;
      }
      
      return await response.json();
    } catch (error) {
      // If it's already our custom error, re-throw it
      if (error.response) {
        throw error;
      }
      
      // If it's a network error or other issue, create a proper error
      const customError = new Error(error.message || 'Network error occurred');
      customError.originalError = error;
      customError.isNetworkError = true;
      throw customError;
    }
  }

  async login(username, password) {
    try {
      const response = await fetch(`${this.baseURL}/login`, {
        method: 'POST',
        headers: {
          'username': username,
          'password': password,
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        mode: 'cors',
        credentials: 'include'
      });
      
      if (!response.ok) {
        let errorData = null;
        let errorMessage = `HTTP error! status: ${response.status}`;
        
        try {
          // Try to parse as JSON first
          errorData = await response.json();
          if (errorData.message) {
            errorMessage = errorData.message;
          } else if (errorData.error) {
            errorMessage = errorData.error;
          }
        } catch (jsonError) {
          // If JSON parsing fails, try to get text response
          try {
            const textResponse = await response.text();
            errorMessage = textResponse || errorMessage;
            errorData = { message: textResponse };
          } catch (textError) {
            console.error('Failed to parse error response:', textError);
          }
        }
        
        const error = new Error(errorMessage);
        error.response = response;
        error.data = errorData;
        error.status = response.status;
        throw error;
      }
      
      return await response.json();
    } catch (error) {
      // If it's already our custom error, re-throw it
      if (error.response) {
        throw error;
      }
      
      // If it's a network error or other issue, create a proper error
      const customError = new Error(error.message || 'Network error occurred');
      customError.originalError = error;
      customError.isNetworkError = true;
      throw customError;
    }
  }

  async validateAccount(username, code) {
    try {
      const response = await fetch(`${this.baseURL}/validateAccount`, {
        method: 'POST',
        headers: {
          'username': username,
          'code': code.toString(),
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        mode: 'cors',
        credentials: 'include'
      });
      
      if (!response.ok) {
        let errorData = null;
        let errorMessage = `HTTP error! status: ${response.status}`;
        
        try {
          // Try to parse as JSON first
          errorData = await response.json();
          if (errorData.message) {
            errorMessage = errorData.message;
          } else if (errorData.error) {
            errorMessage = errorData.error;
          }
        } catch (jsonError) {
          // If JSON parsing fails, try to get text response
          try {
            const textResponse = await response.text();
            errorMessage = textResponse || errorMessage;
            errorData = { message: textResponse };
          } catch (textError) {
            console.error('Failed to parse error response:', textError);
          }
        }
        
        const error = new Error(errorMessage);
        error.response = response;
        error.data = errorData;
        error.status = response.status;
        throw error;
      }
      
      return await response.json();
    } catch (error) {
      // If it's already our custom error, re-throw it
      if (error.response) {
        throw error;
      }
      
      // If it's a network error or other issue, create a proper error
      const customError = new Error(error.message || 'Network error occurred');
      customError.originalError = error;
      customError.isNetworkError = true;
      throw customError;
    }
  }

  async checkUserPendingValidation(username, email) {
    try {
      const response = await fetch(`${this.baseURL}/checkUserPendingValidation`, {
        method: 'POST',
        headers: {
          'username': username,
          'email': email,
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        mode: 'cors',
        credentials: 'include'
      });
      
      if (!response.ok) {
        let errorData = null;
        let errorMessage = `HTTP error! status: ${response.status}`;
        
        try {
          errorData = await response.json();
          if (errorData.message) {
            errorMessage = errorData.message;
          } else if (errorData.error) {
            errorMessage = errorData.error;
          }
        } catch (jsonError) {
          try {
            const textResponse = await response.text();
            errorMessage = textResponse || errorMessage;
            errorData = { message: textResponse };
          } catch (textError) {
            console.error('Failed to parse error response:', textError);
          }
        }
        
        const error = new Error(errorMessage);
        error.response = response;
        error.data = errorData;
        error.status = response.status;
        throw error;
      }
      
      return await response.json();
    } catch (error) {
      if (error.response) {
        throw error;
      }
      
      const customError = new Error(error.message || 'Network error occurred');
      customError.originalError = error;
      customError.isNetworkError = true;
      throw customError;
    }
  }

  async resendValidationEmail(username, email) {
    try {
      const response = await fetch(`${this.baseURL}/resendValidationEmail`, {
        method: 'POST',
        headers: {
          'username': username,
          'email': email,
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        mode: 'cors',
        credentials: 'include'
      });
      
      if (!response.ok) {
        let errorData = null;
        let errorMessage = `HTTP error! status: ${response.status}`;
        
        try {
          errorData = await response.json();
          if (errorData.message) {
            errorMessage = errorData.message;
          } else if (errorData.error) {
            errorMessage = errorData.error;
          }
        } catch (jsonError) {
          try {
            const textResponse = await response.text();
            errorMessage = textResponse || errorMessage;
            errorData = { message: textResponse };
          } catch (textError) {
            console.error('Failed to parse error response:', textError);
          }
        }
        
        const error = new Error(errorMessage);
        error.response = response;
        error.data = errorData;
        error.status = response.status;
        throw error;
      }
      
      return await response.json();
    } catch (error) {
      if (error.response) {
        throw error;
      }
      
      const customError = new Error(error.message || 'Network error occurred');
      customError.originalError = error;
      customError.isNetworkError = true;
      throw customError;
    }
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

const apiService = new ApiService();

export default apiService;
