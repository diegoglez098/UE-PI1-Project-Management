const express = require("express");
const fs = require("fs");
const { get } = require("http");
const crypto = require("crypto");

const login = "login.json";
const projects = "projects.json";

var mysql = require("mysql2");

var loginData = {};
const app = express();
const port = 443;

app.use(express.json());
const clave = Buffer.from(
  "428867088027db5ae2d1c9c8c812e544adb4a4e9b9fe02ad02a265a7161f9d6b",
  "hex"
);
const iv = Buffer.from("cacf125174a7243db083fc6a319efe57", "hex");

var conn = mysql.createPool({
  host: "monorail.proxy.rlwy.net",
  user: "root",
  password: "AXtEqMtrwnrWecrrqYNavtCkflIFGwuD",
  database: "railway",
  port: 47256,
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0
});

var con = mysql.createConnection({
  host: "monorail.proxy.rlwy.net",
    user: "root",
    password: "AXtEqMtrwnrWecrrqYNavtCkflIFGwuD",
    database: "railway",
    port: 47256,
    });

const query = (sql, values) => {
  return new Promise((resolve, reject) => {
    conn.query(sql, values, (error, results) => {
      if (error) return reject(error);
      resolve(results);
    });
  });
};

function readProject() {
  fs.readFile(projects, "utf8", (err, data) => {
    if (err) {
      console.error("Error al leer el archivo:", err);

      return;
    }

    // Parsear el contenido JSON a un array de objetos
    const localData = JSON.parse(data);
    return localData;
  });
}
function generarClaveAES(descripcion) {
  return crypto.createHash("sha256").update(descripcion).digest();
}
function generarIV(descripcion) {
  const salt = crypto.randomBytes(16); // Salt (sal) aleatorio
  const iv = crypto.pbkdf2Sync(descripcion, salt, 100000, 16, "sha256"); // Generar IV utilizando PBKDF2
  return iv;
}
// Función para cifrar utilizando AES
function cifrarAES(mensaje, clave, iv) {
  const cipher = crypto.createCipheriv("aes-256-cbc", clave, iv);
  let encrypted = cipher.update(mensaje, "utf-8", "hex");
  encrypted += cipher.final("hex");
  return encrypted;
}

// Función para descifrar utilizando AES en modo CBC
function descifrarAES(mensajeCifrado, clave, iv) {
  const decipher = crypto.createDecipheriv("aes-256-cbc", clave, iv);
  let decrypted = decipher.update(mensajeCifrado, "hex", "utf-8");
  decrypted += decipher.final("utf-8");
  return decrypted;
}

function getLogin(username) {
  con.connect(function (err) {
    console.log("Connected!");
    var sql = "SELECT * FROM USERS WHERE userName = ?";
    con.query(sql, [username], function (err, result) {
      console.log(result[0]);
      const mensajeDescifrado = descifrarAES(result[0].userPasswd, clave, iv);
      loginData = {
        username: result[0].userName,
        password: mensajeDescifrado,
        rol: result[0].userRole,
      };
    });
  });
}

app.post("/api/v1/login", async (req, res) => {
  try {
    const { username, password } = req.body;
    const results = await query("SELECT * FROM USERS WHERE userName = ?", [username]);
    if (results.length > 0) {
      const user = results[0];
      const decryptedPassword = descifrarAES(user.userPasswd, clave, iv);
      if (decryptedPassword === password) {
        res.json({ status: "OK", rol: user.userRole });
      } else {
        res.json({ status: "ERROR" });
      }
    } else {
      res.json({ status: "ERROR" });
    }
  } catch (err) {
    console.error(err);
    res.status(500).json({ status: "ERROR" });
  }
});

