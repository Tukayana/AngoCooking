const express = require("express");
const mysql = require("mysql2");
const bodyParser = require("body-parser");
const jwt = require("jsonwebtoken");
const bcrypt = require("bcrypt");
const multer = require("multer");
const path = require("path");
const swaggerJsdoc = require('swagger-jsdoc');
const swaggerUi = require('swagger-ui-express');
require("dotenv").config();

const app = express();
const port = process.env.PORT || 3000;

// Swagger definition
const swaggerOptions = {
  definition: {
    openapi: '3.0.0',
    info: {
      title: 'API de Receitas',
      version: '1.0.0',
      description: 'API para gerenciamento de receitas e usuários',
    },
    servers: [
      {
        url: `http://localhost:${port}`,
        description: 'Servidor de desenvolvimento',
      },
    ],
    components: {
      securitySchemes: {
        bearerAuth: {
          type: 'http',
          scheme: 'bearer',
          bearerFormat: 'JWT',
        },
      },
    },
    security: [
      {
        bearerAuth: [],
      },
    ],
  },
  apis: ['./index.js'], 
};

const swaggerSpec = swaggerJsdoc(swaggerOptions);
app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerSpec));

app.use(bodyParser.json());
app.use('/uploads', express.static('uploads'));

// Configuração do Multer para upload de imagens//
const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    cb(null, 'uploads/')
  },
  filename: function (req, file, cb) {
    cb(null, Date.now() + path.extname(file.originalname))
  }
});

const upload = multer({ 
  storage: storage,
  fileFilter: function (req, file, cb) {
    if (!file.originalname.match(/\.(jpg|jpeg|png)$/)) {
      return cb(new Error('Apenas imagens são permitidas!'));
    }
    cb(null, true);
  }
});

// Conexão com o banco de dados MySQL//
const db = mysql.createConnection({
  host: process.env.DB_HOST,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME,
});

db.connect((err) => {
  if (err) {
    console.error("Erro de conexão com o banco de dados:", err);
    process.exit(1);
  }
  console.log("Conectado ao banco de dados MySQL");
});
///////////////tabelas//////////////////
db.query(
  `CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    senha VARCHAR(255) NOT NULL,
    foto_perfil VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
  )`,
  (err) => {
    if (err) console.error("Erro ao criar tabela de usuários:", err);
  }
);


db.query(
  `CREATE TABLE IF NOT EXISTS receitas (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(255) NOT NULL,
    ingredientes TEXT NOT NULL,
    modoPreparo TEXT NOT NULL,
    imagem VARCHAR(255),
    usuarioId INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuarioId) REFERENCES usuarios(id) ON DELETE CASCADE
  )`,
  (err) => {
    if (err) console.error("Erro ao criar tabela de receitas:", err);
  }
);


db.query(
  `CREATE TABLE IF NOT EXISTS comentarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    texto TEXT NOT NULL,
    usuarioId INT,
    receitaId INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (usuarioId) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (receitaId) REFERENCES receitas(id) ON DELETE CASCADE
  )`,
  (err) => {
    if (err) console.error("Erro ao criar tabela de comentários:", err);
  }
);



const authenticateToken = (req, res, next) => {
  const authHeader = req.headers['authorization'];
  const token = authHeader && authHeader.split(' ')[1];

  if (!token) {
    return res.status(401).json({ error: "Token não fornecido" });
  }

  jwt.verify(token, process.env.JWT_SECRET, (err, user) => {
    if (err) {
      return res.status(403).json({ error: "Token inválido" });
    }
    req.user = user;
    next();
  });
};

/**
 * @swagger
 * /register:
 *   post:
 *     summary: Registra um novo usuário
 *     tags: [Usuários]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - nome
 *               - email
 *               - senha
 *             properties:
 *               nome:
 *                 type: string
 *               email:
 *                 type: string
 *               senha:
 *                 type: string
 *     responses:
 *       201:
 *         description: Usuário registrado com sucesso
 *       400:
 *         description: Dados inválidos
 */
app.post("/register", async (req, res) => {
  const { nome, email, senha } = req.body;
  
  if (!nome || !email || !senha) {
    return res.status(400).json({ error: "Todos os campos são obrigatórios" });
  }

  try {
    const hashedSenha = await bcrypt.hash(senha, 10);
    
    db.query(
      "INSERT INTO usuarios (nome, email, senha) VALUES (?, ?, ?)",
      [nome, email, hashedSenha],
      function (err, result) {
        if (err) {
          if (err.code === 'ER_DUP_ENTRY') {
            return res.status(400).json({ error: "Email já cadastrado" });
          }
          return res.status(500).json({ error: err.message });
        }
        
        const token = jwt.sign({ id: result.insertId }, process.env.JWT_SECRET, {
          expiresIn: '24h'
        });
        
        res.status(201).json({ token });
      }
    );
  } catch (err) {
    res.status(500).json({ error: err.message });
  }
});

