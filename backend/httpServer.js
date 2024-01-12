const express = require('express');
const fs = require('fs');

const login = 'login.json';
const projects = 'projects.json';

var loginData = {}

const app = express();
const port = 3000;

app.use(express.json());

function readProject() {
    fs.readFile(projects, 'utf8', (err, data) => {
        if (err) {
            console.error('Error al leer el archivo:', err);
            
            return;
        }

        // Parsear el contenido JSON a un array de objetos
        const localData = JSON.parse(data);
        return localData
    });
}

function readLoginJson() {
    fs.readFile(login, 'utf8', (err, data) => {
        if (err) {
            console.error('Error al leer el archivo:', err);
            return;
        }
        // Parsear el contenido JSON
        loginData = JSON.parse(data);
    });
}

// Endpoint de prueba
app.get('/', (req, res) => {
    res.send('¡Hola, mundo!');
});

app.post('/api/v1/login', (req, res) => {
    readLoginJson()
    console.log(req.body)
    if (loginData[req.body.username]) {
        if (loginData[req.body.username].password == req.body.password) {
            console.log("Login OK")
            res.json({ "status": "OK", "rol": loginData[req.body.username].rol })
        }
        else {
            res.json({ "status": "ERROR" })
        }
    }
    else {
        res.json({ "status": "ERROR" })

    }

});
app.post('/api/v1/addProject', (req, res) => {
    console.log(req.body)
    fs.readFile(projects, 'utf8', (err, data) => {
        if (err) {
            console.error('Error al leer el archivo:', err);
            
            return;
        }

        // Parsear el contenido JSON a un array de objetos
        const localData = JSON.parse(data);
        req.body.estado = "Pendiente"
        req.body.id = localData.length
        
        localData.push(req.body)
    
        const updatedJson = JSON.stringify(localData, null, 2);
    
        fs.writeFile(projects, updatedJson, 'utf8', (err) => {
            if (err) {
                console.error('Error al escribir en el archivo:', err);
                res.json({ "status": "ERROR" })
                return;
            }
            res.json({ "status": "OK" })
            console.log('Nuevo objeto agregado correctamente al archivo.');
        });
    });
   

});
// Endpoint de prueba con método POST
app.post('/api/v1/getProjects', (req, res) => {
    var projectsToIng = []
    fs.readFile(projects, 'utf8', (err, data) => {
        if (err) {
            console.error('Error al leer el archivo:', err);
            res.json({ "status": "ERROR" })
            return;
        }

        // Parsear el contenido JSON a un array de objetos
        const localData = JSON.parse(data);
        for (var i=0;i<localData.length;i++){
            if (localData[i].ingeniero === req.body.ingeniero){
                projectsToIng.push(localData[i])
            }
        }
        console.log("Sent projects asociated to "+req.body.ingeniero)
        res.send(projectsToIng);
    });
});



// Iniciar el servidor
app.listen(port, () => {
    console.log(`Servidor escuchando en http://localhost:${port}`);
});
