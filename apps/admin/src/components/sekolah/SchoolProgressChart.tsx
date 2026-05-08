'use client'

import {
  ComposedChart,
  Bar,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts'

interface MonthlyData {
  month: string
  actual: number
  target: number
}

interface SchoolProgressChartProps {
  data: MonthlyData[]
}

export function SchoolProgressChart({ data }: SchoolProgressChartProps) {
  return (
    <ResponsiveContainer width="100%" height={260}>
      <ComposedChart data={data} margin={{ top: 4, right: 8, left: 0, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="#F0F0F5" />
        <XAxis dataKey="month" tick={{ fontSize: 12, fill: '#6B7280' }} />
        <YAxis tick={{ fontSize: 12, fill: '#6B7280' }} />
        <Tooltip
          contentStyle={{ borderRadius: 8, border: '1px solid #E5E7EB', fontSize: 12 }}
        />
        <Legend wrapperStyle={{ fontSize: 12 }} />
        <Bar dataKey="actual" name="Aktual Daftar" fill="#5C59F8" radius={[4, 4, 0, 0]} />
        <Line
          type="monotone"
          dataKey="target"
          name="Target Kumulatif"
          stroke="#FF6B6B"
          strokeWidth={2}
          dot={false}
        />
      </ComposedChart>
    </ResponsiveContainer>
  )
}
