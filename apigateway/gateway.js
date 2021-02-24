const express = require('express')
const app = express()
const PORT = 3001
const routes = require('./routes/user')
const myconfig = require('./config')
const bp = require("body-parser");

const router = express.Router();
const cors = require("cors");
const axios = require("axios");

app.use(bp.json());
app.use(cors());

app.post('/login',(req,res) => {
    console.log(req.body)
    axios.post('http://localhost:8080/user/login',{
        username: req.body.username,
        password: req.body.password
      }).then(
          function(response) {
            console.log(response.data)
            res.send(response.data);
          }).catch(
            function(error){
                console.log(error)
    })
})

app.post('/register',(req,res) => {
    console.log(req.body)
    axios.post('http://localhost:8080/user/signup',{
        username: req.body.username,
        password: req.body.password,
        firstname:req.body.firstname,
        lastname:req.body.lastname,
        emailID:req.body.emailID,
      }).then(
          function(response) {
            console.log(response.data)
            res.send(response.data);
          }).catch(
            function(error){
                console.log(error)
    })
})


app.use(express.json())
app.listen(PORT, () => {
    console.log("port started on :" + PORT)
})