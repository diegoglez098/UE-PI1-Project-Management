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

var con = mysql.createConnection({
  host: "monorail.proxy.rlwy.net",
  user: "root",
  password: "AXtEqMtrwnrWecrrqYNavtCkflIFGwuD",
  database: "railway",
  port: 47256,
});

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
  await getLogin(req.body.username);

  console.log(req.body);
  if (loginData && loginData.password == req.body.password) {
    console.log("Login OK");
    res.json({ status: "OK", rol: loginData.rol });
    loginData={};
  } else {
    res.json({ status: "ERROR" });
  }
});

app.post("/api/v1/addProject", async (req, res) => {
  console.log(req.body);
  req.body.estado = "Pendiente";

  var ingenieroId;
  var comercialId;
  var projectId;
  await con.connect(function (err) {
    console.log("Connected!");
    var sql = "SELECT * FROM PROJECT";
    con.query(sql, function (err, result) {
      console.log(result);
      projectId = result.length + 1;
    });
  });

  await con.connect(function (err) {
    if (err) throw err;
    var today = new Date();
    var dd = String(today.getDate()).padStart(2, "0");
    var mm = String(today.getMonth() + 1).padStart(2, "0"); //January is 0!
    var yyyy = today.getFullYear();

    today = yyyy + "-" + mm + "-" + dd;

    console.log("Connected!");
    var sql =
      "INSERT INTO PROJECT (proyId, proyName, proyAssignEng,proyAssignCom, proyRegDate,proyModDate,proyClient,proyAmount,proyStatus, proyAttach) VALUES ?";

    var values = [
      [
        projectId,
        req.body.nombre,
        req.body.ingeniero,
        req.body.comercial,
        today,
        today,
        req.body.cliente,
        req.body.importe,
        req.body.estado,
        req.body.adjunto,
      ],
    ];
    con.query(sql, [values], function (err, result) {
      if (err) throw err;
      console.log("Number of records inserted: " + result.affectedRows);
      res.json({ status: "OK" });
    });
  });
  fs.readFile(projects, "utf8", (err, data) => {
    if (err) {
      console.error("Error al leer el archivo:", err);

      return;
    }

    // Parsear el contenido JSON a un array de objetos
    const localData = JSON.parse(data);
    req.body.id = localData.length;
  });
});
// Endpoint de prueba con método POST
app.post("/api/v1/getProjects", (req, res) => {
  var projectsToIng = {};

  con.connect(function (err) {
    var sql = "SELECT * FROM PROJECT WHERE proyAssignEng = ?";
    con.query(sql, [req.body.ingeniero], function (err, result) {
      console.log(result);
      res.json({ status: "OK", projects: result });
    });
  });
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
