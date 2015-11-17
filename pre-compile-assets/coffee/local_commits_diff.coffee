colorDiff = (index) ->
  diff = CodeLoader.getDiff index

  if not diff
    return

  changeType = CodeLoader.getChangeType index

  if changeType == 'ADD'
    path = CodeLoader.getNewPath index
  else
    path = CodeLoader.getOldPath index

  highlighted = hljs.highlight "diff", diff, true
  diffContent = highlighted.value.split '\n'
  codes = diffContent[4..].join '\n'

  diffBlock = $('<div>', {class:'diff-block'})
  infoBlock = $('<div>', {class:'info-block'})
  pathBlock = $('<div>', {class:'path-block'}).html path
  changeTypeBlock = $('<div>', {class:'change-type'}).html changeType
  infoBlock.append changeTypeBlock
  infoBlock.append pathBlock

  codeBlock = $('<code>').html codes
  codeBlock = $('<pre>', {class:"diff"}).append codeBlock
  diffBlock.append infoBlock
  diffBlock.append codeBlock
  $('body').append diffBlock

window.notifyEntriesReady = () ->
  length = CodeLoader.getDiffSize()
  for index in [0..length-1] by 1
    colorDiff index

$(document).ready ()->
  CodeLoader.getDiffEntries()
