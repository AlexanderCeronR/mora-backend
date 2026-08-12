# MORA — Manager Of Restaurant Administration

Sistema de gestión para restaurantes desarrollado con Spring Boot y React.

## Tecnologías

**Backend**
- Java 21
- Spring Boot 4.0.7
- Spring Security + JWT
- Spring Data JPA + Hibernate
- PostgreSQL 16

**Frontend**
- React + Vite
- React Router
- Axios
- Recharts

**Infraestructura**
- Docker + Docker Compose

## Módulos

- Autenticación con JWT y roles (ADMIN, MESERO, CAJERO)
- Gestión de mesas con estados en tiempo real
- Menú de productos por categorías
- Pedidos con flujo de estados completo
- Caja y registro de pagos
- Dashboard con gráficas y asistente IA

## Cómo levantar el proyecto

### Requisitos
- Docker Desktop instalado y corriendo
- Node.js 20+

### Backend + Base de datos

```bash
cd mora
docker compose up -d
```

### Frontend

```bash
cd mora-frontend
npm install
npm run dev
```

Abre http://localhost:5173

### Credenciales de prueba

| Usuario | Email | Password | Rol |
|---|---|---|---|
| Admin MORA | admin@mora.com | admin123 | ADMIN |

## Endpoints principales

| Método | Endpoint | Descripción |
|---|---|---|
| POST | /auth/login | Iniciar sesión |
| POST | /auth/register | Registrar usuario |
| GET | /mesas | Listar mesas |
| GET | /productos | Listar productos |
| POST | /pedidos | Crear pedido |
| POST | /pagos | Registrar pago |
| GET | /reportes/resumen | Dashboard de ventas |