import { BrowserRouter, Route, Routes, Navigate } from "react-router-dom";
import { Toaster } from "react-hot-toast";
import { AuthProvider } from "./context/AuthContext";

import ProtectedRoute from "./components/ProtectedRoute"
import HomePage from "./pages/HomePage";
import Login from "./pages/Login";
import Notes from "./pages/Notes";
import Register from "./pages/Register";

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Toaster
          position="top-right"
          toastOptions={{
            style: { fontFamily: 'Nunito, sans-serif', fontWeight: 700, borderRadius: '12px', border: '2px solid #1a1a2e' },
            success: { iconTheme: { primary: '#4ECDC4', secondary: 'white' } },
            error: { iconTheme: { primary: '#FF6B6B', secondary: 'white' } },
          }}
        />
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/notes" element={<ProtectedRoute><Notes /></ProtectedRoute>} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}

export default App;
