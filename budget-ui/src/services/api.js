import axios from 'axios'

// Base API configuration
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'

const api = axios.create({
    baseURL: API_BASE_URL,
    timeout: 10000,
    headers: {
        'Content-Type': 'application/json',
    },
})

// Request interceptor to add JWT token
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => {
        return Promise.reject(error)
    }
)

// Response interceptor to handle JWT token expiration
api.interceptors.response.use(
    (response) => {
        return response
    },
    (error) => {
        if (error.response?.status === 401) {
            // Token expired or invalid
            localStorage.removeItem('token')
            window.location.href = '/login'
        }
        return Promise.reject(error)
    }
)

// Auth service
export const authService = {
    login: (email, password) =>
        api.post('/auth/login', { email, password }),

    register: (userData) =>
        api.post('/auth/register', userData),

    getCurrentUser: () =>
        api.get('/auth/me'),

    refreshToken: () =>
        api.post('/auth/refresh'),

    logout: () =>
        api.post('/auth/logout'),
}

// Budget service
export const budgetService = {
    // Dashboard data
    getDashboardData: () =>
        api.get('/budget/dashboard'),

    // Budget categories
    getCategories: () =>
        api.get('/budget/categories'),

    createCategory: (categoryData) =>
        api.post('/budget/categories', categoryData),

    updateCategory: (id, categoryData) =>
        api.put(`/budget/categories/${id}`, categoryData),

    deleteCategory: (id) =>
        api.delete(`/budget/categories/${id}`),

    // Transactions
    getTransactions: (params = {}) =>
        api.get('/budget/transactions', { params }),

    createTransaction: (transactionData) =>
        api.post('/budget/transactions', transactionData),

    updateTransaction: (id, transactionData) =>
        api.put(`/budget/transactions/${id}`, transactionData),

    deleteTransaction: (id) =>
        api.delete(`/budget/transactions/${id}`),

    // Budget reports
    getMonthlyReport: (year, month) =>
        api.get(`/budget/reports/monthly/${year}/${month}`),

    getYearlyReport: (year) =>
        api.get(`/budget/reports/yearly/${year}`),

    getCategoryReport: (categoryId, params = {}) =>
        api.get(`/budget/reports/category/${categoryId}`, { params }),
}

// User service
export const userService = {
    getProfile: () =>
        api.get('/user/profile'),

    updateProfile: (profileData) =>
        api.put('/user/profile', profileData),

    changePassword: (passwordData) =>
        api.put('/user/password', passwordData),

    deleteAccount: () =>
        api.delete('/user/account'),
}

export default api