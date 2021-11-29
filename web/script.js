/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

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