// API service for Metal Investment App
const API_BASE_URL = process.env.NODE_ENV === 'production' 
  ? 'https://metal-investment-635786220311.europe-west1.run.app'
  : 'http://localhost:8080';

class ApiService {
  constructor() {
    this.baseURL = API_BASE_URL;
    this.csrfRefreshPromise = null;
    this.csrfHeaderName = 'X-XSRF-TOKEN';
    this.csrfCookieName = 'XSRF-TOKEN';
    this.csrfToken = null;
  }

  async getUiBanner() {
    try {
      const response = await fetch(`${this.baseURL}/api/ui-banner`, {
        method: 'GET',
        headers: {
          'Accept': 'application/json'
        },
        mode: 'cors',
        credentials: 'include'
      });

      if (response.status === 204) {
        return '';
      }

      if (!response.ok) {
        console.warn('Failed to fetch UI banner:', response.status);
        return '';
      }

      const data = await this.parseJsonSafely(response);
      const message = data && typeof data.message === 'string' ? data.message.trim() : '';
      return message;
    } catch (error) {
      console.error('Error while fetching UI banner:', error);
      return '';
    }
  }

  getCookieValue(name) {
    if (typeof document === 'undefined' || !document.cookie) {
      return null;
    }
    const cookies = document.cookie.split(';');
    for (const cookie of cookies) {
      const trimmed = cookie.trim();
      if (trimmed.startsWith(`${name}=`)) {
        return decodeURIComponent(trimmed.substring(name.length + 1));
      }
    }
    return null;
  }

  syncCsrfFromCookies() {
    const tokenFromCookie =
      this.getCookieValue(this.csrfCookieName) ||
      this.getCookieValue('X-XSRF-TOKEN') ||
      this.getCookieValue('_csrf');
    if (tokenFromCookie) {
      this.csrfToken = tokenFromCookie;
    }
    return this.csrfToken;
  }

  updateCsrfMetadata(data) {
    if (data && data.headerName) {
      this.csrfHeaderName = data.headerName;
    }
    if (data && data.token) {
      this.csrfToken = data.token;
    }
    return this.syncCsrfFromCookies();
  }

  async ensureCsrfToken() {
    const existingToken = this.syncCsrfFromCookies();
    if (existingToken) {
      return existingToken;
    }
    const refreshed = await this.refreshCsrfToken();
    if (refreshed) {
      return this.syncCsrfFromCookies();
    }
    return this.csrfToken;
  }

  // Helper method to get CSRF token from cookies
  getCsrfToken() {
    try {
      console.log('=== CSRF TOKEN FROM COOKIES ===');
      console.log('All cookies:', document.cookie);

      const csrfToken = this.syncCsrfFromCookies();

      console.log('CSRF token from cookies:', csrfToken ? 'FOUND' : 'NOT FOUND');
      console.log('CSRF token value:', csrfToken ? csrfToken.substring(0, 10) + '...' : 'null');

      if (csrfToken) {
        console.log('=== CSRF TOKEN FROM COOKIES SUCCESS ===');
        return csrfToken;
      } else {
        console.warn('No CSRF token found in cookies');
        console.log('=== CSRF TOKEN FROM COOKIES FAILED ===');
        return null;
      }
    } catch (error) {
      console.error('Failed to get CSRF token from cookies:', error);
      console.log('=== CSRF TOKEN FROM COOKIES ERROR ===');
      return null;
    }
  }

