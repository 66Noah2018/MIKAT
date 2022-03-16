/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

let recentlyOpened = {};

/**
 * 
 * 
 * @returns {null}
 */
function openProject(){
    // via Java!!
}

/**
 * 
 * @param {type} node
 * @returns {undefined}
 */
function openRecentProject(node){
    // getAttribute('data-filename') always returns the filename of the selected project. project can be opened by searching for full path in array. full path will not be included in listview
    alert(node.getAttribute('data-filename'));
    filename = node.getAttribute('data-filename');
    fullPath = recentlyOpened[filename];
    // some connection to Java code to a) check existence of file (update list if file doesn't exist) and b) load file
}

/**
 * 
 * 
 * @returns {null}
 */
function saveProject(){
    
}

/**
 * 
 * @returns {undefined}
 */
function saveProjectAs(){
    
}

/**
 * 
 * @returns {null}
 */
function editProperties(){}

/**
 * 
 * @returns {null}
 */
function exportAsArden(){}

/**
 * 
 * @returns {null}
 */
function preferences(){}

function loadMappings(){
    var spinner = document.getElementById("process-mappings-load");
    spinner.style.display = "block";
    const dbMappings = JSON.parse(servletRequest("../chartservlet?function=localmap"));
    const standardMappings = JSON.parse(servletRequest("../chartservlet?function=standardmap"));
    const endTableCode = "</tbody></table>";
    
    let singularsTableDb = document.getElementById("database-mapping-table-singular");
    let pluralsTableDb = document.getElementById("database-mapping-table-plural");
    let singularsTableTerm = document.getElementById("standardized-mapping-table-singular");
    let pluralsTableTerm = document.getElementById("standardized-mapping-table-plural");
    
    let singularsTableDbCode = "<table id='database-mapping-singular'><thead><tr><th>Term</th><th>Database query</th></tr></thead><tbody contenteditable>";
    let pluralsTableDbCode = "<table id='database-mapping-plural'><thead><tr><th>Term</th><th>Database query</th></tr></thead><tbody contenteditable>";
    let singularsTableTermCode = "<table id='standardized-mapping-singular'><thead><tr><th>Term</th><th>Standardized term</th></tr></thead><tbody contenteditable>";
    let pluralsTableTermCode = "<table id='standardized-mapping-plural'><thead><tr><th>Term</th><th>Standardized term</th></tr></thead><tbody contenteditable>";
    try {
        Object.keys(dbMappings.singulars).forEach((key) => { singularsTableDbCode += `<tr><td>${key}</td><td>${dbMappings.singulars[key]}</td></tr>`;});
        Object.keys(dbMappings.plurals).forEach((key) => { pluralsTableDbCode += `<tr><td>${key}</td><td>${dbMappings.plurals[key]}</td></tr>`; });
        Object.keys(standardMappings.singulars).forEach((key) => { singularsTableTermCode += `<tr><td>${key}</td><td>${standardMappings.singulars[key]}</td></tr>`; });
        Object.keys(standardMappings.plurals).forEach((key) => { pluralsTableTermCode += `<tr><td>${key}</td><td>${standardMappings.plurals[key]}</td></tr>`; });
    } catch(e) {
        singularsTableDbCode += `<tr><td></td><td></td></tr>`;
        pluralsTableDbCode += `<tr><td></td><td></td></tr>`;
        singularsTableTermCode += `<tr><td></td><td></td></tr>`;
        pluralsTableTermCode += `<tr><td></td><td></td></tr>`;
    }

    singularsTableDbCode += endTableCode;
    pluralsTableDbCode += endTableCode;
    singularsTableTermCode += endTableCode;
    pluralsTableTermCode += endTableCode;
    
    singularsTableDb.appendChild(parser.parseFromString(singularsTableDbCode, 'text/html').body.firstChild);
    pluralsTableDb.appendChild(parser.parseFromString(pluralsTableDbCode, 'text/html').body.firstChild);
    singularsTableTerm.appendChild(parser.parseFromString(singularsTableTermCode, 'text/html').body.firstChild);
    pluralsTableTerm.appendChild(parser.parseFromString(pluralsTableTermCode, 'text/html').body.firstChild);
    spinner.style.display = "none";
}

function saveDbMapChanges(){
    let singulars = {};
    let plurals = {};
    const rowsSingular = document.getElementById("database-mapping-singular").rows;
    const rowsPlural = document.getElementById("database-mapping-plural").rows;
    for (let row of rowsSingular) {
        if (row.cells[0].innerText !== "" && row.cells[1].innerText !== "") {
            let key = row.cells[0].innerText.trim();
            let value = row.cells[1].innerText.trim();
            singulars[key] = value;
        }
    }
    delete singulars.Term;
    
    for (let row of rowsPlural) {
        if (row.cells[0].innerText !== "" && row.cells[1].innerText !== "") {
            let key = row.cells[0].innerText.trim();
            let value = row.cells[1].innerText.trim();
            plurals[key] = value;
        }
    }
    delete plurals.Term;
    
    const mapping = {"singulars": singulars, "plurals": plurals};
    
    servletRequestPost("../chartservlet?function=updateLocalMapping", mapping);
}

function saveTermMapChanges(){
    let singulars = {};
    let plurals = {};
    const rowsSingular = document.getElementById("standardized-mapping-singular").rows;
    const rowsPlural = document.getElementByid("standardized-mapping-plural").rows;
    for (let row of rowsSingular) {
        singulars[row.cells[0].innerHTML] = row.cells[1].innerHTML;
    }
    delete singulars.Term;
    
    for (let row of rowsPlural) {
        plurals[row.cells[0].innerHTML] = row.cells[1].innerHTML;
    }
    delete plurals.Term;
    
    const mapping = {"singulars": singulars, "plurals": plurals};
    servletRequestPost("../chartservlet?function=updateStandardizedMapping", mapping);
}

function addNewDBMapSingular() {
    let newRow = document.getElementById("database-mapping-singular").insertRow();
    newRow.insertCell(0);
    newRow.insertCell(0);
}

function addNewDbMapPlural() {
    let newRow = document.getElementById("database-mapping-plural").insertRow();
    newRow.insertCell(0);
    newRow.insertCell(0);
}

function addNewStandardMapSingular() {
    let newRow = document.getElementById("standardized-mapping-singular").insertRow();
    newRow.insertCell(0);
    newRow.insertCell(0);
}

function addNewStandardMapPlural() {
    let newRow = document.getElementById("standardized-mapping-plural").insertRow();
    newRow.insertCell(0);
    newRow.insertCell(0);
}

function servletRequestPost(url, body) {
    const http = new XMLHttpRequest();
    http.open("POST", url, true);
    http.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    http.send(JSON.stringify(body));
    if (http.readyState === 4 && http.status === 200) {
        return http.responseText;
    }
}