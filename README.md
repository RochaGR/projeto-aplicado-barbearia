# 💈 Barbearia - Sistema de Agendamento

Um sistema completo para gerenciamento de uma barbearia, desenvolvido com **Java Spring Boot** e **Angular**, oferecendo funcionalidades para clientes, barbeiros e administradores.  
O sistema permite **agendamentos online**, controle de serviços e gerenciamento de usuários com autenticação e autorização.

---

## 🚀 Funcionalidades

- 📅 **Agendamento de horários**  
- 💇 **Cadastro e gerenciamento de barbeiros**  
- 🧑 **Cadastro e gerenciamento de clientes**  
- ✂️ **Cadastro de serviços** (ex.: corte de cabelo, barba, etc.)  
- 🔐 **Autenticação e autorização** com **Spring Security**  
- 🖥️ **Painel administrativo** para controle de agendamentos e usuários  
- 🎨 **Frontend moderno com Angular 19**  
- 📱 **Interface responsiva e intuitiva**  

---

## 🛠️ Tecnologias Utilizadas

### Backend
- **Java 17+**
- **Spring Boot**
- **Spring Security**
- **Spring Data JPA (Hibernate)**
- **Maven**
- **PostgreSQL Database**

### Frontend
- **Angular 19**
- **TypeScript**
- **HTML5, CSS3**
<<<<<<< HEAD
- **Bootstrap**
=======
- **RxJS**
>>>>>>> 3986e7d

---

## 📂 Estrutura do Projeto

```
projeto-aplicado-barbearia/
├── barbearia/                 # Backend Spring Boot
│   ├── src/
│   │   └── main/
│   │       ├── java/         # Código Java
│   │       └── resources/    # Configurações
│   └── pom.xml               # Dependências Maven
├── barbearia-front/           # Frontend Angular
│   ├── src/
│   │   └── app/              # Código Angular
│   ├── package.json          # Dependências Node.js
│   └── angular.json          # Configuração Angular
└── README.md                 # Documentação
```

---

## 🚀 Como Executar

### Pré-requisitos
- Java 17+
- Node.js 18+
- PostgreSQL
- Maven

### Backend
```bash
cd barbearia
mvn spring-boot:run
```

### Frontend
```bash
cd barbearia-front
npm install
ng serve
```

A aplicação frontend estará disponível em `http://localhost:4200` e a API backend em `http://localhost:8080`.