/**
 * @swagger
 * /login:
 *   post:
 *     summary: Realiza login do usuário
 *     tags: [Usuários]
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - email
 *               - senha
 *             properties:
 *               email:
 *                 type: string
 *               senha:
 *                 type: string
 *     responses:
 *       200:
 *         description: Login realizado com sucesso
 *       401:
 *         description: Credenciais inválidas
 */
app.post("/login", (req, res) => {
  const { email, senha } = req.body;
  
  if (!email || !senha) {
    return res.status(400).json({ error: "Email e senha são obrigatórios" });
  }

  db.query(
    "SELECT * FROM usuarios WHERE email = ?",
    [email],
    async function (err, results) {
      if (err) return res.status(500).json({ error: err.message });
      
      if (results.length === 0) {
        return res.status(401).json({ error: "Credenciais inválidas" });
      }

      const user = results[0];
      
      const validPassword = await bcrypt.compare(senha, user.senha);
      if (!validPassword) {
        return res.status(401).json({ error: "Credenciais inválidas" });
      }

      const token = jwt.sign({ id: user.id }, process.env.JWT_SECRET, {
        expiresIn: '24h'
      });
      
      res.json({ token, user});
    }
  );
});

/**
 * @swagger
 * /users/profile-photo:
 *   put:
 *     summary: Atualiza a foto de perfil do usuário
 *     tags: [Usuários]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         multipart/form-data:
 *           schema:
 *             type: object
 *             properties:
 *               foto:
 *                 type: string
 *                 format: binary
 *     responses:
 *       200:
 *         description: Foto de perfil atualizada com sucesso
 *       401:
 *         description: Não autorizado
 */
app.put("/users/profile-photo", authenticateToken, upload.single('foto'), (req, res) => {
  const usuarioId = req.user.id;
  const fotoUrl = req.file ? `/uploads/${req.file.filename}` : null;

  if (!fotoUrl) {
    return res.status(400).json({ error: "Nenhuma foto foi enviada" });
  }

  db.query(
    "UPDATE usuarios SET foto_perfil = ? WHERE id = ?",
    [fotoUrl, usuarioId],
    function (err, result) {
      if (err) return res.status(500).json({ error: err.message });
      res.json({ 
        message: "Foto de perfil atualizada com sucesso",
        fotoUrl: fotoUrl 
      });
    }
  );
});

/**
 * @swagger
 * /users/profile:
 *   get:
 *     summary: Obtém o perfil do usuário
 *     tags: [Usuários]
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Dados do perfil do usuário
 *       401:
 *         description: Não autorizado
 */
app.get("/users/profile", authenticateToken, (req, res) => {
  const usuarioId = req.user.id;

  db.query(
    "SELECT id, nome, email, foto_perfil, created_at FROM usuarios WHERE id = ?",
    [usuarioId],
    function (err, results) {
      if (err) return res.status(500).json({ error: err.message });
      if (results.length === 0) {
        return res.status(404).json({ error: "Usuário não encontrado" });
      }
      res.json(results[0]);
    }
  );
});

/**
 * @swagger
 * /receitas:
 *   post:
 *     summary: Cria uma nova receita
 *     tags: [Receitas]
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         multipart/form-data:
 *           schema:
 *             type: object
 *             required:
 *               - nome
 *               - ingredientes
 *               - modoPreparo
 *             properties:
 *               nome:
 *                 type: string
 *               ingredientes:
 *                 type: string
 *               modoPreparo:
 *                 type: string
 *               imagem:
 *                 type: string
 *                 format: binary
 *     responses:
 *       201:
 *         description: Receita criada com sucesso
 *       400:
 *         description: Dados inválidos
 */
app.post("/receitas", authenticateToken, upload.single('imagem'), (req, res) => {
  const { nome, ingredientes, modoPreparo } = req.body;
  const usuarioId = req.user.id;
  const imagemUrl = req.file ? `/uploads/${req.file.filename}` : null;

  if (!nome || !ingredientes || !modoPreparo) {
    return res.status(400).json({ error: "Campos obrigatórios não preenchidos" });
  }

  db.query(
    "INSERT INTO receitas (nome, ingredientes, modoPreparo, imagem, usuarioId) VALUES (?, ?, ?, ?, ?)",
    [nome, ingredientes, modoPreparo, imagemUrl, usuarioId],
    function (err, result) {
      if (err) return res.status(500).json({ error: err.message });
      res.status(201).json({ 
        id: result.insertId,
        imagemUrl: imagemUrl 
      });
    }
  );
});

