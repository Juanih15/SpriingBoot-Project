import { useState } from "react";
import AddExpense from "./AddExpense";
import Summary from "./Summary";

export default function App() {
  const [refresh, setRefresh] = useState(0);

  return (
    <div className="p-6">
      <h1 className="text-2xl font-bold mb-4">MoneyBuddy – MVP</h1>

      {/* Form to add an expense */}
      <AddExpense onAdded={() => setRefresh(r => r + 1)} />

      {/* Aggregated summary that rerenders when refresh changes */}
      <Summary refreshTrigger={refresh} />
    </div>
  );
}