  // Force refresh CSRF token by calling the endpoint
  async forceRefreshCsrfToken() {
    try {
      console.log('=== FORCE REFRESH CSRF TOKEN ===');
      const response = await fetch(`${this.baseURL}/csrf-token`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        mode: 'cors',
        credentials: 'include'
      });
      
      console.log('Force refresh response status:', response.status);
      console.log('Force refresh response headers:', Object.fromEntries(response.headers.entries()));
      
      if (response.ok) {
        const data = await response.json();
        console.log('Force refresh response data:', data);
        console.log('Cookies after force refresh:', document.cookie);
        this.updateCsrfMetadata(data);
        return true;
      } else {
        const errorText = await response.text();
        console.error('Force refresh failed:', response.status, errorText);
        return false;
      }
    } catch (error) {
      console.error('Force refresh error:', error);
      return false;
    }
  }

  // Helper method to refresh CSRF token by making a request to get a new one
  async refreshCsrfToken() {
    if (this.csrfRefreshPromise) {
      console.log('CSRF token refresh already in progress, waiting for existing request...');
      return this.csrfRefreshPromise;
    }

    this.csrfRefreshPromise = (async () => {
      try {
        console.log('=== REFRESHING CSRF TOKEN ===');
        console.log('Calling /csrf-token endpoint...');
        
        const response = await fetch(`${this.baseURL}/csrf-token`, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
          },
          mode: 'cors',
          credentials: 'include'
        });
        
        console.log('CSRF token refresh response status:', response.status);
        console.log('CSRF token refresh response headers:', Object.fromEntries(response.headers.entries()));
        
        if (response.ok) {
          const data = await response.json();
          console.log('CSRF token refresh response data:', data);
          console.log('CSRF token refreshed successfully');
          this.updateCsrfMetadata(data);
          
          // Check if cookies were set
          console.log('Cookies after CSRF token request:', document.cookie);
          
          return true;
        } else {
          const errorText = await response.text();
          console.error('Failed to refresh CSRF token:', response.status, errorText);
          return false;
        }
      } catch (error) {
        console.error('Error refreshing CSRF token:', error);
        return false;
      } finally {
        this.csrfRefreshPromise = null;
      }
    })();

    return this.csrfRefreshPromise;
  }

  // Helper method to get auth headers (synchronous version without CSRF)
  getAuthHeaders(token) {
    return {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    };
  }

  // Helper method to get auth headers with CSRF token (async version)
  async getAuthHeadersWithCsrf(token) {
    console.log('getAuthHeadersWithCsrf called with token:', token ? 'EXISTS' : 'MISSING');
    
    const headers = {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    };
    
    console.log('Base headers created:', Object.keys(headers));
    
    try {
      const csrfToken = await this.ensureCsrfToken();
      console.log('CSRF token result:', csrfToken ? 'SUCCESS' : 'FAILED');
      
      if (csrfToken) {
        headers[this.csrfHeaderName] = csrfToken;
        console.log(`CSRF token added to headers (${this.csrfHeaderName}):`, csrfToken.substring(0, 10) + '...');
      } else {
        console.warn('No CSRF token available - request may fail with 403');
      }
    } catch (error) {
      console.error('Error ensuring CSRF token:', error);
      console.warn('Proceeding without CSRF token - request may fail with 403');
    }
    
    console.log('Final headers for API request:', Object.keys(headers));
    return headers;
  }

  // Helper method to get token from sessionStorage
  getToken() {
    return localStorage.getItem('userToken');
  }

  // Helper method to get auth headers with token from storage (synchronous)
  getAuthHeadersFromStorage() {
    const token = this.getToken();
    return token ? this.getAuthHeaders(token) : {};
  }

  // Helper method to get auth headers with CSRF token from storage (async)
  async getAuthHeadersFromStorageWithCsrf() {
    const token = this.getToken();
    console.log('getAuthHeadersFromStorageWithCsrf - Token from storage:', token ? 'EXISTS' : 'MISSING');
    console.log('getAuthHeadersFromStorageWithCsrf - Token value:', token ? token.substring(0, 20) + '...' : 'null');
    
    if (!token) {
      console.warn('No user token found - user may not be logged in');
      return {};
    }

    return await this.getAuthHeadersWithCsrf(token);
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
      const headers = {
        'username': username,
        'password': password,
        'email': email,
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      };
      
      // Add CSRF token if available
      const csrfToken = await this.ensureCsrfToken();
      if (csrfToken) {
        headers[this.csrfHeaderName] = csrfToken;
      }
      
      const response = await fetch(`${this.baseURL}/userRegistration`, {
        method: 'POST',
        headers: headers,
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
        ...await this.getAuthHeadersFromStorageWithCsrf(),
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
        ...await this.getAuthHeadersFromStorageWithCsrf(),
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
        ...await this.getAuthHeadersFromStorageWithCsrf(),
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
    try {
      console.log('=== PURCHASE REQUEST START ===');
      console.log('Attempting to record purchase with CSRF protection');
      console.log('Purchase data:', { metalAmount, metalSymbol, cost });
      
      // Force refresh CSRF token before making the request
      console.log('Force refreshing CSRF token...');
      await this.forceRefreshCsrfToken();
      
      const headers = await this.getAuthHeadersFromStorageWithCsrf();
      console.log('Headers for purchase request:', Object.keys(headers));
      console.log('Headers details:', headers);
      
      const fullHeaders = {
        ...headers,
        'metalAmount': metalAmount.toString(),
        'metalSymbol': metalSymbol,
        'cost': cost.toString(),
        'Accept': 'application/json'
      };
      
      console.log('Full headers being sent:', fullHeaders);
      console.log('Request URL:', `${this.baseURL}/api/purchase`);
      const csrfHeader = this.csrfHeaderName;
      console.log('CSRF token in headers:', fullHeaders[csrfHeader] ? 'PRESENT' : 'MISSING');
      console.log('CSRF token value:', fullHeaders[csrfHeader] ? fullHeaders[csrfHeader].substring(0, 20) + '...' : 'null');
      console.log('Authorization header:', fullHeaders['Authorization'] ? 'PRESENT' : 'MISSING');
      
      const response = await fetch(`${this.baseURL}/api/purchase`, {
        method: 'POST',
        headers: fullHeaders,
        mode: 'cors',
        credentials: 'include'
      });
      
      console.log('Purchase response status:', response.status);
      console.log('Purchase response headers:', Object.fromEntries(response.headers.entries()));
      
      if (!response.ok) {
        const rawText = await response.text();
        console.error('Purchase failed:', response.status, rawText);
        
        // If 403 Forbidden, try to refresh CSRF token and retry once
        if (response.status === 403) {
          console.log('403 Forbidden - attempting to refresh CSRF token and retry...');
          
          // Try to get a fresh CSRF token
          const refreshSuccess = await this.forceRefreshCsrfToken();
          
          if (refreshSuccess) {
            console.log('CSRF token refreshed, retrying purchase...');
            // Retry the request with refreshed CSRF token
            const retryHeaders = await this.getAuthHeadersFromStorageWithCsrf();
            console.log('Retry headers:', Object.keys(retryHeaders));
            const retryCsrfHeader = this.csrfHeaderName;
            console.log('Retry CSRF token:', retryHeaders[retryCsrfHeader] ? retryHeaders[retryCsrfHeader].substring(0, 20) + '...' : 'null');
            
            const retryResponse = await fetch(`${this.baseURL}/api/purchase`, {
              method: 'POST',
              headers: {
                ...retryHeaders,
                'metalAmount': metalAmount.toString(),
                'metalSymbol': metalSymbol,
                'cost': cost.toString(),
                'Accept': 'application/json'
              },
              mode: 'cors',
              credentials: 'include'
            });
            
            console.log('Retry response status:', retryResponse.status);
            
            if (retryResponse.ok) {
              console.log('Purchase successful after CSRF token refresh');
              return await this.parseJsonSafely(retryResponse);
            } else {
              const retryErrorText = await retryResponse.text();
              console.error('Purchase still failed after CSRF token refresh:', retryResponse.status, retryErrorText);
            }
          } else {
            console.error('Failed to refresh CSRF token');
          }
        }
        
        let message = rawText;
        try {
          if (rawText) {
            const json = JSON.parse(rawText);
            message = json.message || json.error || rawText;
          }
        } catch (e) {
          // Keep original message if JSON parsing fails
        }
        const error = new Error(message);
        error.status = response.status;
        throw error;
      }
      
      return await this.parseJsonSafely(response);
    } catch (error) {
      console.error('Purchase request failed:', error);
      throw error;
    }
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
    const authHeaders = await this.getAuthHeadersFromStorageWithCsrf();
    console.log('Sell API - Auth headers:', authHeaders);
    console.log('Sell API - Token from storage:', this.getToken());
    
    const response = await fetch(`${this.baseURL}/api/sell`, {
      method: 'DELETE',
      headers: {
        ...authHeaders,
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
    const authHeaders = await this.getAuthHeadersFromStorageWithCsrf();
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
        ...await this.getAuthHeadersFromStorageWithCsrf(),
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
        ...await this.getAuthHeadersFromStorageWithCsrf(),
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
        ...await this.getAuthHeadersFromStorageWithCsrf(),
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

  /**
   * Logout the current user from the application.
   */
  async logout() {
    try {
      const authHeaders = await this.getAuthHeadersFromStorageWithCsrf();
      const response = await fetch(`${this.baseURL}/api/logout`, {
        method: 'POST',
        headers: authHeaders,
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
        const customError = new Error(errorData.message || 'Failed to logout');
        customError.data = errorData;
        throw customError;
      }

      return await this.parseJsonSafely(response);
    } catch (error) {
      console.error('Logout error:', error);
      const customError = new Error(error.message || 'Network error occurred');
      customError.data = error.data || { message: 'Failed to logout' };
      throw customError;
    }
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
