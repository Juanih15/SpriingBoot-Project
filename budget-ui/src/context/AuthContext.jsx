import React, { createContext, useContext, useState, useEffect } from 'react'
import { authService } from '../services/api'

const AuthContext = createContext()

export const useAuth = () => {
    const context = useContext(AuthContext)
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider')
    }
    return context
}

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null)
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        const initializeAuth = async () => {
            try {
                const token = localStorage.getItem('token')
                if (token) {
                    // Validate token with backend
                    const userData = await authService.getCurrentUser()
                    setUser(userData)
                }
            } catch (error) {
                console.error('Token validation failed:', error)
                localStorage.removeItem('token')
            } finally {
                setLoading(false)
            }
        }

        initializeAuth()
    }, [])

    const login = async (email, password) => {
        try {
            const response = await authService.login(email, password)
            const { token, user: userData } = response.data

            localStorage.setItem('token', token)
            setUser(userData)

            return { success: true }
        } catch (error) {
            console.error('Login failed:', error)
            return {
                success: false,
                error: error.response?.data?.message || 'Login failed. Please try again.'
            }
        }
    }

    const logout = () => {
        localStorage.removeItem('token')
        setUser(null)
    }

    const register = async (userData) => {
        try {
            const response = await authService.register(userData)
            const { token, user: newUser } = response.data

            localStorage.setItem('token', token)
            setUser(newUser)

            return { success: true }
        } catch (error) {
            console.error('Registration failed:', error)
            return {
                success: false,
                error: error.response?.data?.message || 'Registration failed. Please try again.'
            }
        }
    }

    const updateUser = (userData) => {
        setUser(prevUser => ({ ...prevUser, ...userData }))
    }

    const devBypass = () => {
        const dummyUser = {
            email: 'dev@bypass.com',
            name: 'Dev User',
            // add any other user fields your app expects
        };

        localStorage.setItem('token', 'dev-bypass-token');
        setUser(dummyUser);
    };

    const value = {
        user,
        loading,
        login,
        logout,
        register,
        updateUser,
        devBypass
    }

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    )
}