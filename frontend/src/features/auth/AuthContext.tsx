import React, { createContext, useContext, useState, useEffect } from 'react';
import { User, Role } from '@/types';
import { authApi, setAuthToken, getAuthToken } from '@/services/api';

interface AuthContextType {
  user: User | null;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (data: { email: string; password: string; fullName: string; role: Role; phone?: string }) => Promise<void>;
  quickLogin: (role: Role) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

const DEMO_CREDENTIALS: Record<Role, { email: string; pass: string }> = {
  ADMIN: { email: 'admin@fieldops.demo', pass: 'FieldOps@2026!' },
  DISPATCHER: { email: 'dispatcher@fieldops.demo', pass: 'FieldOps@2026!' },
  TECHNICIAN: { email: 'technician@fieldops.demo', pass: 'FieldOps@2026!' },
  CUSTOMER: { email: 'customer@fieldops.demo', pass: 'FieldOps@2026!' },
};

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<User | null>(() => {
    const saved = localStorage.getItem('fieldops_user');
    return saved ? JSON.parse(saved) : null;
  });
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    const initAuth = async () => {
      const token = getAuthToken();
      if (token) {
        try {
          const me = await authApi.getCurrentUser();
          setUser(me);
          localStorage.setItem('fieldops_user', JSON.stringify(me));
        } catch (err) {
          console.error('Failed to restore session:', err);
          setAuthToken(null);
          localStorage.removeItem('fieldops_user');
          setUser(null);
        }
      }
      setIsLoading(false);
    };

    initAuth();
  }, []);

  const login = async (email: string, pass: string) => {
    setIsLoading(true);
    try {
      const u = await authApi.login(email, pass);
      setUser(u);
      localStorage.setItem('fieldops_user', JSON.stringify(u));
    } finally {
      setIsLoading(false);
    }
  };

  const register = async (data: { email: string; password: string; fullName: string; role: Role; phone?: string }) => {
    setIsLoading(true);
    try {
      const u = await authApi.register(data);
      setUser(u);
      localStorage.setItem('fieldops_user', JSON.stringify(u));
    } finally {
      setIsLoading(false);
    }
  };

  const quickLogin = async (role: Role) => {
    const creds = DEMO_CREDENTIALS[role];
    if (creds) {
      await login(creds.email, creds.pass);
    }
  };

  const logout = () => {
    authApi.logout();
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, isLoading, login, register, quickLogin, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
