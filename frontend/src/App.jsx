import { Navigate, Route, Routes } from 'react-router-dom'
import Layout from './components/Layout'
import { PublicOnly, RequireAuth, RequireRole } from './components/RouteGuards'

import Login from './pages/auth/Login'
import Register from './pages/auth/Register'
import VerifyOtp from './pages/auth/VerifyOtp'
import ForgotPassword from './pages/auth/ForgotPassword'

import Dashboard from './pages/Dashboard'
import Projects from './pages/shared/Projects'
import ProjectDetail from './pages/shared/ProjectDetail'
import AssignmentDetail from './pages/shared/AssignmentDetail'
import Requirements from './pages/shared/Requirements'
import Profile from './pages/shared/Profile'
import { Contractors, Labourers } from './pages/shared/Directory'
import NewProject from './pages/owner/NewProject'
import NewRequirement from './pages/owner/NewRequirement'
import Work from './pages/labour/Work'
import MyLabour from './pages/contractor/MyLabour'
import AdminUsers from './pages/admin/AdminUsers'
import EngineerDirectory from './pages/engineer/EngineerDirectory'
import EngineerPublicProfile from './pages/engineer/EngineerPublicProfile'
import EngineerProfile from './pages/engineer/EngineerProfile'
import Invitations from './pages/engineer/Invitations'
import InviteEngineer from './pages/owner/InviteEngineer'

export default function App() {
  return (
    <Routes>
      <Route element={<PublicOnly />}>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/verify" element={<VerifyOtp />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
      </Route>

      <Route element={<RequireAuth />}>
        <Route element={<Layout />}>
          <Route path="/" element={<Dashboard />} />
          <Route path="/profile" element={<Profile />} />
          <Route path="/projects" element={<Projects />} />
          <Route path="/projects/:id" element={<ProjectDetail />} />
          <Route path="/assignments/:id" element={<AssignmentDetail />} />
          <Route path="/engineers/:id" element={<EngineerPublicProfile />} />

          <Route element={<RequireRole roles={['HOUSE_OWNER', 'CONTRACTOR']} />}>
            <Route path="/projects/new" element={<NewProject />} />
            <Route path="/projects/:id/invite-engineer" element={<InviteEngineer />} />
          </Route>

          <Route element={<RequireRole roles={['HOUSE_OWNER', 'CONTRACTOR', 'ENGINEER']} />}>
            <Route path="/invitations" element={<Invitations />} />
          </Route>

          <Route element={<RequireRole roles={['HOUSE_OWNER', 'CONTRACTOR', 'ADMIN']} />}>
            <Route path="/engineers" element={<EngineerDirectory />} />
          </Route>

          <Route element={<RequireRole roles={['ENGINEER']} />}>
            <Route path="/engineer-profile" element={<EngineerProfile />} />
          </Route>

          <Route element={<RequireRole roles={['HOUSE_OWNER']} />}>
            <Route path="/requirements/new" element={<NewRequirement />} />
            <Route path="/contractors" element={<Contractors />} />
          </Route>

          <Route element={<RequireRole roles={['HOUSE_OWNER', 'CONTRACTOR', 'ADMIN']} />}>
            <Route path="/requirements" element={<Requirements />} />
          </Route>

          <Route element={<RequireRole roles={['CONTRACTOR']} />}>
            <Route path="/my-labour" element={<MyLabour />} />
          </Route>

          <Route element={<RequireRole roles={['CONTRACTOR', 'ADMIN']} />}>
            <Route path="/labourers" element={<Labourers />} />
          </Route>

          <Route element={<RequireRole roles={['LABOUR']} />}>
            <Route path="/work" element={<Work />} />
          </Route>

          <Route element={<RequireRole roles={['ADMIN']} />}>
            <Route path="/users" element={<AdminUsers />} />
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}