/**
 * @swagger
 * /receitas:
 *   get:
 *     summary: Lista todas as receitas
 *     tags: [Receitas]
 *     responses:
 *       200:
 *         description: Lista de receitas
 */
app.get("/receitas", (req, res) => {
  db.query(
    `SELECT r.*, u.nome as autorNome, 
     (SELECT COUNT(*) FROM comentarios c WHERE c.receitaId = r.id) as totalComentarios 
     FROM receitas r 
     JOIN usuarios u ON r.usuarioId = u.id
     ORDER BY r.created_at DESC`,
    (err, results) => {
      if (err) return res.status(500).json({ error: err.message });
      res.json(results);
    }
  );
});

/**
 * @swagger
 * /receitas/{id}:
 *   get:
 *     summary: Obtém uma receita específica
 *     tags: [Receitas]
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *     responses:
 *       200:
 *         description: Dados da receita
 *       404:
 *         description: Receita não encontrada
 */
app.get("/receitas/:id", (req, res) => {
  const { id } = req.params;
  
  db.query(
    `SELECT r.*, u.nome as autorNome
     FROM receitas r 
     JOIN usuarios u ON r.usuarioId = u.id 
     WHERE r.id = ?`,
    [id],
    (err, results) => {
      if (err) return res.status(500).json({ error: err.message });
      if (results.length === 0) {
        return res.status(404).json({ error: "Receita não encontrada" });
      }
      res.json(results[0]);
    }
  );
});

/**
 * @swagger
 * /receitas/{id}:
 *   put:
 *     summary: Atualiza uma receita
 *     tags: [Receitas]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *     requestBody:
 *       required: true
 *       content:
 *         multipart/form-data:
 *           schema:
 *             type: object
 *             properties:
 *               nome:
 *                 type: string
 *               ingredientes:
 *                 type: string
 *               modoPreparo:
 *                 type: string
 *               imagem:
 *                 type: string
 *                 format: binary
 *     responses:
 *       200:
 *         description: Receita atualizada com sucesso
 *       404:
 *         description: Receita não encontrada
 */
app.put("/receitas/:id", authenticateToken, upload.single('imagem'), (req, res) => {
  const { id } = req.params;
  const { nome, ingredientes, modoPreparo } = req.body;
  const usuarioId = req.user.id;
  const imagemUrl = req.file ? `/uploads/${req.file.filename}` : null;

  let updateQuery = "UPDATE receitas SET ";
  const updateValues = [];
  const updateFields = [];

  if (nome) {
    updateFields.push("nome = ?");
    updateValues.push(nome);
  }
  if (ingredientes) {
    updateFields.push("ingredientes = ?");
    updateValues.push(ingredientes);
  }
  if (modoPreparo) {
    updateFields.push("modoPreparo = ?");
    updateValues.push(modoPreparo);
  }
  if (imagemUrl) {
    updateFields.push("imagem = ?");
    updateValues.push(imagemUrl);
  }

  if (updateFields.length === 0) {
    return res.status(400).json({ error: "Nenhum campo para atualizar" });
  }

  updateQuery += updateFields.join(", ");
  updateQuery += " WHERE id = ? AND usuarioId = ?";
  updateValues.push(id, usuarioId);

  db.query(updateQuery, updateValues, function (err, result) {
    if (err) return res.status(500).json({ error: err.message });
    if (result.affectedRows === 0) {
      return res.status(404).json({ error: "Receita não encontrada ou não autorizado" });
    }
    res.json({ message: "Receita atualizada com sucesso" });
  });
});

/**
 * @swagger
 * /receitas/{id}:
 *   delete:
 *     summary: Deleta uma receita
 *     tags: [Receitas]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *     responses:
 *       200:
 *         description: Receita deletada com sucesso
 *       404:
 *         description: Receita não encontrada
 */
app.delete("/receitas/:id", authenticateToken, (req, res) => {
  const { id } = req.params;
  const usuarioId = req.user.id;

  db.query(
    "DELETE FROM receitas WHERE id = ? AND usuarioId = ?",
    [id, usuarioId],
    function (err, result) {
      if (err) return res.status(500).json({ error: err.message });
      if (result.affectedRows === 0) {
        return res.status(404).json({ error: "Receita não encontrada ou não autorizado" });
      }
      res.json({ message: "Receita excluída com sucesso" });
    }
  );
});

/**
 * @swagger
 * /receitas/{receitaId}/comentarios:
 *   post:
 *     summary: Adiciona um comentário a uma receita
 *     tags: [Comentários]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: receitaId
 *         required: true
 *         schema:
 *           type: integer
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - texto
 *             properties:
 *               texto:
 *                 type: string
 *     responses:
 *       201:
 *         description: Comentário criado com sucesso
 */
