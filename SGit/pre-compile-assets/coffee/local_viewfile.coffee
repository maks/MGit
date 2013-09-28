rawCodes = undefined

displayFileContent = (lang) ->
  if not rawCodes
    rawCodes = CodeLoader.getCode()
  if not lang
    lang = CodeLoader.getLanguage()
  if lang
    highlighted = hljs.highlight lang, rawCodes, true
    $('.codes code').html highlighted.value
    $('.codes code').addClass highlighted.language
  else
    $('.codes code').html rawCodes
    # highlighted = hljs.highlightAuto rawCodes
  length = CodeLoader.getLineNumber()
  lineNumbersList = (i + '.' for i in [1 .. length])
  lineNumbers = lineNumbersList.join '\n'
  $('.line_numbers').html lineNumbers

window.notifyFileLoaded = () ->
  displayFileContent()

window.setLanguage = (lang) ->
  displayFileContent(lang)


$(document).ready ()->
  CodeLoader.loadCode()
