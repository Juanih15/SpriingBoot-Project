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
                console.log('AuthContext initializing...');
                console.log('  - Token exists:', !!token);
                console.log('  - Token preview:', token ? token.substring(0, 20) + '...' : 'none');

                if (token) {
                    console.log('Validating token with backend...');

                    try {
                        // Use authService for consistency
                        const response = await authService.getCurrentUser();
                        console.log('Token validation response:', response);

                        if (response.data.success && response.data.data) {
                            const userData = {
                                username: response.data.data.username,
                                email: response.data.data.email,
                                name: response.data.data.username,
                                roles: response.data.data.roles || ['ROLE_USER']
                            };
                            setUser(userData);
                            console.log('Token validation successful, user set:', userData);
                        } else {
                            throw new Error('Invalid response format: ' + JSON.stringify(response.data));
                        }

                    } catch (validationError) {
                        console.error('Token validation failed:', validationError);

                        // In development, fall back to mock user for better dev experience
                        if (process.env.NODE_ENV === 'development') {
                            console.log('DEV MODE: Falling back to mock user...');
                            const mockUser = {
                                username: 'demo',
                                email: 'demo@moneymapper.com',
                                name: 'Demo User',
                                roles: ['ROLE_USER']
                            };
                            setUser(mockUser);
                            console.log('Using mock user:', mockUser);
                        } else {
                            // In production, properly log out
                            console.log('PROD MODE: Logging out due to invalid token');
                            localStorage.removeItem('token');
                            setUser(null);
                        }
                    }
                } else {
                    console.log('No token found, user will remain null');
                }
            } catch (error) {
                console.error('AuthContext initialization error:', error);
                localStorage.removeItem('token');
                setUser(null);
            } finally {
                setLoading(false);
                console.log('AuthContext initialization complete');
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

            if (response.data.success) {
                const { token, username, email, roles } = response.data.data;

                const userData = {
                    username: username,
                    email: email,
                    roles: roles,
                    name: username
                };

                console.log('Login successful');
                console.log('Token received:', token.substring(0, 20) + '...');
                console.log('User data:', userData);

                localStorage.setItem('token', token);
                setUser(userData);

                return { success: true };
            } else {
                console.log('Login failed:', response.data.message);
                return {
                    success: false,
                    error: response.data.message || 'Login failed'
                };
            }
        } catch (error) {
            console.error('Login failed:', error);
            console.error('Error response:', error.response?.data);
            return {
                success: false,
                error: error.response?.data?.message || 'Login failed. Please try again.'
            };
        }
    }

    const logout = async () => {
        console.log('Logging out...');

        try {
            const token = localStorage.getItem('token');

            if (token) {
                console.log('Calling logout API...');
                await authService.logout();
                console.log('Logout API call successful');
            }
        } catch (error) {
            console.error('Logout API call failed:', error);
            // Continue with local logout even if API fails
        } finally {
            // Always clear local state
            console.log('Clearing local storage and user state');
            localStorage.removeItem('token');
            setUser(null);
            console.log('Logout complete');
        }
    }

    const register = async (userData) => {
        console.log('Registering user:', userData.username);

        try {
            const response = await authService.register(userData)
            console.log('Registration response:', response);

            if (response.data.success) {
                console.log('Registration successful');
                return {
                    success: true,
                    message: response.data.message // Important for email verification messages
                };
            } else {
                console.log('Registration failed:', response.data.message);
                return {
                    success: false,
                    error: response.data.message || 'Registration failed'
                };
            }
        } catch (error) {
            console.error('Registration failed:', error)
            return {
                success: false,
                error: error.response?.data?.message || 'Registration failed. Please try again.'
            }
        }
    }

    const updateUser = (userData) => {
        console.log('Updating user data:', userData);
        setUser(prevUser => ({ ...prevUser, ...userData }))
    }

    const devBypass = async () => {
        console.log('DEV BYPASS - Using authService login');

        try {
            const response = await authService.login('demo', 'demo123');
            console.log('DevBypass response:', response);

            if (response.data.success) {
                const { token, username, email, roles } = response.data.data;

                const userData = {
                    username: username,
                    email: email,
                    roles: roles,
                    name: username
                };

                console.log('DevBypass successful');
                console.log('Token received:', token.substring(0, 30) + '...');

                localStorage.setItem('token', token);
                setUser(userData);

                // Verify everything is saved
                const savedToken = localStorage.getItem('token');
                console.log('Verification - Token in storage:', !!savedToken);

                return { success: true };
            } else {
                console.log('DevBypass failed:', response.data.message);
                return { success: false, error: response.data.message };
            }
        } catch (error) {
            console.error('DevBypass failed:', error);
            return { success: false, error: error.message };
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