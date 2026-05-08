import type { Metadata } from 'next'
import { Toaster } from 'sonner'
import { QueryProvider } from '@/providers/QueryProvider'
import './globals.css'

export const metadata: Metadata = {
  title: 'Tentuin Admin',
  description: 'Dashboard admin untuk sistem agen Tentuin',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="id">
      <body>
        <QueryProvider>
          {children}
          <Toaster richColors position="top-right" />
        </QueryProvider>
      </body>
    </html>
  )
}
