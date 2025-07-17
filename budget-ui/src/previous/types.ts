export type Category =
  | "GROCERIES" | "GAS" | "ENTERTAINMENT"
  | "ATHLETICS" | "RECREATION" | "SUBSCRIPTIONS";

export interface Totals { [key: string]: number; }
