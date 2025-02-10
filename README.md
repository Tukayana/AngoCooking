# Título e Tema do Projeto

## Título: "AngoCooking - Aplicativo de Compartilhamento de Receitas"

## Resumo
O AngoCooking é um aplicativo que permite aos usuários criar uma conta, fazer login, publicar receitas e comentar em cada receita. Além disso, os usuários podem visualizar seu perfil, onde podem ver as receitas já publicadas e fazer alterações ou excluir publicações. O aplicativo utiliza Kotlin e Jetpack Compose para a interface do usuário, e uma API REST para o gerenciamento de dados, como autenticação de usuários, publicação de receitas e comentários.

## Estrutura da Aplicação
O aplicativo foi desenvolvido usando Kotlin e Jetpack Compose para a construção das interfaces de usuário. A comunicação entre a interface do usuário e o backend é feita via API REST, que gerencia operações CRUD (Create, Read, Update, Delete) para usuários, receitas e comentários.

### Componentes principais da aplicação:

#### Tela Inicial (Splash Screen)
- Exibe o logo e o nome do aplicativo por alguns segundos antes de redirecionar para a tela de login.

#### Tela de Login
- Permite a autenticação do usuário com um sistema básico de login, onde os dados de login são validados via API REST.
- Validação de campos obrigatórios (e-mail e senha).
- Mensagens de erro em caso de falha de autenticação.

#### Tela de Cadastro de Usuário (RegisterScreen)
- Permite o cadastro de novos usuários, incluindo validação de campos obrigatórios (nome, e-mail e senha).
- O cadastro é feito via API REST.

#### Tela Principal (HomeScreen)
- Exibe a lista de receitas publicadas, com informações como nome, ingredientes e modo de preparo.
- A lista é populada a partir de dados recebidos da API REST.
- Permite a navegação para a tela de detalhes da receita.

#### Tela de Detalhes da Receita (RecipeDetailScreen)
- Exibe os detalhes completos de uma receita, incluindo ingredientes, modo de preparo e comentários.
- Permite ao usuário adicionar comentários à receita.

#### Tela de Perfil (ProfileScreen)
- Exibe as informações do usuário, como nome, e-mail e foto de perfil.
- Permite ao usuário visualizar e gerenciar as receitas que publicou, com opções de edição e exclusão.

#### Tela de Criação de Receita (CreateRecipeScreen)
- Permite ao usuário criar uma nova receita, fornecendo nome, ingredientes, modo de preparo e uma imagem.
- A receita é enviada para o backend via API REST.

#### Tela de Edição de Receita (EditRecipeScreen)
- Permite ao usuário editar uma receita já publicada, com opções para atualizar nome, ingredientes, modo de preparo e imagem.
- As alterações são enviadas para o backend via API REST.

## Funcionalidades e Tecnologias Usadas
- Kotlin: Linguagem principal para desenvolvimento do aplicativo Android.
- Jetpack Compose: Usado para construção das interfaces de usuário (UI), como telas de login, home, cadastro de receitas, etc.
- API REST: Criada para gerenciar as interações entre o front-end (Jetpack Compose) e o banco de dados remoto. A API permite a comunicação de dados, como registros de usuários, receitas e comentários.
- Splash Screen: Exibe uma tela inicial mostrando o logo e o nome do aplicativo.
- Validações de Campos: A aplicação garante que os campos obrigatórios estejam preenchidos corretamente ao fazer login, cadastro e ao adicionar/editar receitas.

## Design e Interface de Usuário

### Tela de Login
- Campos de entrada para e-mail e senha.
- Botão para autenticação.
- Mensagens de erro em caso de falha no login.
- Validação dos campos de e-mail e senha.

### Tela de Home
- Lista de receitas, com nome, ingredientes e modo de preparo.
- A lista de receitas é carregada a partir da API REST.
- Permite a navegação para a tela de detalhes da receita.

### Tela de Detalhes da Receita
- Exibe os detalhes completos da receita, incluindo ingredientes, modo de preparo e comentários.
- Permite ao usuário adicionar comentários.

### Tela de Perfil
- Exibe as informações do usuário, como nome, e-mail e foto de perfil.
- Permite ao usuário visualizar e gerenciar as receitas que publicou.

### Tela de Criação de Receita
- Campos para adicionar uma nova receita (nome, ingredientes, modo de preparo e imagem).
- Botão para efetuar a publicação

### Tela de Edição de Receita
- Campos para editar uma receita já publicada (nome, ingredientes, modo de preparo e imagem).
- Botão para editar

## Backend: API REST
A API REST foi desenvolvida utilizando Node.js e Express, com Mysql como base de dados e multer para upload de image mem disco.

A API REST é capaz de realizar operações como:
- POST para criar novos usuários, receitas e comentários.
- GET para listar ou buscar receitas e comentários.
- PUT/para atualizar as informações de receitas e comentários.
- DELETE para remover receitas e comentários.

## Rotas do AngoCooking

### Autenticação
- POST /register – Cadastro de novos usuários, armazenando nome, e-mail e senha.
- POST /login – Autenticação de usuários, verificando credenciais e retornando um token JWT.

### Usuários
- GET /users/profile – Obtém os dados do perfil do usuário autenticado, incluindo nome, e-mail e foto de perfil.

### Receitas
- POST /receitas – Criação de novas receitas, incluindo nome, ingredientes, modo de preparo e imagem.
- GET /receitas – Retorna a lista de todas as receitas cadastradas.
- GET /receitas/:id – Obtém detalhes de uma receita específica com base no seu ID.
- PUT /receitas/:id – Atualiza uma receita específica, permitindo modificar nome, ingredientes, modo de preparo e imagem.
- DELETE /receitas/:id – Remove uma receita específica cadastrada pelo usuário autenticado.

### Comentários
- POST /receitas/:receitaId/comentarios – Adiciona um comentário a uma receita específica.
- GET /receitas/:receitaId/comentarios – Lista todos os comentários de uma determinada receita.
- PUT /comentarios/:id – Atualiza um comentário específico, garantindo que apenas o autor possa editá-lo.
- DELETE /comentarios/:id – Exclui um comentário específico feito pelo usuário autenticado.

### Busca
- GET /receitas/search – Permite buscar receitas pelo nome ou pelos ingredientes.