app.post("/receitas/:receitaId/comentarios", authenticateToken, (req, res) => {
  const { receitaId } = req.params;
  const { texto } = req.body;
  const usuarioId = req.user.id;

  if (!texto) {
    return res.status(400).json({ error: "O comentário não pode estar vazio" });
  }

  db.query(
    "INSERT INTO comentarios (texto, usuarioId, receitaId) VALUES (?, ?, ?)",
    [texto, usuarioId, receitaId],
    function (err, result) {
      if (err) return res.status(500).json({ error: err.message });
      res.status(201).json({ 
        id: result.insertId,
        texto,
        usuarioId,
        receitaId
      });
    }
  );
});

/**
 * @swagger
 * /receitas/{receitaId}/comentarios:
 *   get:
 *     summary: Lista comentários de uma receita
 *     tags: [Comentários]
 *     parameters:
 *       - in: path
 *         name: receitaId
 *         required: true
 *         schema:
 *           type: integer
 *     responses:
 *       200:
 *         description: Lista de comentários
 */
app.get("/receitas/:receitaId/comentarios", (req, res) => {
  const { receitaId } = req.params;

  db.query(
    `SELECT c.*, u.nome as autorNome 
     FROM comentarios c 
     JOIN usuarios u ON c.usuarioId = u.id 
     WHERE c.receitaId = ? 
     ORDER BY c.created_at DESC`,
    [receitaId],
    (err, results) => {
      if (err) return res.status(500).json({ error: err.message });
      res.json(results);
    }
  );
});

/**
 * @swagger
 * /comentarios/{id}:
 *   put:
 *     summary: Atualiza um comentário
 *     tags: [Comentários]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - texto
 *             properties:
 *               texto:
 *                 type: string
 *     responses:
 *       200:
 *         description: Comentário atualizado com sucesso
 *       404:
 *         description: Comentário não encontrado
 */
app.put("/comentarios/:id", authenticateToken, (req, res) => {
  const { id } = req.params;
  const { texto } = req.body;
  const usuarioId = req.user.id;

  if (!texto) {
    return res.status(400).json({ error: "O comentário não pode estar vazio" });
  }

  db.query(
    "UPDATE comentarios SET texto = ? WHERE id = ? AND usuarioId = ?",
    [texto, id, usuarioId],
    function (err, result) {
      if (err) return res.status(500).json({ error: err.message });
      if (result.affectedRows === 0) {
        return res.status(404).json({ error: "Comentário não encontrado ou não autorizado" });
      }
      res.json({ message: "Comentário atualizado com sucesso" });
    }
  );
});

/**
 * @swagger
 * /comentarios/{id}:
 *   delete:
 *     summary: Deleta um comentário
 *     tags: [Comentários]
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - in: path
 *         name: id
 *         required: true
 *         schema:
 *           type: integer
 *     responses:
 *       200:
 *         description: Comentário deletado com sucesso
 *       404:
 *         description: Comentário não encontrado
 */
app.delete("/comentarios/:id", authenticateToken, (req, res) => {
  const { id } = req.params;
  const usuarioId = req.user.id;

  db.query(
    "DELETE FROM comentarios WHERE id = ? AND usuarioId = ?",
    [id, usuarioId],
    function (err, result) {
      if (err) return res.status(500).json({ error: err.message });
      if (result.affectedRows === 0) {
        return res.status(404).json({ error: "Comentário não encontrado ou não autorizado" });
      }
      res.json({ message: "Comentário excluído com sucesso" });
    }
  );
});
/** 
@swagger
* /receitas/search:
*   get:
*     summary: Pesquisa receitas por nome ou ingredientes
*     tags: [Receitas]
*     parameters:
*       - in: query
*         name: q
*         schema:
*           type: string
*         description: Termo de pesquisa para nome ou ingredientes
*     responses:
*       200:
*         description: Lista de receitas encontradas
*/
app.get("/receitas/search", (req, res) => {
 const searchTerm = req.query.q;
 
 if (!searchTerm) {
   return res.status(400).json({ error: "Termo de pesquisa é obrigatório" });
 }

 const query = `
   SELECT r.*, u.nome as autorNome,
   (SELECT COUNT(*) FROM comentarios c WHERE c.receitaId = r.id) as totalComentarios 
   FROM receitas r 
   JOIN usuarios u ON r.usuarioId = u.id
   WHERE r.nome LIKE ? OR r.ingredientes LIKE ?
   ORDER BY r.created_at DESC
 `;
 
 const searchPattern = `%${searchTerm}%`;
 
 db.query(query, [searchPattern, searchPattern], (err, results) => {
   if (err) return res.status(500).json({ error: err.message });
   res.json(results);
 });
});

app.listen(port, () => {
  console.log(`Servidor rodando na porta http://localhost:${port}`);
  console.log(`Documentação Swagger disponível em http://localhost:${port}/api-docs`);
});