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

  // Helper method to get token from sessionStorage
  getToken() {
    return localStorage.getItem('userToken');
  }

  // Helper method to get auth headers with token from storage
  getAuthHeadersFromStorage() {
    const token = this.getToken();
    return token ? this.getAuthHeaders(token) : {};
  }

  // Safely parse JSON; returns null for empty body, or { message: text } for non-JSON text
  async parseJsonSafely(response) {
    try {
      const text = await response.text();
      if (!text) return null;
      try {
        return JSON.parse(text);
      } catch (_) {
        return { message: text };
      }
    } catch (_) {
      return null;
    }
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
      
      return await this.parseJsonSafely(response);
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
      
      return await this.parseJsonSafely(response);
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
      
      return await this.parseJsonSafely(response);
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
      
      return await this.parseJsonSafely(response);
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
  async getProfit() {
    const response = await fetch(`${this.baseURL}/api/profit`, {
      method: 'GET',
      headers: {
        ...this.getAuthHeadersFromStorage(),
        'Accept': 'application/json'
      },
      mode: 'cors',
      credentials: 'include'
    });
    if (!response.ok) {
      const rawText = await response.text();
      let message = rawText;
      try {
        if (rawText) {
          const json = JSON.parse(rawText);
          message = json?.message || json?.error || rawText;
        }
      } catch (_) {
        // keep message as text
      }
      if (response.status === 401 || response.status === 403) {
        message = 'Please log in to continue.';
      }
      const error = new Error(message || 'Request failed');
      error.status = response.status;
      throw error;
    }
    return await this.parseJsonSafely(response);
  }

  async addAlert(metalSymbol, expression, frequency) {
    const response = await fetch(`${this.baseURL}/api/addAlert`, {
      method: 'POST',
      headers: {
        ...this.getAuthHeadersFromStorage(),
        'metalSymbol': metalSymbol,
        'expression': expression,
        'frequency': frequency,
        'Accept': 'application/json'
      },
      mode: 'cors',
      credentials: 'include'
    });
    if (!response.ok) {
      const rawText = await response.text();
      let message = rawText;
      try {
        if (rawText) {
          const json = JSON.parse(rawText);
          message = json?.message || json?.error || rawText;
        }
      } catch (_) {}
      if (response.status === 401 || response.status === 403) {
        message = 'Please log in to continue.';
      }
      const error = new Error(message || 'Request failed');
      error.status = response.status;
      throw error;
    }
    return await this.parseJsonSafely(response);
  }

  async getAlerts() {
    const response = await fetch(`${this.baseURL}/api/getAlerts`, {
      method: 'GET',
      headers: {
        ...this.getAuthHeadersFromStorage(),
        'Accept': 'application/json'
      },
      mode: 'cors',
      credentials: 'include'
    });
    if (!response.ok) {
      const rawText = await response.text();
      let message = rawText;
      try {
        if (rawText) {
          const json = JSON.parse(rawText);
          message = json?.message || json?.error || rawText;
        }
      } catch (_) {}
      if (response.status === 401 || response.status === 403) {
        message = 'Please log in to continue.';
      }
      const error = new Error(message || 'Request failed');
      error.status = response.status;
      throw error;
    }
    return await this.parseJsonSafely(response);
  }

  async recordPurchase(metalAmount, metalSymbol, cost) {
    const response = await fetch(`${this.baseURL}/api/purchase`, {
      method: 'POST',
      headers: {
        ...this.getAuthHeadersFromStorage(),
        'metalAmount': metalAmount.toString(),
        'metalSymbol': metalSymbol,
        'cost': cost.toString(),
        'Accept': 'application/json'
      },
      mode: 'cors',
      credentials: 'include'
    });
    if (!response.ok) {
      const rawText = await response.text();
      let message = rawText;
      try {
        if (rawText) {
          const json = JSON.parse(rawText);
          message = json?.message || json?.error || rawText;
        }
      } catch (_) {}
      if (response.status === 401 || response.status === 403) {
        message = 'Please log in to continue.';
      }
      const error = new Error(message || 'Request failed');
      error.status = response.status;
      throw error;
    }
    return await this.parseJsonSafely(response);
  }

  async resetPassword(email) {
    try {
      const response = await fetch(`${this.baseURL}/resetPassword`, {
        method: 'POST',
        headers: {
          'email': email,
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        mode: 'cors',
        credentials: 'include'
      });

      if (!response.ok) {
        let errorData;
        try {
          errorData = await response.json();
        } catch {
          errorData = { message: await response.text() };
        }
        const customError = new Error(errorData.message || 'Failed to reset password');
        customError.data = errorData;
        throw customError;
      }

      return await this.parseJsonSafely(response);
    } catch (error) {
      console.error('Reset password error:', error);
      const customError = new Error(error.message || 'Network error occurred');
      customError.data = error.data || { message: 'Failed to reset password' };
      throw customError;
    }
  }

  async changePassword(token, code, newPassword, email) {
    try {
      const response = await fetch(`${this.baseURL}/changePassword`, {
        method: 'PUT',
        headers: {
          'token': token,
          'code': code,
          'newPassword': newPassword,
          'email': email,
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        mode: 'cors',
        credentials: 'include'
      });

      if (!response.ok) {
        let errorData;
        try {
          errorData = await response.json();
        } catch {
          errorData = { message: await response.text() };
        }
        const customError = new Error(errorData.message || 'Failed to change password');
        customError.data = errorData;
        throw customError;
      }

      return await this.parseJsonSafely(response);
    } catch (error) {
      console.error('Change password error:', error);
      const customError = new Error(error.message || 'Network error occurred');
      customError.data = error.data || { message: 'Failed to change password' };
      throw customError;
    }
  }

  async sellMetal(amount, symbol, price) {
    const authHeaders = this.getAuthHeadersFromStorage();
    console.log('Sell API - Auth headers:', authHeaders);
    console.log('Sell API - Token from storage:', this.getToken());
    
    const response = await fetch(`${this.baseURL}/api/sell`, {
      method: 'DELETE',
      headers: {
        ...this.getAuthHeadersFromStorage(),
        'metalAmount': amount.toString(),
        'metalSymbol': symbol,
        'price': price.toString(),
        'Accept': 'application/json'
      },
      mode: 'cors',
      credentials: 'include'
    });

    if (!response.ok) {
      const errorData = await this.parseJsonSafely(response);
      const errorMessage = this.getFriendlyErrorMessage(response.status, errorData);
      const error = new Error(errorMessage);
      error.response = response;
      error.data = errorData;
      error.status = response.status;
      throw error;
    }

    return await this.parseJsonSafely(response);
  }

  async getNotificationPeriod() {
    const authHeaders = this.getAuthHeadersFromStorage();
    console.log('Get Notification API - Auth headers:', authHeaders);
    console.log('Get Notification API - Token from storage:', this.getToken());
    
    const response = await fetch(`${this.baseURL}/api/getNotificationPeriod`, {
      method: 'GET',
      headers: {
        ...authHeaders,
        'Accept': 'application/json'
      },
      mode: 'cors',
      credentials: 'include'
    });

    if (!response.ok) {
      const errorData = await this.parseJsonSafely(response);
      const errorMessage = this.getFriendlyErrorMessage(response.status, errorData);
      const error = new Error(errorMessage);
      error.response = response;
      error.data = errorData;
      error.status = response.status;
      throw error;
    }

    return await this.parseJsonSafely(response);
  }

  async setNotificationPeriod(days) {
    console.log('Set Notification API - Sending period:', days);
    console.log('Set Notification API - Period type:', typeof days);
    
    const response = await fetch(`${this.baseURL}/api/setNotificationPeriod`, {
      method: 'PUT',
      headers: {
        ...this.getAuthHeadersFromStorage(),
        'period': days.toString(),
        'Accept': 'application/json'
      },
      mode: 'cors',
      credentials: 'include'
    });

    if (!response.ok) {
      const errorData = await this.parseJsonSafely(response);
      const errorMessage = this.getFriendlyErrorMessage(response.status, errorData);
      const error = new Error(errorMessage);
      error.response = response;
      error.data = errorData;
      error.status = response.status;
      throw error;
    }

    return await this.parseJsonSafely(response);
  }

  // Account deletion methods
  async deleteAccountPreparation() {
    const response = await fetch(`${this.baseURL}/api/deleteAccountPreparation`, {
      method: 'POST',
      headers: {
        ...this.getAuthHeadersFromStorage(),
        'Accept': 'application/json'
      },
      mode: 'cors',
      credentials: 'include'
    });

    if (!response.ok) {
      const errorData = await this.parseJsonSafely(response);
      const errorMessage = this.getFriendlyErrorMessage(response.status, errorData);
      const error = new Error(errorMessage);
      error.response = response;
      error.data = errorData;
      error.status = response.status;
      throw error;
    }

    return await this.parseJsonSafely(response);
  }

  async deleteAccount(password, code) {
    const response = await fetch(`${this.baseURL}/api/deleteAccount`, {
      method: 'DELETE',
      headers: {
        ...this.getAuthHeadersFromStorage(),
        'password': password,
        'code': code,
        'Accept': 'application/json'
      },
      mode: 'cors',
      credentials: 'include'
    });

    if (!response.ok) {
      const errorData = await this.parseJsonSafely(response);
      const errorMessage = this.getFriendlyErrorMessage(response.status, errorData);
      const error = new Error(errorMessage);
      error.response = response;
      error.data = errorData;
      error.status = response.status;
      throw error;
    }

    return await this.parseJsonSafely(response);
  }

  getFriendlyErrorMessage(status, errorData) {
    if (status === 401 || status === 403) {
      return 'Please log in to continue.';
    }
    if (errorData && errorData.message) {
      return errorData.message;
    }
    return 'Request failed';
  }
}

const apiService = new ApiService();

export default apiService;
