import { useState, type FormEvent } from "react";
import type { Category } from "./types.ts";

export default function AddExpense({ onAdded }: { onAdded: () => void }) {
  const [category, setCategory] = useState<Category>("GROCERIES");
  const [amount, setAmount] = useState("");

  async function submit(e: FormEvent) {
    e.preventDefault();
    await fetch(`/api/expenses?category=${category}&amount=${encodeURIComponent(amount)}`,
                { method: "POST" });
    onAdded(); setAmount("");
  }

  return (
    <form onSubmit={submit} className="space-x-2 mb-4">
      <select value={category}
              onChange={e => setCategory(e.target.value as Category)}>
        {["GROCERIES","GAS","ENTERTAINMENT","ATHLETICS","RECREATION","SUBSCRIPTIONS"]
          .map(c => <option key={c}>{c}</option>)}
      </select>

      <input type="number" step="0.01" required
             value={amount}
             onChange={e => setAmount(e.target.value)} />

      <button>Add</button>
    </form>
  );
}
