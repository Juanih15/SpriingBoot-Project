// src/pages/LoginPage.jsx
import React, { useState } from 'react'
import { useForm } from 'react-hook-form'
import { yupResolver } from '@hookform/resolvers/yup'
import * as yup from 'yup'
import { useAuth } from '../context/AuthContext.jsx'
import { useNavigate } from 'react-router-dom'

const loginSchema = yup.object({
    usernameOrEmail: yup
        .string()
        .required('Username or email is required')
        .test('username-or-email', 'Please enter a valid username or email', function(value) {
            if (!value) return false;
            // Check if it's an email
            const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
            // Check if it's a valid username (alphanumeric, underscore, dash, min 3 chars)
            const usernameRegex = /^[a-zA-Z0-9_-]{3,}$/;
            return emailRegex.test(value) || usernameRegex.test(value);
        }),
    password: yup
        .string()
        .min(6, 'Password must be at least 6 characters')
        .required('Password is required'),
})

const LoginPage = () => {
    const { login, devBypass } = useAuth()
    const navigate = useNavigate()
    const [isLoading, setIsLoading] = useState(false)
    const [loginError, setLoginError] = useState('')

    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm({
        resolver: yupResolver(loginSchema),
    })

    const onSubmit = async (data) => {
        setIsLoading(true)
        setLoginError('')

        try {
            console.log('Form data:', data); // Debug log
            console.log('Calling login with:', data.usernameOrEmail, 'and password length:', data.password?.length);

            const result = await login(data.usernameOrEmail, data.password)

            if (result.success) {
                navigate('/')
            } else {
                setLoginError(result.error)
            }
        } catch (error) {
            console.error('Login error:', error); // Debug log
            setLoginError('An unexpected error occurred. Please try again.')
        } finally {
            setIsLoading(false)
        }
    }

    const handleDevBypass = async () => {
        setIsLoading(true)
        try {
            const result = await devBypass()
            if (result.success) {
                navigate('/')
            } else {
                setLoginError('Dev bypass failed')
            }
        } catch (error) {
            setLoginError('Dev bypass failed')
        } finally {
            setIsLoading(false)
        }
    }

    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
            <div className="max-w-md w-full space-y-8">
                <div>
                    <div className="mx-auto h-12 w-12 bg-primary-600 rounded-full flex items-center justify-center">
                        <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
                        </svg>
                    </div>
                    <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
                        Sign in to your account
                    </h2>
                    <p className="mt-2 text-center text-sm text-gray-600">
                        Welcome back to Budget Tracker
                    </p>
                </div>

                <form className="mt-8 space-y-6" onSubmit={handleSubmit(onSubmit)}>
                    <div className="rounded-md shadow-sm space-y-4">
                        <div>
                            <label htmlFor="usernameOrEmail" className="block text-sm font-medium text-gray-700">
                                Username or Email
                            </label>
                            <input
                                id="usernameOrEmail"
                                type="text"
                                autoComplete="username email"
                                className={`mt-1 input-field ${errors.usernameOrEmail ? 'border-red-500' : ''}`}
                                placeholder="Enter your username or email"
                                {...register('usernameOrEmail')}
                            />
                            {errors.usernameOrEmail && (
                                <p className="form-error">{errors.usernameOrEmail.message}</p>
                            )}
                        </div>

                        <div>
                            <label htmlFor="password" className="block text-sm font-medium text-gray-700">
                                Password
                            </label>
                            <input
                                id="password"
                                type="password"
                                autoComplete="current-password"
                                className={`mt-1 input-field ${errors.password ? 'border-red-500' : ''}`}
                                placeholder="Enter your password"
                                {...register('password')}
                            />
                            {errors.password && (
                                <p className="form-error">{errors.password.message}</p>
                            )}
                        </div>
                    </div>

                    {loginError && (
                        <div className="rounded-md bg-red-50 p-4">
                            <div className="flex">
                                <div className="flex-shrink-0">
                                    <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                                        <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                                    </svg>
                                </div>
                                <div className="ml-3">
                                    <p className="text-sm text-red-800">{loginError}</p>
                                </div>
                            </div>
                        </div>
                    )}

                    <div className="flex items-center justify-between">
                        <div className="flex items-center">
                            <input
                                id="remember-me"
                                name="remember-me"
                                type="checkbox"
                                className="h-4 w-4 text-primary-600 focus:ring-primary-500 border-gray-300 rounded"
                            />
                            <label htmlFor="remember-me" className="ml-2 block text-sm text-gray-900">
                                Remember me
                            </label>
                        </div>

                        <div className="text-sm">
                            <a href="#" className="font-medium text-primary-600 hover:text-primary-500">
                                Forgot your password?
                            </a>
                        </div>
                    </div>

                    <div>
                        <button
                            type="submit"
                            disabled={isLoading}
                            className="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                        >
                            {isLoading ? (
                                <div className="flex items-center">
                                    <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                    </svg>
                                    Signing in...
                                </div>
                            ) : (
                                'Sign in'
                            )}
                        </button>
                    </div>

                    <div className="text-center">
                        <p className="text-sm text-gray-600">
                            Don't have an account?{' '}
                            <a href="#" className="font-medium text-primary-600 hover:text-primary-500">
                                Sign up here
                            </a>
                        </p>
                    </div>
                </form>

                {/* Dev bypass button and test credentials info */}
                <div className="mt-6 space-y-4">
                    <button
                        type="button"
                        onClick={handleDevBypass}
                        disabled={isLoading}
                        className="w-full text-center text-blue-600 hover:text-blue-800 hover:underline block text-sm font-medium disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                        Skip login (dev only - uses demo account)
                    </button>

                    {/* Test credentials info for development */}
                    <div className="text-center text-xs text-gray-500 space-y-1">
                        <p className="font-semibold">Test Accounts:</p>
                        <p>Admin: admin / admin123</p>
                        <p>Demo: demo / demo123</p>
                        <p>Test: testuser / password123</p>
                    </div>
                </div>
            </div>
        </div>
    )
}

export default LoginPage