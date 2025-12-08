import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './assets/index.css'
import App from './App.tsx'
import {BrowserRouter, Route, Routes} from "react-router";
import Login from "@/pages/auth/login.tsx";
import {ProtectedRouteProvider} from "@/features/auth/contexts/protected-route-provider.tsx";
import {QueryClientProvider, QueryClient} from "@tanstack/react-query";

export const queryClient = new QueryClient();

createRoot(document.getElementById('root')!).render(
  <StrictMode>
      <QueryClientProvider client={queryClient}>
          <BrowserRouter>
              <Routes>
                  <Route path='/' element={<ProtectedRouteProvider />}>
                      <Route path="/" element={<App/>} />
                  </Route>
                  <Route path='/login' element={<Login/>}/>
              </Routes>
          </BrowserRouter>
      </QueryClientProvider>
  </StrictMode>,
)