app.post("/api/v1/addProject", async (req, res) => {
  try {
    const { nombre, ingeniero, comercial, cliente, importe, adjunto } = req.body;
    const today = new Date().toISOString().split('T')[0];
    const result = await query("SELECT COUNT(*) AS count FROM PROJECT");
    const projectId = result[0].count + 1;
    const values = [projectId, nombre, ingeniero, comercial, today, today, cliente, importe, "Pendiente", adjunto];
    await query("INSERT INTO PROJECT (proyId, proyName, proyAssignEng, proyAssignCom, proyRegDate, proyModDate, proyClient, proyAmount, proyStatus, proyAttach) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", values);
    res.json({ status: "OK" });
  } catch (err) {
    console.error(err);
    res.status(500).json({ status: "ERROR" });
  }
});
// Endpoint de prueba con método POST
app.post("/api/v1/getProjects", async (req, res) => {
  try {
    const { ingeniero } = req.body;
    const results = await query("SELECT * FROM PROJECT WHERE proyAssignEng = ?", [ingeniero]);
    res.json({ status: "OK", projects: results });
  } catch (err) {
    console.error(err);
    res.status(500).json({ status: "ERROR" });
  }
});
app.post("/api/v1/getProjectsComercial", (req, res) => {
  var projectsToCom = {};
  fs.readFile(projects, "utf8", (err, data) => {
    if (err) {
      console.error("Error al leer el archivo:", err);
      res.json({ status: "ERROR" });
      return;
    }

    // Parsear el contenido JSON a un array de objetos
    const localData = JSON.parse(data);
    for (var i = 0; i < localData.length; i++) {
      if (
        localData[i].comercial === req.body.comercial &&
        (localData[i].estado == "Entregado" ||
          localData[i].estado == "Desestimado")
      ) {
        projectsToCom[i + 1] = localData[i];
      }
    }

    console.log("Sent comercial requests asociated to " + req.body.comercial);
    res.json(projectsToCom);
  });
});

app.post("/api/v1/updateProject", async (req, res) => {
  datos = req.body;
  var originalProject = {};
  var nombre = datos.nombre;
  var cliente = datos.cliente;
  var importe = datos.importe;
  var estado = datos.estado;
  var ingeniero = datos.ingeniero;
  var fecha = datos.fechaEntrega;
  var comercial = datos.comercial;
  var projectId;

  con.connect(function (err) {
    if (err) throw err;
    projectId = datos.id;
    var sql = "SELECT * from PROJECT where proyId = ?";
    try {
      con.query(sql, [projectId], function (err, result) {
        if (err) throw err;
        originalProject = {
          proyName: result[0].proyName,
          proyClient: result[0].proyClient,
          proyAmount: result[0].proyAmount,
          proyStatus: result[0].proyStatus,
          proyAssignEng: result[0].proyAssignEng,
          proyModDate: result[0].proyModDate,
          proyAssignCom: result[0].proyAssignCom,
        };
        con.connect(function (err) {
          var updatedProject = {
            proyName: nombre == "" ? originalProject.proyName : nombre,
            proyClient: cliente == "" ? originalProject.proyClient : cliente,
            proyAmount: importe == "" ? originalProject.proyAmount : importe,
            proyStatus: estado == null ? originalProject.proyStatus : estado,
            proyAssignEng:
              ingeniero == "" ? originalProject.proyAssignEng : ingeniero,
            proyModDate: fecha == "" ? originalProject.proyModDate : fecha,
            proyAssignCom:
              comercial == "" ? originalProject.proyAssignCom : comercial,
          };
          var sql =
            "UPDATE PROJECT SET proyName = ?, proyClient = ?, proyAmount = ?, proyStatus = ?, proyAssignEng = ?, proyModDate = ?, proyAssignCom = ? WHERE proyId = ?";
          var values = [
            updatedProject.proyName,
            updatedProject.proyClient,
            updatedProject.proyAmount,
            updatedProject.proyStatus,
            updatedProject.proyAssignEng,
            updatedProject.proyModDate,
            updatedProject.proyAssignCom,
            projectId,
          ];

          con.query(sql, values, function (err, result) {
            if (err) throw err;
            console.log("Number of records updated: " + result.affectedRows);
            res.json({ status: "OK" });
          });
        });
      });
    } catch (err) {
      console.log(err);
    }
  });
});

// Iniciar el servidor
app.listen(port, () => {
  console.log(`Servidor escuchando en http://localhost:${port}`);
});
