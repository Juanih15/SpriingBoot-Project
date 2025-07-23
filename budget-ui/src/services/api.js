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
// Auth service
export const authService = {
    // Update login to use correct endpoint and request format
    login: (usernameOrEmail, password) =>
        api.post('/auth/login', {
            username: usernameOrEmail,  // Backend LoginRequest expects 'username' field
            password: password
        }),

    register: (userData) =>
        api.post('/auth/register', userData),

    logout: () =>
        api.post('/auth/logout'),

    // Get current user
    getCurrentUser: () =>
        api.get('/auth/me'),

    // Password reset endpoints
    forgotPassword: (usernameOrEmail) =>
        api.post('/auth/forgot-password', null, {
            params: { usernameOrEmail }
        }),

    resetPassword: (token, newPassword) =>
        api.post('/auth/reset-password', null, {
            params: { token, newPassword }
        }),

    // Email verification
    verifyEmail: (token) =>
        api.post('/auth/verify-email', null, {
            params: { token }
        }),

    resendVerification: (usernameOrEmail) =>
        api.post('/auth/resend-verification', null, {
            params: { usernameOrEmail }
        }),
}

// Budget service
export const budgetService = {
    // Dashboard data
    getDashboardData: () =>
        api.get('/budgets/dashboard'),

    // Budgets
    createBudget: (budgetData) =>
        api.post('/budgets', budgetData),

    getUserBudgets: () =>
        api.get('/budgets'),

    getBudget: (id) =>
        api.get(`/budgets/${id}`),

    updateBudget: (id, budgetData) =>
        api.put(`/budgets/${id}`, budgetData),

    deleteBudget: (id) =>
        api.delete(`/budgets/${id}`),

    getBudgetWithExpenses: (id) =>
        api.get(`/budgets/${id}/expenses`),

    // Add expense to budget
    addExpense: (expenseData) =>
        api.post('/budgets/expenses', expenseData),

    // Monthly and yearly reports - these endpoints don't exist in controllers
    // You may need to implement these in your backend
    getMonthlyReport: (year, month) =>
        api.get(`/budget/reports/monthly/${year}/${month}`),

    getYearlyReport: (year) =>
        api.get(`/budget/reports/yearly/${year}`),

    getCategoryReport: (categoryId, params = {}) =>
        api.get(`/budget/reports/category/${categoryId}`, { params }),
}

// Category service
export const categoryService = {
    getCategories: () =>
        api.get('/categories'),

    createCategory: (categoryData) =>
        api.post('/categories', categoryData),

    updateCategory: (id, categoryData) =>
        api.put(`/categories/${id}`, categoryData),

    deleteCategory: (id) =>
        api.delete(`/categories/${id}`),
}

// Expense service
export const expenseService = {
    // Add expense with query parameters
    addExpense: (categoryId, amount) =>
        api.post('/expenses', null, {
            params: { categoryId, amount }
        }),

    // Get expense summary
    getExpenseSummary: () =>
        api.get('/expenses/summary'),

    // Get expenses by category
    getExpensesByCategory: (categoryId) =>
        api.get(`/expenses/category/${categoryId}`),

    // Update expense
    updateExpense: (id, expenseData) =>
        api.patch(`/expenses/${id}`, expenseData),

    // Delete single expense
    deleteExpense: (id) =>
        api.delete(`/expenses/${id}`),

    // Delete expenses in time frame
    deleteExpensesInTimeFrame: (startDate, endDate) =>
        api.delete('/expenses', {
            params: { startDate, endDate }
        }),
}

// User service
export const userService = {
    // Get current user profile
    getCurrentUser: () =>
        api.get('/users/me'),

    // Get all users (admin only)
    getAllUsers: () =>
        api.get('/users'),

    // Get user by ID
    getUser: (id) =>
        api.get(`/users/${id}`),

    // Create user (registration alternative)
    createUser: (userData) =>
        api.post('/users', userData),

    // Update current user profile
    updateProfile: (profileData) =>
        api.put('/users/me', profileData),

    // Update any user by ID (admin)
    updateUser: (id, userData) =>
        api.put(`/users/${id}`, userData),

    // Change password
    changePassword: (passwordData) =>
        api.patch('/users/me/password', passwordData),

    // Delete current user account
    deleteAccount: () =>
        api.delete('/users/me'),

    // Delete user by ID (admin)
    deleteUser: (id) =>
        api.delete(`/users/${id}`),
}

export default api