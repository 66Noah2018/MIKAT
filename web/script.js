/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

var parser = new DOMParser();

function getRandom(max){
    return (Math.floor(Math.random() * max));
}
/**
 * 
 * 
 * @returns {null}
 */
function openProject(){
    
}

/**
 * 
 * @param {type} node
 * @returns {undefined}
 */
function openRecentProject(node){
    // getAttribute('data-filename') always returns the filename of the selected project. project can be opened by searching for full path in array. full path will not be included in listview
    alert(node.getAttribute('data-filename'))
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

/**
 * 
 * @returns {undefined}
 */
function generateTestcases(){
    var spinner = $("#generate_testcases_load")[0];
    spinner.style.display = "block";
}

/**
 * 
 * 
 * @returns {undefined}
 */
function minimizeWindow(){
    var window = document.getElementById(event.path[3].id);
    window.classList.add("minimized")
    if (window.parentElement.classList[0] == "cell-2"){ 
        return;
    }
    var doc = parser.parseFromString(`<div class='cell-2' id=cell${getRandom(10)}></div>`, 'text/html');
    var cell = doc.body.firstChild;
    var row = document.getElementsByClassName("row")[0];
    row.appendChild(cell);
    var targetList = document.getElementsByClassName("cell-2");
    var target = targetList[targetList.length -1];
    target.appendChild(window);
}

/**
 * 
 * @returns {undefined}
 */
function maximizeWindow(){
    var window = document.getElementById(event.path[3].id);
    var cellid = window.parentElement.getAttribute('id');
    if (cellid == null){
        return;
    }
    var target = document.getElementsByClassName("index-body")[0];
    target.appendChild(window);
    document.getElementById(cellid).remove();
}