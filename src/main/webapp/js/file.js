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
