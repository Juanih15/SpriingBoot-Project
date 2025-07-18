import React, { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext.jsx'
import { budgetService } from '../services/api'

const Dashboard = () => {
    const { user } = useAuth()
    const [dashboardData, setDashboardData] = useState({
        totalIncome: 0,
        totalExpenses: 0,
        balance: 0,
        recentTransactions: [],
        monthlyBudget: 0,
        budgetUsed: 0,
    })
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')

    useEffect(() => {
        fetchDashboardData()
    }, [])

    const fetchDashboardData = async () => {
        try {
            setLoading(true)
            // Uncomment when backend is ready
            // const response = await budgetService.getDashboardData()
            // setDashboardData(response.data)

            // Mock data for now
            setTimeout(() => {
                setDashboardData({
                    totalIncome: 5000,
                    totalExpenses: 3200,
                    balance: 1800,
                    recentTransactions: [
                        { id: 1, description: 'Grocery Shopping', amount: -120, category: 'Food', date: '2024-01-15' },
                        { id: 2, description: 'Salary', amount: 5000, category: 'Income', date: '2024-01-01' },
                        { id: 3, description: 'Utilities', amount: -200, category: 'Bills', date: '2024-01-10' },
                    ],
                    monthlyBudget: 4000,
                    budgetUsed: 3200,
                })
                setLoading(false)
            }, 1000)
        } catch (err) {
            setError('Failed to load dashboard data')
            setLoading(false)
        }
    }

    const formatCurrency = (amount) => {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD',
        }).format(amount)
    }

    const budgetPercentage = dashboardData.monthlyBudget > 0
        ? (dashboardData.budgetUsed / dashboardData.monthlyBudget) * 100
        : 0

    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-600"></div>
            </div>
        )
    }

    return (
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            {/* Welcome Section */}
            <div className="mb-8">
                <h1 className="text-3xl font-bold text-gray-900">
                    Welcome back, {user?.name || user?.email}!
                </h1>
                <p className="text-gray-600 mt-2">
                    Here's your financial overview for {new Date().toLocaleDateString('en-US', { month: 'long', year: 'numeric' })}
                </p>
            </div>

            {error && (
                <div className="mb-6 rounded-md bg-red-50 p-4">
                    <div className="flex">
                        <div className="flex-shrink-0">
                            <svg className="h-5 w-5 text-red-400" viewBox="0 0 20 20" fill="currentColor">
                                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                            </svg>
                        </div>
                        <div className="ml-3">
                            <p className="text-sm text-red-800">{error}</p>
                        </div>
                    </div>
                </div>
            )}

            {/* Stats Cards */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
                <div className="bg-white rounded-lg shadow-md p-6">
                    <div className="flex items-center justify-between">
                        <div>
                            <p className="text-sm font-medium text-gray-600">Total Income</p>
                            <p className="text-2xl font-bold text-green-600">
                                {formatCurrency(dashboardData.totalIncome)}
                            </p>
                        </div>
                        <div className="bg-green-100 p-3 rounded-full">
                            <svg className="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 11l5-5m0 0l5 5m-5-5v12" />
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
                            <p className="text-sm font-medium text-gray-600">Balance</p>
                            <p className={`text-2xl font-bold ${dashboardData.balance >= 0 ? 'text-green-600' : 'text-red-600'}`}>
                                {formatCurrency(dashboardData.balance)}
                            </p>
                        </div>
                        <div className={`p-3 rounded-full ${dashboardData.balance >= 0 ? 'bg-green-100' : 'bg-red-100'}`}>
                            <svg className={`w-6 h-6 ${dashboardData.balance >= 0 ? 'text-green-600' : 'text-red-600'}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
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
                        : `You have ${formatCurrency(dashboardData.monthlyBudget - dashboardData.budgetUsed)} left to spend this month.`
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
                <button className="btn-primary">
                    Add Transaction
                </button>
                <button className="btn-secondary">
                    View Reports
                </button>
                <button className="btn-secondary">
                    Manage Categories
                </button>
            </div>
        </div>
    )
}

export default Dashboard