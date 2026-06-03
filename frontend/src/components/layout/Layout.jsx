import Sidebar from './Sidebar'
import BottomNav from './BottomNav'

export default function Layout({ children }) {
  return (
    <div className="flex min-h-screen bg-surface">
      <Sidebar />

      <main className="flex-1 flex flex-col min-w-0">
        <div className="flex-1 p-4 md:p-8 pb-24 md:pb-8 max-w-2xl mx-auto w-full">
          {children}
        </div>
      </main>

      <BottomNav />
    </div>
  )
}
