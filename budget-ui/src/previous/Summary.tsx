import { useEffect, useState } from "react";
import type { Totals } from "./types.ts";

export default function Summary({ refreshTrigger }: { refreshTrigger: number }) {
  const [data, setData] = useState<Totals>({});

  useEffect(() => {
    fetch("/api/summary").then(r => r.json()).then(setData);
  }, [refreshTrigger]);

  return (
    <table>
      <tbody>
        {Object.entries(data).map(([cat, total]) => (
          <tr key={cat}>
            <td>{cat}</td>
            <td>${total.toFixed(2)}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
