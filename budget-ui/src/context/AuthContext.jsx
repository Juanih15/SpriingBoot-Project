import React, { createContext, useContext, useState, useEffect } from 'react'
import {authService, userService} from '../services/api'

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
                    // Check if it's a dev/mock token
                    if (token === 'dev-bypass-token' || token.includes('mock-signature')) {
                        // Use mock user data for dev tokens
                        const mockUser = {
                            email: 'dev@bypass.com',
                            name: 'Dev User',
                            id: 'dev-user-id'
                        };
                        setUser(mockUser);
                    } else {
                        // Validate real token with backend
                        const userData = await authService.getCurrentUser();
                        setUser(userData);
                    }
                }
            } catch (error) {
                console.error('Token validation failed:', error);
                localStorage.removeItem('token');
            } finally {
                setLoading(false);
            }
        }

        initializeAuth()
    }, [])

    const login = async (usernameOrEmail, password) => {
        console.log('=== AUTH CONTEXT LOGIN CALLED ===');
        console.log('Username/Email:', usernameOrEmail);
        console.log('Password provided:', !!password);

        try {
            console.log('Calling authService.login...');
            const response = await authService.login(usernameOrEmail, password)
            console.log('Auth service response:', response);

            // Handle the ApiResponse wrapper structure
            if (response.data.success) {
                const { token, user: userData } = response.data.data // JwtResponse is in data.data

                console.log('Login successful, storing token');
                localStorage.setItem('token', token)
                setUser(userData)

                return { success: true }
            } else {
                return {
                    success: false,
                    error: response.data.message || 'Login failed'
                }
            }
        } catch (error) {
            console.error('Login failed:', error)
            console.error('Error response:', error.response?.data)
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

    const devBypass = async () => {
        try {
            // Use one of the seeded users
            const response = await authService.login('demo', 'demo123');

            if (response.data.success) {
                const { token, user } = response.data.data;
                localStorage.setItem('token', token);
                setUser(user);
                return { success: true };
            } else {
                return { success: false, error: response.data.message };
            }
        } catch (error) {
            console.error('Dev bypass error:', error);
            return { success: false, error: 'Dev bypass failed' };
        }
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