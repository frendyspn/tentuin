import Link from 'next/link'

export default function NotFound() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-4 bg-gray-50">
      <h1 className="text-4xl font-bold text-gray-900">404</h1>
      <p className="text-gray-600">Halaman tidak ditemukan.</p>
      <Link href="/agen" className="text-blue-600 underline hover:text-blue-800">
        Kembali ke Dashboard
      </Link>
    </div>
  )
}
