// Optimized Dashboard Component with proper data fetching
import React, { useState, useEffect, useCallback, useRef } from 'react'
import { useAuth } from '../context/AuthContext.jsx'
import {budgetService, expenseService} from '../services/api'

const Dashboard = () => {
    const { user } = useAuth()
    const [dashboardData, setDashboardData] = useState({
        totalExpenses: 0,
        monthlyBudget: 0,
        budgetUsed: 0,
        budgetRemaining: 0,
        recentTransactions: [],
    });
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    const [showAddModal, setShowAddModal] = useState(false)

    const [transactionForm, setTransactionForm] = useState({
        description: '',
        amount: '',
        expenseDate: '',
        memo: '',
        categoryId: ''
    });

    const [isSubmitting, setIsSubmitting] = useState(false)
    const [formError, setFormError] = useState('')


    // Use refs to prevent duplicate requests
    const isLoadingRef = useRef(false)
    const lastFetchTime = useRef(0)
    const CACHE_DURATION = 30000 // 30 seconds cache

    const fetchDashboardData = useCallback(async (forceRefresh = false) => {
        // Prevent duplicate requests
        if (isLoadingRef.current) {
            return
        }

        // Check cache duration
        const now = Date.now()
        if (!forceRefresh && (now - lastFetchTime.current) < CACHE_DURATION) {
            return
        }

        try {
            setError('')
            isLoadingRef.current = true

            // Only show loading on initial load or force refresh
            if (forceRefresh || dashboardData.totalExpenses === 0) {
                setLoading(true)
            }

            const response = await budgetService.getDashboardData()

            if (response.data?.success) {
                setDashboardData(response.data.data)
                lastFetchTime.current = now
            } else {
                throw new Error(response.data?.error || 'Failed to load dashboard data')
            }
        } catch (err) {
            console.error('Dashboard fetch error:', err)
            setError('Failed to load dashboard data')
        } finally {
            setLoading(false)
            isLoadingRef.current = false
        }
    }, [dashboardData.totalExpenses])

    // Initial load only
    useEffect(() => {
        fetchDashboardData(true)
    }, []) // Remove fetchDashboardData from dependency array

    // Manual refresh function
    const handleRefresh = useCallback(() => {
        fetchDashboardData(true)
    }, [fetchDashboardData])
    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setTransactionForm((prev) => ({ ...prev, [name]: value }));
    };
    const handleAddTransaction = async (e) => {
        e.preventDefault();
        setIsSubmitting(true);
        setFormError('');

        try {
            const payload = {
                ...transactionForm,
                amount: parseFloat(transactionForm.amount),
                categoryId: parseInt(transactionForm.categoryId)
            };
            await expenseService.addExpense(payload);
            setShowAddModal(false);
            setTransactionForm({ description: '', amount: '', expenseDate: '', memo: '', categoryId: '' });
            fetchDashboardData(true);
        } catch (err) {
            console.error('Add expense error:', err);
            setFormError('Failed to add transaction. Please check your input.');
        } finally {
            setIsSubmitting(false);
        }
    };

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
        }).format(amount)
    }

    const budgetPercentage = dashboardData.monthlyBudget > 0
        ? (dashboardData.budgetUsed / dashboardData.monthlyBudget) * 100
        : 0

    if (loading && dashboardData.totalExpenses === 0) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
            </div>
        )
    }

    return (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            {/* Welcome, Section with Refresh Button */}
            <div className="mb-8 flex justify-between items-center">
                <div>
                    <h1 className="text-3xl font-bold text-gray-900">
                        Welcome back, {user?.name || user?.email}!
                    </h1>
                    <p className="text-gray-600 mt-2">
                        Here's your financial overview for {new Date().toLocaleDateString('en-US', { month: 'long', year: 'numeric' })}
                    </p>
                </div>
                <button
                    onClick={handleRefresh}
                    disabled={isLoadingRef.current}
                    className="btn-secondary flex items-center space-x-2"
                >
                    <svg
                        className={`w-4 h-4 ${isLoadingRef.current ? 'animate-spin' : ''}`}
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                    >
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                    </svg>
                    <span>Refresh</span>
                </button>
            </div>

            {error && (
                <div className="mb-6 rounded-md bg-red-50 p-4">
                    <div className="flex">
                        <div className="flex-shrink-0">
                            <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                            </svg>
                        </div>
                        <div className="ml-3 flex justify-between items-center w-full">
                            <p className="text-sm text-red-800">{error}</p>
                            <button
                                onClick={handleRefresh}
                                className="text-red-600 hover:text-red-800 underline text-sm"
                            >
                                Try Again
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Stats Cards */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                <div className="bg-white rounded-lg shadow-md p-6">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-sm font-medium text-gray-600">Monthly Budget</p>
                            <p className="text-2xl font-bold text-blue-600">
                                {formatCurrency(dashboardData.monthlyBudget)}
                            </p>
                        </div>
                        <div className="bg-blue-100 p-3 rounded-full">
                            <svg className="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 7h6m0 10v-3m-3 3h.01M9 17h.01M9 14h.01M12 14h.01M15 11h.01M12 11h.01M9 11h.01M7 21h10a2 2 0 002-2V5a2 2 0 00-2-2H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
                            </svg>
                        </div>
                    </div>
                </div>

                <div className="bg-white rounded-lg shadow-md p-6">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-sm font-medium text-gray-600">Total Expenses</p>
                            <p className="text-2xl font-bold text-red-600">
                                {formatCurrency(dashboardData.totalExpenses)}
                            </p>
                        </div>
                        <div className="bg-red-100 p-3 rounded-full">
                            <svg className="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 13l-5 5m0 0l-5-5m5 5V6" />
                            </svg>
                        </div>
                    </div>
                </div>

                <div className="bg-white rounded-lg shadow-md p-6">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-sm font-medium text-gray-600">Budget Remaining</p>
                            <p className={`text-2xl font-bold ${dashboardData.budgetRemaining >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                                {formatCurrency(dashboardData.budgetRemaining)}
                            </p>
                        </div>
                        <div className={`p-3 rounded-full ${dashboardData.budgetRemaining >= 0 ? 'bg-green-100' : 'bg-red-100'}`}>
                            <svg className={`w-6 h-6 ${dashboardData.budgetRemaining >= 0 ? 'text-green-600' : 'text-red-600'}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
                            </svg>
                        </div>
                    </div>
                </div>
            </div>

            {/* Budget Progress */}
            <div className="bg-white rounded-lg shadow-md p-6 mb-8">
                <h2 className="text-lg font-semibold text-gray-900 mb-4">Monthly Budget Progress</h2>
                <div className="flex items-center justify-between mb-2">
                    <span className="text-sm text-gray-600">
                        {formatCurrency(dashboardData.budgetUsed)} of {formatCurrency(dashboardData.monthlyBudget)}
                    </span>
                    <span className="text-sm text-gray-600">
                        {budgetPercentage.toFixed(1)}%
                    </span>
                </div>
                <div className="w-full bg-gray-200 rounded-full h-3">
                    <div
                        className={`h-3 rounded-full transition-all duration-300 ${
                            budgetPercentage > 90 ? 'bg-red-500' : budgetPercentage > 70 ? 'bg-yellow-500' : 'bg-green-500'
                        }`}
                        style={{ width: `${Math.min(budgetPercentage, 100)}%` }}
                    ></div>
                </div>
                <p className="text-sm text-gray-600 mt-2">
                    {budgetPercentage > 100
                        ? `You're ${formatCurrency(dashboardData.budgetUsed - dashboardData.monthlyBudget)} over budget this month!`
                        : `You have ${formatCurrency(dashboardData.budgetRemaining)} left to spend this month.`
                    }
                </p>
            </div>

            {/* Recent Transactions */}
            <div className="bg-white rounded-lg shadow-md p-6">
                <div className="flex items-center justify-between mb-4">
                    <h2 className="text-lg font-semibold text-gray-900">Recent Transactions</h2>
                    <button className="text-primary-600 hover:text-primary-700 text-sm font-medium">
                        View All
                    </button>
                </div>

                {dashboardData.recentTransactions.length === 0 ? (
                    <div className="text-center py-8">
                        <svg className="mx-auto h-12 w-12 text-gray-400" stroke="currentColor" fill="none" viewBox="0 0 48 48">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M34 40h10v-4a6 6 0 00-10.712-3.714M34 40H14m20 0v-4a9.971 9.971 0 00-.712-3.714M14 40H4v-4a6 6 0 0110.713-3.714M14 40v-4c0-1.313.253-2.566.713-3.714m0 0A10.003 10.003 0 0124 26c4.21 0 7.814 2.602 9.288 6.286" />
                        </svg>
                        <p className="mt-2 text-sm text-gray-500">No transactions yet</p>
                        <p className="text-sm text-gray-500">Add your first transaction to get started!</p>
                    </div>
                ) : (
                    <div className="space-y-4">
                        {dashboardData.recentTransactions.map((transaction) => (
                            <div key={transaction.id} className="flex items-center justify-between py-3 border-b border-gray-100 last:border-b-0">
                                <div className="flex items-center space-x-3">
                                    <div className={`p-2 rounded-full ${transaction.amount > 0 ? 'bg-green-100' : 'bg-red-100'}`}>
                                        <svg className={`w-4 h-4 ${transaction.amount > 0 ? 'text-green-600' : 'text-red-600'}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d={transaction.amount > 0 ? "M7 11l5-5m0 0l5 5m-5-5v12" : "M17 13l-5 5m0 0l-5-5m5 5V6"} />
                                        </svg>
                                    </div>
                                    <div>
                                        <p className="text-sm font-medium text-gray-900">{transaction.description}</p>
                                        <p className="text-sm text-gray-500">{transaction.category} • {new Date(transaction.date).toLocaleDateString()}</p>
                                    </div>
                                </div>
                                <p className={`text-sm font-medium ${transaction.amount > 0 ? 'text-green-600' : 'text-red-600'}`}>
                                    {transaction.amount > 0 ? '+' : ''}{formatCurrency(transaction.amount)}
                                </p>
                            </div>
                        ))}
                    </div>
                )}
            </div>

            {/* Quick Actions */}
            <div className="mt-8 flex flex-wrap gap-4 justify-center">
                <button className="btn-primary" onClick={() => setShowAddModal(true)}>
                    Add Transaction
                </button>
                <button className="btn-secondary">View Reports</button>
                <button className="btn-secondary">Manage Categories</button>
            </div>

            {/* Add Transaction Modal */}
            {showAddModal && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50">
                    <div className="bg-white p-6 rounded-lg shadow-md w-full max-w-md">
                        <h2 className="text-lg font-semibold mb-4">Add New Transaction</h2>
                        <form onSubmit={handleAddTransaction}>
                            <input type="text" name="description" placeholder="Description" value={transactionForm.description} onChange={handleInputChange} className="input input-bordered w-full mb-3" required />
                            <input type="number" name="amount" placeholder="Amount" value={transactionForm.amount} onChange={handleInputChange} className="input input-bordered w-full mb-3" min="0.01" step="0.01" required />
                            <input type="date" name="expenseDate" value={transactionForm.expenseDate} onChange={handleInputChange} className="input input-bordered w-full mb-3" required />
                            <input type="text" name="memo" placeholder="Memo (optional)" value={transactionForm.memo} onChange={handleInputChange} className="input input-bordered w-full mb-3" />
                            <input type="number" name="categoryId" placeholder="Category ID" value={transactionForm.categoryId} onChange={handleInputChange} className="input input-bordered w-full mb-3" required />
                            {formError && <p className="text-red-600 text-sm mb-2">{formError}</p>}
                            <div className="flex justify-end space-x-2">
                                <button type="button" onClick={() => setShowAddModal(false)} className="btn btn-secondary">Cancel</button>
                                <button type="submit" className="btn btn-primary" disabled={isSubmitting}>
                                    {isSubmitting ? 'Saving...' : 'Add'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>


    )
}

export default Dashboard