// Centralised fetch/AJAX helper for Java Servlet endpoints
const API_BASE_URL = '/api';

async function fetchAPI(endpoint, method = 'GET', body = null) {
    const options = {
        method,
        headers: {
            'Content-Type': 'application/json'
        }
    };
    if (body) {
        options.body = JSON.stringify(body);
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
        if (!response.ok) {
            throw new Error(`API error! status: ${response.status}`);
        }
        return await response.json();
    } catch (error) {
        console.error("API Call Failed:", error);
        return null;
    }
}
