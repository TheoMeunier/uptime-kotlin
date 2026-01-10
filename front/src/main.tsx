import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./assets/index.css";
import App from "./App.tsx";
import { BrowserRouter, Navigate, Route, Routes } from "react-router";
import Login from "@/pages/auth/login.tsx";
import { ProtectedRouteProvider } from "@/features/auth/contexts/protected-route-provider.tsx";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import Layout from "@/components/layouts/layout.tsx";
import CreateProbe from "@/pages/probes/create-probe.tsx";
import { ShowProbe } from "@/pages/probes/show-probe.tsx";
import Profile from "@/pages/profile/profile.tsx";
import ProbesStatus from "@/pages/probes/probes-status.tsx";
import "./lang/i18n.ts";

export const queryClient = new QueryClient();

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<ProtectedRouteProvider />}>
            <Route path="/" element={<Layout />}>
              <Route path="/dashboard" element={<App />} />

              <Route path="monitors/new" element={<CreateProbe />} />
              <Route path="monitors/:probeId" element={<ShowProbe />} />

              <Route path="profile" element={<Profile />} />
            </Route>
          </Route>

          <Route path="/status" element={<ProbesStatus />} />
          <Route path="/login" element={<Login />} />

          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  </StrictMode>,
);
