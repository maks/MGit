String.prototype.rtrim = () ->
  return this.replace /\s+$/,''

lang = undefined
editor = undefined

displayFileContent = () ->
  rawCodes = CodeLoader.getCode()
  $('#editor').text rawCodes
  editorElm = document.getElementById "editor"
  editorOption =
    lineNumbers: true
    mode: lang
    matchBrackets: true
    lineWrapping: true
    readOnly: true
  editor = CodeMirror.fromTextArea editorElm, editorOption

window.setLang = (l) ->
  lang = l
  if editor
    editor.setOption "mode", lang

window.display = displayFileContent

window.setEditable= () ->
  editor.setOption "readOnly", false

window.save = () ->
  editor.setOption "readOnly", true
  value = editor.getValue().rtrim()
  CodeLoader.save(value)

$(document).ready ()->
  CodeLoader.loadCode()
