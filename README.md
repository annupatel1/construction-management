# Construction Site Management System

A full-stack Spring Boot backend for a construction marketplace connecting House Owners, Contractors, and Labourers — built with role-based access control, JWT authentication, and a proper layered architecture.

## Overview

This platform models a real-world construction workflow:
- **House Owners** post project requirements or hire contractors directly, and track payments made.
- **Contractors** browse/take up projects, assign labourers, mark daily attendance, log material expenses, and pay wages.
- **Labourers** get assigned to projects with a daily rate, tracked attendance, and advance/wage payments.

The system supports two contract types — **Labour Only** (owner supplies material) and **Labour + Material** (contractor supplies material) — which affects how project costs and profit are calculated.

## Tech Stack

- **Java 21**, **Spring Boot 4.1.1**
- **Spring Security** with **JWT** authentication (stateless, BCrypt password hashing)
- **Spring Data JPA** + **Hibernate** (MySQL)
- **Role-based access control** via `@PreAuthorize`
- **Maven** for build management
- **Lombok** for boilerplate reduction

## Architecture

The project follows a standard layered architecture: